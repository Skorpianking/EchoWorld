package Ants;

import Sample.behaviors.AvoidObstacle;
import Vehicles.Action;
import Vehicles.SensedObject;
import Vehicles.State;
import Sample.behaviors.GotoXX;
import Vehicles.Vehicle;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.CompositeBehavior;
import behaviorFramework.arbiters.HighestPriority;
import Sample.behaviors.Wander;
import framework.SimulationBody;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Interval;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Ant extends Vehicle {
    // Echo parameters -- will be extended over time
    String id;
    String tag; // this is the Echo tag variable described by Holland
    String offenseTag;
    String defenseTag;
    String matingTag;
    String interActionTag;
    String tradingTag;
    String parent = "NONE";

    // Parameters added from Smith and Bedau Echo-world implementation
    double randomDeathProb = .0001; // prob of death and taxes
    double probTax = .0001;

    // Conditions: these must match another agent's interAction tag to occur.  Right now these will
    // be of length 1 -- but can be of arbitrary length.
    String tradeCondition = "";
    String combatCondition = "";
    String matingCondition = "";

    // Possibly useful metrics to highlight a genome's combat and trade capabilities during its lifetime in the system
    int numTrades = 0;
    int numCombats = 0;
    int numReproductions = 0;
    boolean fight = false;
    boolean trade = false;
    boolean reproduce = false;
    String predator = "NONE"; // empty string if ant dies naturally or through taxation, otherwise, set by the combat outcome
    String tradePartners = "NONE"; // add partners here

    // Genomic length, how long can each tag be -- this is just one more parameter that can affect system behavior
    // Our original work allowed each tag to be up to 6 characters in length.  Change this to match
    // whichever starting length you have selected as main... note:  future should add this as a parameter
    // that gets updated.
    int genomeLength = 1;

    // Parameter for seeing how long the ant lived, they can recharge at home.  More successful ants should live longer
    // Generation = timestep it was introduced into the population
    // Death generation - if we desire it - can be calculate as generation + life when dead
    private int life = 0;
    private int generation = 0;
    private int death = 0;

    ArrayList<Resource> reservoir = new ArrayList<>();

    // UBF assist, I can also envision this helping with Echo behaviors
    private State state = new EchoAgentState();
    private behaviorFramework.Action action = new behaviorFramework.Action();
    CompositeBehavior behaviorTree;

    // Extra variables to track the ants
    private boolean alive = true;
    private int lifespan = 3000;
    private Vector2 home; // where the ant's home is

    // Actual ant "body" for the world to paint
    private Vector2 leftWheelLocation = new Vector2(-0.5, -0.5); // holdovers from vehicle class, right now
    private Vector2 rightWheelLocation = new Vector2( 0.5, -0.5); // I don't want to rehash the physics to turn the ant
    private double GRAB_RANGE = 1; // how far "off" the target can be, allows us to home in on a target
    private final int SENSOR_RANGE = 3; // how far the line casts go

    private World<SimulationBody> myWorld;

    // All taken from the cue class template
    final double ballRadius = 0.25; //0.1; //0.028575;
    final double ballDensity = 217.97925;
    final double ballFriction = 0.00;
    final double ballRestitution = 0.9;


    public Ant() {
    }

    public Ant(World<SimulationBody> myWorld) {
        this.myWorld = myWorld;
        //this.setColor(Color.CYAN);
        setRandomColor();
        this.addFixture(Geometry.createCircle(ballRadius), ballDensity, ballFriction, ballRestitution);
        this.setRandomVelocity(); // set a random initial velocity
        this.setLinearDamping(0.3);
        this.setAngularDamping(0.8);
        this.setMass(MassType.NORMAL);

        int max = 15;
        int min = -15;
        double x_pos = Math.floor(Math.random()*(max-min+1)+min); // done just for code clarity
        double y_pos = Math.floor(Math.random()*(max-min+1)+min);
        this.translate(x_pos,y_pos);

        home = new Vector2(x_pos,y_pos); // center of the screen for now.

        // Instantiate behaviorTree
        ArrayList<Double> weights = new ArrayList<>();
        setWeights(weights);
        ArbitrationUnit arbiter = new HighestPriority(weights);
        behaviorTree = new CompositeBehavior();
        behaviorTree.setArbitrationUnit(arbiter);
        //behaviorTree.add(new AvoidObstacle());
        behaviorTree.add(new Wander());
        behaviorTree.add(new GotoXX("Home"));
        behaviorTree.add(new GotoXX("Resource"));
        setInitialTag(); // set the ant's tag
        id = interActionTag+matingTag+offenseTag+defenseTag+tradingTag+combatCondition+tradeCondition+matingCondition; // combine all the tags eventually
        tag = interActionTag+matingTag+offenseTag+defenseTag+tradingTag+combatCondition+tradeCondition+matingCondition;
    }

    public Ant(World<SimulationBody> world, String[] newTag1) {
        this.myWorld = world;
        //this.setColor(Color.CYAN);
        setRandomColor();
        this.addFixture(Geometry.createCircle(ballRadius), ballDensity, ballFriction, ballRestitution);
        this.setRandomVelocity(); // set a random initial velocity
        this.setLinearDamping(0.3);
        this.setAngularDamping(0.8);
        this.setMass(MassType.NORMAL);

        int max = 15;
        int min = -15;
        double x_pos = Math.floor(Math.random()*(max-min+1)+min); // done just for code clarity
        double y_pos = Math.floor(Math.random()*(max-min+1)+min);
        //this.translate(x_pos,y_pos);

        home = new Vector2(x_pos,y_pos); // center of the screen for now.

        // Instantiate behaviorTree
        ArrayList<Double> weights = new ArrayList<>();
        setWeights(weights);
        ArbitrationUnit arbiter = new HighestPriority(weights);
        behaviorTree = new CompositeBehavior();
        behaviorTree.setArbitrationUnit(arbiter);

        behaviorTree.add(new Wander());
        behaviorTree.add(new GotoXX("Home"));
        behaviorTree.add(new GotoXX("Resource"));

        // ********* behavior wander testing with following the light ********* //
        behaviorTree.add(new GotoXX("Light"));

        // Tags are done special with this constructor
        interActionTag = newTag1[0];
        matingTag = newTag1[1];
        offenseTag = newTag1[2];
        defenseTag = newTag1[3];
        tradingTag = newTag1[4];
        combatCondition = newTag1[5];
        tradeCondition = newTag1[6];
        matingCondition = newTag1[7];

        id = interActionTag+matingTag+offenseTag+defenseTag+tradingTag+combatCondition+tradeCondition+matingCondition; // combine all the tags eventually
        tag = interActionTag+matingTag+offenseTag+defenseTag+tradingTag+combatCondition+tradeCondition+matingCondition;
    }

    /**
     * Set the weights for each action:  wander, goHome, GotoXX
     * @param weights
     */
    private void setWeights(ArrayList<Double> weights) {
        weights.add(0,.9);
        weights.add(1,1.2);
        weights.add(2, 1.1);
        weights.add(2, 1.1);
    }

    /**
     * Good old copy constructor
     * @param copy
     */
    public Ant(Ant copy) {
        this.myWorld = copy.myWorld;
        this.id = copy.id;
        this.home = copy.home;
        this.state = copy.state;
        this.action = copy.action;
        this.behaviorTree = copy.behaviorTree;
        this.alive = copy.alive;
        this.lifespan = copy.lifespan;
        this.tag = copy.tag;
        this.offenseTag = copy.offenseTag;
        this.defenseTag = copy.defenseTag;
        this.matingTag = copy.matingTag;
        this.interActionTag = copy.interActionTag;
        this.reservoir = copy.reservoir;
        this.generation = copy.generation;
        this.life = copy.life;
        this.death = copy.death;
        this.color = copy.color;
        this.tradingTag = copy.tradingTag;
        this.numCombats = copy.numCombats;
        this.numTrades = copy.numTrades;
        this.numReproductions = copy.numReproductions;
        this.tradeCondition = copy.tradeCondition;
        this.combatCondition = copy.combatCondition;
        this.matingCondition = copy.matingCondition;
        this.predator = copy.predator;
        this.tradePartners = copy.tradePartners;
    }

    public boolean sense() {
        state.setHeading(convertTransformToHeading());
        state.setVelocity(this.getLinearVelocity()); // LinearVelocity captures heading and speed
        state.setAngularVelocity(this.getAngularVelocity());
        state.updateLightStrengths();
        state.tick();
        return true;
    }

    /**
     * Must be overloaded.
     * Called before render to show action in the current time step.
     * Current action is no op
     */
    public Action decideAction() {
        action.clear();
        action = behaviorTree.genAction(state); // get an action from the behavior tree
        //System.out.println(action.name);
        return action;
    }


    /**
     *  Applies the action to the vehicle. Calculates the torque and force from the left and right wheel velocity
     *  requests and then clamps them into a performance range.
     *
     * @param a Action being applied to the vehicle
     */
    public void act(Action a) {
        double left = a.getLeftWheelVelocity();
        double right = a.getRightWheelVelocity();

        state.setLeftWheelVelocity(left);
        state.setRightWheelVelocity(right);

        // Calculate Torque
        Vector2 applyLeft = new Vector2(0.0, 1.0);
        applyLeft.multiply(left);
        Vector2 applyRight = new Vector2(0.0,1.0);
        applyRight.multiply(right);
        double torqueL = leftWheelLocation.cross(applyLeft);
        double torqueR = rightWheelLocation.cross(applyRight);
        double baseTorque = torqueL + torqueR;

        // Proportional Controller
        double error = baseTorque - this.getAngularVelocity(); // SetPoint - ProcessVariable (e(t) = r(t)-y(t))
        double u = K_p * error; // Control variable

        // Apply Torque
        this.applyTorque(u);

        // System.out.println("Current: " + baseVehicle.getAngularVelocity() + "; Applied: " + u + "; Target: " + baseTorque);

        // Apply the forces in the direction the baseVehicle is facing
        Vector2 baseNormal = this.getTransform().getTransformedR(new Vector2(0.0,1.0));
        baseNormal.multiply(left+right);
        this.setLinearVelocity(baseNormal);

        // Constrain the vehicle to prevent spinning out of control
        // Get the linear velocity in the direction of the baseVehicle's front
        Vector2 clamp = this.getTransform().getTransformedR(new Vector2(0.0, 1.0));
        double defl = this.getLinearVelocity().dot(clamp);

        // clamp the velocity
        defl = Interval.clamp(defl, -MAX_VELOCITY, MAX_VELOCITY);
        if (defl < 0.0)
            this.setLinearVelocity(baseNormal.multiply(-defl));
        else
            this.setLinearVelocity(baseNormal.multiply(defl));

        // clamp the angular velocity
        double av = this.getAngularVelocity();
        av = Interval.clamp(av, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);
        this.setAngularVelocity(av);
    }

    /**
     * Assigns randoms tag to the ant -- tag length is in the range [1,6],
     * with potential values: A, B, C, D.
     *
     * Todo: later we can add wildcards to the tags, but this would necessitate changing interaction comparisons
     */
    private void setInitialTag() {
        this.matingTag = ""; // for now, the ants are asexual
        this.offenseTag = "";
        this.defenseTag = "";
        this.interActionTag = "";
        this.tradingTag = "";
        this.matingCondition = "";
        this.tradeCondition = "";
        this.combatCondition = "";

        Random rand = new Random();
        this.matingTag += buildTag(rand.nextInt(genomeLength)+1);
        this.offenseTag += buildTag(rand.nextInt(genomeLength)+1);
        this.defenseTag += buildTag(rand.nextInt(genomeLength)+1);
        this.interActionTag += buildTag(rand.nextInt(genomeLength)+1);
        this.tradingTag += buildTag(1); // only trades one commodity
        this.matingCondition += buildTag(1);
        this.tradeCondition += buildTag(1);
        this.combatCondition += buildTag(1);
    }

    private String buildTag(int length) {
        String t = "";
        ArrayList<Character> characters = new ArrayList<>();
        characters.add('A');
        characters.add('B');
        characters.add('C');
        characters.add('D');
        for(int i = 0; i < length; i++) {
            t += characters.get(new Random().nextInt(4));
        }
        return t;
    }

    private String mutateTag(String tag, double mutationRate) {
        String t = "";
        ArrayList<Character> characters = new ArrayList<>();
        characters.add('A');
        characters.add('B');
        characters.add('C');
        characters.add('D');
        for(int i = 0; i < tag.length(); i++) {
            double prob = new Random().nextDouble();
            if(prob <= mutationRate) {
                //System.out.println("mutation occurred");
                t += characters.get(new Random().nextInt(4));
                //setRandomColor(); // it's no longer the same species, this may change back to stay the same for observing
                // lineages.
            }
            else {
                t += tag.charAt(i);
            }
        }
        return t;
    }

    /**
     * Determines if the ant can replicate -- based on resources in the reservoir and its tag.  Items in its tag
     * must each be present in the reservoir for it to replicate.  For this iteraion of ants, this is asexual reproduction.
     *
     * @param burn -- if set to True, will remove resources immediately if the agent can replicate, otherwise,
     *             the function will still say if the agent can replicate, just not burn the resources.
     * @return
     */
    public boolean replicate(boolean burn) {
        // Doing this the naive way first
        int countA = 0;
        int countB = 0;
        int countC = 0;
        int countD = 0;

        // Count occurrences in the ant's tag, remember, this will later be fairly unique to sets of ants
        //long tagCountA = this.matingTag.chars().filter(ch -> ch == 'A').count();
        //long tagCountB = this.matingTag.chars().filter(ch -> ch == 'B').count();
        //long tagCountC = this.matingTag.chars().filter(ch -> ch == 'C').count();
        //long tagCountD = this.matingTag.chars().filter(ch -> ch == 'D').count();

        long tagCountA = this.tag.chars().filter(ch -> ch == 'A').count();
        long tagCountB = this.tag.chars().filter(ch -> ch == 'B').count();
        long tagCountC = this.tag.chars().filter(ch -> ch == 'C').count();
        long tagCountD = this.tag.chars().filter(ch -> ch == 'D').count();

        ArrayList<Resource> toBurn = new ArrayList<Resource>(); // save resources to burn here
        for(Resource res: reservoir) {
            if(res.type.equals("A") && countA < tagCountA) {
                countA++;
                toBurn.add(res);
            }
            if(res.type.equals("B")  && countB < tagCountB) {
                countB++;
                toBurn.add(res);
            }
            if(res.type.equals("C")  && countC < tagCountC) {
                countC++;
                toBurn.add(res);
            }
            if(res.type.equals("D")  && countD < tagCountD) {
                countD++;
                toBurn.add(res);
            }
        }
        // Now we can see if the ant can replicate
        if(countA >= tagCountA && countB >= tagCountB && countC >= tagCountC && countD >= tagCountD) {
            // Agent can replicate -- burn the resources
            if(burn) {
                //System.out.println("Replication try -- res before: " + this.reservoir.size() + " tag + length: "
                //        + tag + " " + tag.length());
                for (Resource remove : toBurn) {
                    reservoir.remove(remove);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Decision-making process for an ant - look around, do I see food? do I see another ant? etc.
     * This is called by the AntWorld simulation loop, so if you want to add functionality, this
     * is the spot.
     *
     * @param resources
     */
    public void decide(ArrayList<Ant> allAgent_Ants, ArrayList<Resource> resources) {
        action.clear();
        deathAndTaxes();
        if(this.alive) { // as we have introduced interaction, dead ants could get processed inside the game loop
            // Step 1:  Sense
            //life++;
            updateLife();
            updateHome();
            updateResources(resources);
            updateAnts(allAgent_Ants);
            // Step 2:  Decide
            decideAction();
        }
        // Step 3:  Act
        act(action);
    }


    /**
     * Random death and tax function - both are inevitable.  Controlled by randomDeathProb and probTax set at
     * the top of the class.
     */
    private void deathAndTaxes() {
        Random rand = new Random();
        if (rand.nextDouble() < randomDeathProb) {
            this.alive = false;
            //System.out.println("Ant mysteriously died.");
        }
        else if(rand.nextDouble() < probTax) {
            // ant must pay the tax -- according to the paper, this is one of each resource.
            // if it cannot, it dies.  Now there's is more severe, as in, it must also be able
            // to replicate... to do that, I would have to call this before going through the breeding process?
            if(!replicate(false)) { // if it cannot replicate, it dies.
                this.alive = false;
                //System.out.println("Could not replicate before the tax man came.");
            }
            else { // Remove one of each resource from the reservoir -- this is the tax
                ArrayList<Resource> resRemoval = new ArrayList<>();
                // very hacked
                boolean a = false;
                boolean b = false;
                boolean c = false;
                boolean d = false;
                for(Resource res : reservoir) {
                    if(!a && res.type.equals("A")) {
                        resRemoval.add(res);
                        a = true;
                    }
                    if(!b && res.type.equals("B")) {
                        resRemoval.add(res);
                        b = true;
                    }
                    if(!c && res.type.equals("B")) {
                        resRemoval.add(res);
                        c = true;
                    }
                    if(!d && res.type.equals("B")) {
                        resRemoval.add(res);
                        d = true;
                    }
                }
                for(Resource res : resRemoval) {
                    reservoir.remove(res); // remove what we can from the ant. Smith and Bedau did not say
                    // they killed an ant that couldn't pay the full tax, just if it couldn't replicate.
                }
            }
        }
    }

    /**
     * Mutates the genome of an agent with some probability per space
     * @param mutationRate
     */
    public void mutate(double mutationRate) {
        interActionTag = mutateTag(interActionTag,mutationRate);
        matingTag = mutateTag(matingTag,mutationRate);
        offenseTag = mutateTag(offenseTag,mutationRate);
        defenseTag = mutateTag(defenseTag,mutationRate);
        tradingTag = mutateTag(tradingTag,mutationRate);
        combatCondition = mutateTag(combatCondition,mutationRate);
        tradeCondition = mutateTag(tradeCondition,mutationRate);
        matingCondition = mutateTag(matingCondition,mutationRate);
        tag = interActionTag+matingTag+offenseTag+defenseTag+tradingTag+combatCondition+tradeCondition+matingCondition;
    }

    /**
     * Determines if the ant is "home." If so, it's lifespan is extended. -- This will change in the future.
     */
    private void updateHome() {
        double dist = this.getWorldCenter().distance(home);
        if(dist < 2) {
            //System.out.println("Dist from home: " + dist + " WC: " + this.basicAnt.getWorldCenter());
            lifespan = 3000;
            setRandomVelocity(); // send it back into the world
        }
    }

    /**
     * Establishes if the ant has to go home due to a low lifespan (low energy).  Later this will probably extend
     * to dropping off resources at home for other ants to use.
     */
    private void updateLife() {
        if(lifespan <= 100*this.getWorldCenter().distance(home)) {
            Vector2 heading = new Vector2(this.getWorldCenter(), home);
            double angle = heading.getAngleBetween(this.getLinearVelocity()); // radians
            double distance = this.getWorldCenter().distance(home); //
            SensedObject obj = new SensedObject(heading, angle, distance, "Home", "None", home);
            state.addSensedObject(obj);
        }
    }

    /**
     * Adds sensed ants to the sensed object list
     *
     *  @param allAgent_ants
     *
     */
    private void updateAnts(ArrayList<Ant> allAgent_ants) {
        for(Ant antObj : allAgent_ants) {
            Vector2 heading = new Vector2(this.getWorldCenter(), antObj.getWorldCenter());
            double angle = heading.getAngleBetween(this.getLinearVelocity()); // radians
            double distance = this.getWorldCenter().distance(antObj.getWorldCenter()); //
            if(distance <= SENSOR_RANGE && this.id != antObj.id) { // ignore oneself
                String type = "Ant";
                SensedObject obj = new SensedObject(heading, angle, distance, type, "None", antObj.getWorldCenter());
                state.addSensedObject(obj);
                //System.out.println("I see an ant: " + antObj.id);

                // Ok, ok, we see an ant.  Let's see what interactions we get for right now
                EchoAntCatFly model = new EchoAntCatFly();

                if (model.canInteract(this.combatCondition, antObj.interActionTag)) { // if true, combat can occur
                    double maybeIWin = model.likelyWinner(this.offenseTag, antObj.defenseTag);
                    double maybeYouWin = model.likelyWinner(antObj.offenseTag, this.defenseTag);

                    // According to the Smith and Bedau paper, the likelihood of fleeing is equal to the probability
                    // of the agent losing a fight to another agent.  If it is the agent's turn, which it should be,
                    // and the agent decides to flee, then we will need to give the agent an opportunity to move away
                    // from the enemy.  This should also override anything else the agent wants to do.  For now, we will handle
                    // this via the UBF structure again.  We will only add ants that we want to flee from.  Otherwise, if it
                    // is the ants turn it should eat the other ant.  This will set the other ant to dead and we will transfer
                    // resources. -- todo:  look at hidden order as I think this is proportional may also just follow S & B paper
                    if (maybeIWin > maybeYouWin) {
                       // System.out.println("Likely to win the fight");
                        double prob = new Random().nextDouble();
                        if (prob <= maybeIWin) {
                            antObj.alive = false; // the other ant is killed
                            System.out.println("Predation event (Type 1): " + this.tag + " ate " + antObj.tag);
                            antObj.predator = this.tag; // predation event
                            cleanUp(antObj); // take the ant's resources
                           // System.out.println("Momma! I just killed an ant...");
                        }
                        numCombats++;
                        fight = true;
                    } else if (maybeIWin != maybeYouWin) {
                        //System.out.println("You better run!");  // todo this should probably result in a fleeing action, GOTOXX anywhere but here
                        double prob = new Random().nextDouble();
                        if (prob <= maybeYouWin) { // other ant succeeds in killing us
                            this.alive = false;
                            this.predator = antObj.tag; // predation event
                            System.out.println("Predation event (Type 2):" + antObj.tag + " ate " + this.tag);
                            antObj.cleanUp(this);
                            //System.out.println("Failed to flee.");
                        }
                        numCombats++;
                        fight = true;
                    }
                } // end combat check
                // Trading check -- two way condition must be met!
                else if(model.canInteract(this.tradeCondition, antObj.interActionTag) &&
                model.canInteract(antObj.tradeCondition, this.interActionTag) && this.alive) { // ant can give commodity to another
                    trade = true;
                    numTrades++;
                    addTradeTag(antObj.tag);
                    antObj.addTradeTag(this.tag);
                    Resource temp = null;
                    for(Resource r : this.reservoir) {
                        if(r.type.equals(this.tradingTag)) {
                            temp = r;
                        }
                    }
                    if(temp != null) {
                        // Note:  this is VERY dumb atm as it's only checking if it has a resource, not if it needs it
                        // and wants to hold onto it
                        //System.out.println("I am trading resource: " + temp.type);
                        reservoir.remove(temp); // remove from this reservoir
                        antObj.reservoir.add(temp); // transfer to other reservoir
                    }
                    // Now trade the other way
                    temp = null;
                    for(Resource r : antObj.reservoir) {
                        if(r.type.equals(antObj.tradingTag)) {
                            temp = r;
                        }
                    }
                    if(temp != null) {
                        // Note:  this is VERY dumb atm as it's only checking if it has a resource, not if it needs it
                        // and wants to hold onto it
                        //System.out.println("I am receiving resource: " + temp.type);
                        this.reservoir.add(temp); // remove from this reservoir
                        antObj.reservoir.remove(temp); // transfer to other reservoir
                    }
                }
                // Todo:  mating check goes here, also two way condition -- according to S & B, if a trade occurs, mating will not
                /** -- it's causing concurrency issues with dyn4j right now.  We may have to do mating in it's
                 * own loop in the game loop.
                 */
                /*
                else if(model.canInteract(this.matingCondition, antObj.interActionTag) &&
                        model.canInteract(antObj.matingCondition, this.interActionTag)) {
                    // Agents can mate -- pass pairings along via an arraylist to the simulation / game loop
                    //System.out.println("Mating possible");
                    Ant[] pair = new Ant[2];
                    pair[0] = this;
                    pair[1] = antObj;
                    matingPairs.add(pair);
                    this.alive = false;
                    antObj.alive = false;
                }
                */

            } // end if within sensor range
        }
    }

    private void addTradeTag(String id) {
        if(this.tradePartners.equals("NONE")) {
            this.tradePartners = id;
        }
        else {
            this.tradePartners += "_" + id; // allow us to break this apart later
        }
    }

    /**
     * Quick method to reset these booleans each time step
     */
    public void reset() {
        trade = false;
        reproduce = false;
        fight = false;
    }

    private void cleanUp(Ant otherAnt) {
        for(Resource res : otherAnt.reservoir) {
            this.reservoir.add(res);
        }
        otherAnt.reservoir = new ArrayList<>(); // empty the reservoir of the other ant
    }

    /**
     * Adds sensed resources to the sensed object list
     * @param resources
     */
    private void updateResources(ArrayList<Resource> resources) {
        double distance = Double.POSITIVE_INFINITY;
        SensedObject obj = new SensedObject(new Vector2(0,0), 0, 0, "empty", "empty", new Vector2(0,0));
        Resource found = new Resource();

        // Find the closest resource and head towards it
        for(Resource resObj : resources) {
            Vector2 myPos = this.getWorldCenter();
            Vector2 resPos = resObj.location;

            double tDist = Math.sqrt(Math.pow(myPos.x - resPos.x,2) +
                    Math.pow(myPos.y - resPos.y,2));

            Vector2 heading = new Vector2(this.getWorldCenter(), resObj.location);
            double angle = heading.getAngleBetween(this.getLinearVelocity()); // radians

            if(tDist < distance && tDist <= SENSOR_RANGE) {
                //System.out.println("Resource located at: " + resObj.location + " " + tDist);
                String type = "Resource";
                distance = tDist;
                obj = new SensedObject(heading, angle, distance, type, "Resource", resObj.location);
                found = resObj;
            }
        }
        if(!obj.getType().equals("empty")) {
            // Before we add this object, we have to see if it's within "grabbing" range
            if(obj.getDistance() < GRAB_RANGE) {
                // Add to the ant's reservoir
                reservoir.add(new Resource(found));
                resources.remove(found); // remove from the resource array
            }
            else
                state.addSensedObject(obj);
        }
    }

    /**
     * Sets the linear velocity to a random vector
     */
    private void setRandomVelocity() {
        Random rand = new Random();
        int max = 2;
        int min = -2;
        this.setLinearVelocity(rand.nextInt((max - min) + 1) + min,rand.nextInt((max - min) + 1) + min);
    }

    private void setRandomColor() {
        Random rand = new Random();
        Color c = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
        this.setColor(c);
    }

    public void render(Graphics2D g, double scale) {
        super.render(g, scale);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    public void setHome(Vector2 home) {
        this.home = home;
    }


    /**
     * Decrements the agent's lifespan by one.
     */
    public void decLife() {
        this.lifespan--;
        if(this.lifespan <= 0) {
            this.alive = false;
        }
    }

    public void setParent(String par) {
        parent = par;
    }

    public void incLife() {
        this.lifespan++;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getLife() {
        return life;
    }

    public int getDeath() {
        return death;
    }

    public void setDeath(int death) {
        this.death = death;
    }

    /**
     * Equals
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ant ant = (Ant) o;
        return Objects.equals(id, ant.id);
    }

    /**
     * Hash code
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Vector2 getHome() {
        return home;
    }

    public void setPosition(Vector2 localCenter) {
        this.translate(localCenter.x,localCenter.y);
    }

}

