package Ants;

import Ants.Behaviors.GoHome;
import Ants.Behaviors.GotoResource;
import Braitenburg.Action;
import Braitenburg.SensedObject;
import Braitenburg.State;
import Sample.behaviors.AvoidObstacle;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.CompositeBehavior;
import behaviorFramework.arbiters.HighestActivation;
import behaviorFramework.arbiters.HighestPriority;
import behaviorFramework.behaviors.Wander;
import Sample.behaviors.GotoXX;
import com.sun.deploy.util.StringUtils;
import framework.SimulationBody;
import org.dyn4j.geometry.*;
import org.dyn4j.world.World;
import java.awt.*;
import java.util.*;

public class Ant extends SimulationBody {
    // Echo parameters -- will be extended over time
    String id;
    String tag; // this is the Echo tag variable described by Holland
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
    private SimulationBody basicAnt;
    private Vector2 leftWheelLocation = new Vector2(-0.5, -0.5); // holdovers from vehicle class, right now
    private Vector2 rightWheelLocation = new Vector2( 0.5, -0.5); // I don't want to rehash the physics to turn the ant

    // All taken from the cue basicAnt class template
    // 2.25 in diameter = 0.028575 m radius
    final double ballRadius = 0.25; //0.1; //0.028575;
    // 0.126 oz/in^3 = 217.97925 kg/m^3
    final double ballDensity = 217.97925;
    final double ballFriction = 0.00;
    final double ballRestitution = 0.9;

    private double MAX_VELOCITY = 1; // arbitrary right now
    private int SENSOR_RANGE = 10; // how far an ant can see
    private int MAX_TORQUE = 1; // how fast we can turn
    private double GRAB_RANGE = 1; // how far "off" the target can be, allows us to home in on a target

    // World the ant lives in
    World<SimulationBody> myWorld;

    public Ant(World<SimulationBody> world) {
        myWorld = world;
        basicAnt = new SimulationBody(new Color(255, 255, 255)); // leaving this in, ideally i'd like child ants to be a combination of parent colors
        basicAnt.setColor(Color.CYAN);
        basicAnt.addFixture(Geometry.createCircle(ballRadius), ballDensity, ballFriction, ballRestitution);
        basicAnt.translate(-0.25, 0.0);
        this.setRandomVelocity(); // set a random initial velocity
        basicAnt.setLinearDamping(0.3);
        basicAnt.setAngularDamping(0.8);
        basicAnt.setMass(MassType.NORMAL);
        myWorld.addBody(basicAnt);

        int max = 15;
        int min = -15;
        double x_pos = Math.floor(Math.random()*(max-min+1)+min); // done just for code clarity
        double y_pos = Math.floor(Math.random()*(max-min+1)+min);
        basicAnt.translate(x_pos,y_pos);
        id = new String(basicAnt.getWorldCenter().toString()); // hopefully this makes their names random enough for now

        home = new Vector2(-5,5); // center of the screen for now.

        // Instantiate behaviorTree
        ArbitrationUnit arbiter = new HighestPriority();
        behaviorTree = new CompositeBehavior();
        behaviorTree.setArbitrationUnit(arbiter);
        behaviorTree.add(new GoHome()); // <-- parameter to get home before death needs to be tuned
        behaviorTree.add(new GotoXX("Resource"));
        behaviorTree.add(new AvoidObstacle());
        behaviorTree.add(new Wander());

        setInitialTag(); // set the ant's tag
    }

    /**
     * Good old copy constructor
     * @param copy
     */
    public Ant(Ant copy) {
        this.myWorld = copy.myWorld;
        this.basicAnt = copy.basicAnt;
        this.id = copy.id;
        this.home = copy.home;
        this.state = copy.state;
        this.action = copy.action;
        this.behaviorTree = copy.behaviorTree;
        this.alive = copy.alive;
        this.lifespan = copy.lifespan;
        this.myWorld.addBody(this.basicAnt);
        this.tag = copy.tag;
        this.reservoir = copy.reservoir;
    }

    /**
     *
     */
    public boolean sense() {
        state.tick();
        state.setVelocity(basicAnt.getLinearVelocity()); // LinearVelocity captures heading and speed
        return true;
    }

    /**
     * Must be overloaded.
     * Called before render to show action in the current time step.
     * Current action is no op
     */
    public Action decideAction() {
        action.clear();
        // Get an action from the behaviorTree
        action = behaviorTree.genAction(state);
        System.out.println(action.name + " " + action.getLeftWheelVelocity() + " " + action.getRightWheelVelocity());
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

//        System.out.println("Left Torque: " + torqueL + " Right Torque: " + torqueR);

        // Apply torque to basicAnt
        double baseTorque = torqueL + torqueR;
        basicAnt.applyTorque(baseTorque);

        // Constrain the vehicle to prevent spinning out of control
        // Apply the forces in the direction the basicAnt is facing
        Vector2 baseNormal = basicAnt.getTransform().getTransformedR(new Vector2(0.0,1.0));
        baseNormal.multiply(left+right);
//        System.out.println(baseNormal);
        basicAnt.setLinearVelocity(baseNormal);

        // Get the linear velocity in the direction of the basicAnt's front
        Vector2 clamp = this.basicAnt.getTransform().getTransformedR(new Vector2(0.0, 1.0));
        double defl = basicAnt.getLinearVelocity().dot(clamp);

        // clamp the velocity
        defl = Interval.clamp(defl, 0.0, MAX_VELOCITY);
        basicAnt.setLinearVelocity(baseNormal.multiply(defl));

        // clamp the angular velocity
        double av = basicAnt.getAngularVelocity();
        av = Interval.clamp(av, -MAX_TORQUE, MAX_TORQUE);
        basicAnt.setAngularVelocity(av);
    }

    /**
     * Set the color of the vehicle
     *
     * @param c Color
     */
    public void setColor(Color c) {
        basicAnt.setColor(c);
    }

    /**
     * Assigns a random tag to the ant -- right now, it is very, very non-random
     */
    private void setInitialTag() {
        this.tag = "AABBCC";
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
        long tagCountA = this.tag.chars().filter(ch -> ch == 'A').count();
        long tagCountB = this.tag.chars().filter(ch -> ch == 'B').count();
        long tagCountC = this.tag.chars().filter(ch -> ch == 'C').count();
        long tagCountD = this.tag.chars().filter(ch -> ch == 'D').count();

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
        // Step 1:  Sense
        updateLife();
        updateResources(resources);
        updateAnts(allAgent_Ants);
        // Step 2:  Decide
        decideAction();
        // Step 3:  Act
        act(action);
    }

    private void updateLife() {
        if(lifespan <= 100*basicAnt.getWorldCenter().distance(home)) {
            Vector2 heading = new Vector2(basicAnt.getWorldCenter(), home);
            double angle = heading.getAngleBetween(basicAnt.getLinearVelocity()); // radians
            double distance = basicAnt.getWorldCenter().distance(home); //
            SensedObject obj = new SensedObject(heading, angle, distance, "Home", "None", home);
            state.addSensedObject(obj);
        }
    }

    /**
     * Adds sensed ants to the sensed object list
     * @param allAgent_ants
     */
    private void updateAnts(ArrayList<Ant> allAgent_ants) {
        for(Ant antObj : allAgent_ants) {
            Vector2 heading = new Vector2(basicAnt.getWorldCenter(), antObj.basicAnt.getWorldCenter());
            double angle = heading.getAngleBetween(basicAnt.getLinearVelocity()); // radians
            double distance = basicAnt.getWorldCenter().distance(antObj.basicAnt.getWorldCenter()); //
            if(distance <= SENSOR_RANGE && this.id != antObj.id) { // ignore oneself
                String type = "Ant";
                SensedObject obj = new SensedObject(heading, angle, distance, type, "None", antObj.basicAnt.getWorldCenter());
                state.addSensedObject(obj);
            }
        }
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
            Vector2 heading = new Vector2(basicAnt.getWorldCenter(), resObj.location);
            double angle = heading.getAngleBetween(basicAnt.getLinearVelocity()); // radians
            double tDist = basicAnt.getWorldCenter().distance(resObj.location); //
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
     * Sets the basicAnts linear velocity to a random vector
     */
    private void setRandomVelocity() {
        Random rand = new Random();
        int max = 2;
        int min = -2;
        basicAnt.setLinearVelocity(rand.nextInt((max - min) + 1) + min,rand.nextInt((max - min) + 1) + min);
    }

    public void render(Graphics2D g, double scale) {
        super.render(g, scale, Color.CYAN);
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

    /**
     * Decrements the agent's lifespan by one.
     */
    public void decLife() {
        this.lifespan--;
        if(this.lifespan <= 0) {
            this.alive = false;
        }
    }

    public void incLife() {
        this.lifespan++;
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

