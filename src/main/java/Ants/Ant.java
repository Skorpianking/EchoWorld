package Ants;

import Vehicles.Action;
import Vehicles.SensedObject;
import Vehicles.State;
import Sample.behaviors.GotoXX;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.CompositeBehavior;
import behaviorFramework.arbiters.HighestPriority;
import Sample.behaviors.Wander;
import framework.SimulationBody;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Ant extends SimulationBody {
    // Echo parameters -- will be extended over time
    String id;
    String tag; // this is the Echo tag variable described by Holland
    String offenseTag;
    String defenseTag;
    String matingTag;
    String parent = "NONE";

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
    private int lifespan = 2000;
    private Vector2 home; // where the ant's home is

    // Actual ant "body" for the world to paint
    private Vector2 leftWheelLocation = new Vector2(-0.5, -0.5); // holdovers from vehicle class, right now
    private Vector2 rightWheelLocation = new Vector2( 0.5, -0.5); // I don't want to rehash the physics to turn the ant

    // All taken from the cue class template
    // 2.25 in diameter = 0.028575 m radius
    final double ballRadius = 0.25; //0.1; //0.028575;
    // 0.126 oz/in^3 = 217.97925 kg/m^3
    final double ballDensity = 217.97925;
    final double ballFriction = 0.00;
    final double ballRestitution = 0.9;

    // Straight from vehicle
    private double MAX_VELOCITY = 2; // arbitrary right now
    private final double MAX_ANGULAR_VELOCITY = 1; // how fast we can turn
    private final double ANGULAR_DAMPENING = 0.1;
    private final double K_p = 10;   //Proportional Control Constant

    private int SENSOR_RANGE = 2; // how far an ant can see
    private int MAX_TORQUE = 1; // how fast we can turn
    private double GRAB_RANGE = 1; // how far "off" the target can be, allows us to home in on a target

    // World the ant lives in
    protected World<SimulationBody> myWorld;

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
        behaviorTree.add(new Wander());
        //behaviorTree.add(new GoHome()); // <-- parameter to get home before death needs to be tuned
        behaviorTree.add(new GotoXX("Home"));
        behaviorTree.add(new GotoXX("Resource"));
        setInitialTag(); // set the ant's tag
        id = matingTag+offenseTag+defenseTag; // combine all the tags eventually
        tag = matingTag+offenseTag+defenseTag;
    }

    /**
     * Set the weights for each action:  wander, goHome, GotoXX
     * @param weights
     */
    private void setWeights(ArrayList<Double> weights) {
        weights.add(0,1.0);
        weights.add(1,3.0);
        weights.add(2, 2.0);
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
        this.reservoir = copy.reservoir;
        this.generation = copy.generation;
        this.life = copy.life;
        this.death = copy.death;
        this.color = copy.color;
    }

    public boolean sense() {
        state.tick();
        state.setVelocity(this.getLinearVelocity()); // LinearVelocity captures heading and speed
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

    }

    /**
     * Assigns a random tag to the ant -- later we can add a tag length, for now they will be of length six,
     * with potential values: A, B, C, D
     */
    private void setInitialTag() {
        //this.tag = "";
        this.matingTag = ""; // for now, the ants are asexual
        this.offenseTag = "";
        this.defenseTag = "";
        ArrayList<Character> characters = new ArrayList<>();
        characters.add('A');
        characters.add('B');
        characters.add('C');
        characters.add('D');

        for(int i = 0; i < 6; i++) {
            this.matingTag += characters.get(new Random().nextInt(4));
            this.offenseTag += characters.get(new Random().nextInt(4));
            this.defenseTag += characters.get(new Random().nextInt(4));
        }
        //System.out.println("Hello! My name is: " + this.tag);
    }

    /**
     * Determines if the ant can replicate -- based on resources in the reservoir and its tag.  Items in its tag
     * must each be present in the reservoir for it to replicate.  For this iteraion of ants, this is asexual reproduction.
     * @return
     */
    public boolean replicate() {
        // Doing this the naive way first
        int countA = 0;
        int countB = 0;
        int countC = 0;
        int countD = 0;

        // Count occurrences in the ant's tag, remember, this will later be fairly unique to sets of ants
        long tagCountA = this.matingTag.chars().filter(ch -> ch == 'A').count();
        long tagCountB = this.matingTag.chars().filter(ch -> ch == 'B').count();
        long tagCountC = this.matingTag.chars().filter(ch -> ch == 'C').count();
        long tagCountD = this.matingTag.chars().filter(ch -> ch == 'D').count();

        ArrayList<Resource> toBurn = new ArrayList<Resource>(); // save resources to burn here
        for(Resource res: reservoir) {
            if(res.type.equals("A")) {
                countA++;
                toBurn.add(res);
            }
            if(res.type.equals("B")) {
                countB++;
                toBurn.add(res);
            }
            if(res.type.equals("C")) {
                countC++;
                toBurn.add(res);
            }
            if(res.type.equals("D")) {
                countD++;
                toBurn.add(res);
            }
        }
        // Now we can see if the ant can replicate
        if(countA >= tagCountA && countB >= tagCountB && countC >= tagCountC && countD >= tagCountD) {
            // Agent can replicate -- burn the resources
            for(Resource remove : toBurn) {
                reservoir.remove(remove);
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
        if(this.alive) { // as we have introduced interaction, dead ants could get processed inside the game loop
            // Step 1:  Sense
            life++;
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
     * Determines if the ant is "home." If so, it's lifespan is extended. -- This will change in the future.
     */
    private void updateHome() {
        double dist = this.getWorldCenter().distance(home);
        if(dist < 2) {
            //System.out.println("Dist from home: " + dist + " WC: " + this.basicAnt.getWorldCenter());
            lifespan = 2000;
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
     * May not need to add this to the sense object list as we can have this behavior trump other actions?
     *
     * @param allAgent_ants
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
                double maybeIWin = model.likelyWinner(this.offenseTag,antObj.defenseTag);
                double maybeYouWin = model.likelyWinner(antObj.offenseTag, this.defenseTag);
                //System.out.println("Fight Values: " + maybeIWin + " " + this.offenseTag + " " +  maybeYouWin + antObj.defenseTag);
                //System.out.println("Fight Values: " + maybeIWin + " " + this.defenseTag + " " +  maybeYouWin + antObj.offenseTag);

                // According to the Smith and Bedau paper, the likelihood of fleeing is equal to the probability
                // of the agent losing a fight to another agent.  If it is the agent's turn, which it should be,
                // and the agent decides to flee, then we will need to give the agent an opportunity to move away
                // from the enemy.  This should also override anything else the agent wants to do.  For now, we will handle
                // this via the UBF structure again.  We will only add ants that we want to flee from.  Otherwise, if it
                // is the ants turn it should eat the other ant.  This will set the other ant to dead and we will transfer
                // resources. -- todo:  look at hidden order as I think this is proportional may also just follow S & B paper
                if(maybeIWin > maybeYouWin) {
                    System.out.println("Likely to win the fight");
                    double prob = new Random().nextDouble();
                    if(prob <= maybeIWin) {
                        antObj.alive = false; // the other ant is killed
                        cleanUp(antObj); // take the ant's resources
                        System.out.println("Momma! I just killed an ant...");
                    }
                }
                else if(maybeIWin != maybeYouWin) {
                    System.out.println("You better run!");  // todo this should probably result in a fleeing action, GOTOXX anywhere but here
                    double prob = new Random().nextDouble();
                    if(prob >= maybeYouWin) { // we don't succeed
                        this.alive = false;
                        antObj.cleanUp(this);
                        System.out.println("Failed to flee.");
                    }
                }
            }
        }
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
            Vector2 heading = new Vector2(this.getWorldCenter(), resObj.location);
            double angle = heading.getAngleBetween(this.getLinearVelocity()); // radians
            double tDist = this.getWorldCenter().distance(resObj.location); //
            if(tDist < distance && tDist <= SENSOR_RANGE) {
                String type = "Resource";
                distance = tDist;
                obj = new SensedObject(heading, angle, distance, type, "empty", resObj.location);
                found = resObj;
            }
        }
        if(!obj.getType().equals("empty")) {
            // Before we add this object, we have to see if it's within "grabbing" range
            if(obj.getDistance() < GRAB_RANGE) {
                // Add to the ant's reservoir
                reservoir.add(new Resource(found));
                resources.remove(found); // remove from the resource array -- unsure if this is working 100%
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
}

