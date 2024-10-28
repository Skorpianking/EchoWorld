package Boid;

import Boid.behaviors.BoidAlign;
import Boid.behaviors.BoidAvoidObstacle;
import Boid.behaviors.BoidCohesion;
import Boid.behaviors.BoidWander;
import Vehicles.Action;
import Vehicles.Vehicle;
import Vehicles.Vehicles;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.CompositeBehavior;
import behaviorFramework.arbiters.CommandFusion;

import java.awt.*;
import java.util.ArrayList;


/**
 * <p>A Sample Behavior Framework Vehicle</p>
 * <p>This one wanders the world, avoiding obstacles and seeking out the light</p>
 *
 * <p>Making changes focuses on modifying the initialization method with a new
 * behavior tree.</p>
 */
public class Boid extends Vehicle {
    /**
     * Using our own State object to store our vehicles personal data
     */
    private BoidState state;
    private behaviorFramework.Action action = new behaviorFramework.Action();
    private int ID;

    /**
     * The Behavior Tree that will be executed
     */
    CompositeBehavior behaviorTree;

    public Boid() {
        super();
        state = new BoidState();
    }

    /**
     * <p>The intialization method contains the construction of the behavior tree
     * that will be executed on each call.</p>
     *
     * <p>Call super, set your vehicle's color and then create a top level
     * CompositeBehavior that then has the remaining behaviors and
     * arbiters added to it.</p>
     *
     * <p>You will need to import any behaviors and arbiters that you make use of.</p>
     *
     * @param myWorld the simulation world passed to Vehicle to maintain connection
     */
    public void initialize(Vehicles myWorld, String vehicleType) {
        super.initialize(myWorld, state, vehicleType);
        setColor(new Color(206,81,156));
        setUserData(getClass().getName());
        state.setBlackBoard(blackBoard);

        /****************************************************

                      CommandFusion
                             |
                 ------------------------------------------
                 |            |             |             |
             BoidAlign    BoidCohesion  BoidAvoidObst  Wander

        ***************************************************/

        // Instantiate behaviorTree
        behaviorTree = new CompositeBehavior();

        // Set arbiter
        ArrayList<Double> weights = new ArrayList<Double>();
        weights.add(0.2);
        weights.add(0.4);
        weights.add(0.2);
        weights.add(0.1);
        ArbitrationUnit arbiter = new CommandFusion(weights);
        behaviorTree.setArbitrationUnit(arbiter);

        BoidAlign ba = new BoidAlign();
        BoidCohesion bc = new BoidCohesion();
        BoidAvoidObstacle ao = new BoidAvoidObstacle();
        BoidWander w = new BoidWander();
        //GetUnstuck gu = new GetUnstuck();

        behaviorTree.add(ba);
        behaviorTree.add(bc);
        behaviorTree.add(ao);
        behaviorTree.add(w);
    }

    /**
     * Every vehicle senses the world
     *
     * If you want to preprocess sensor data in some way, you
     * would call those methods and classes here.
     *
     * @return true
     */
    public boolean sense(){
        // Send our message
        ID = Integer.parseInt(((String)this.userData).substring(((String)this.userData).length() - 3, ((String)this.userData).length()));
        blackBoard.setMessage(ID,this.getLinearVelocity());

        // Must update the base sensors first
        super.sense();

        // Place any other state updating you would like to do here:


        return true;
    }

    /**
     * Call the behavior framework and get an action to execute
     *
     * @return the action to execute
     */
    public Action decideAction() {
        action.clear();

        // Get an action from the behaviorTree
        action = behaviorTree.genAction(state);

        System.out.println(getClass().getName() + ": " + action.name + " " + action.getLeftWheelVelocity() + " " + action.getRightWheelVelocity());

        lastAction = action.name;

        return action;
    }
}

