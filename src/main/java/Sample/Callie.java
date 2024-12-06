package Sample;

import Sample.behaviors.*;
import Vehicles.Action;
import Vehicles.Vehicle;
import Vehicles.Vehicles;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.CompositeBehavior;
import behaviorFramework.arbiters.HighestActivation;
import behaviorFramework.arbiters.SimplePriority;
import behaviorFramework.arbiters.Conditional;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * <p>A Sample Behavior Framework Vehicle</p>
 * <p>This one wanders the world, avoiding obstacles and seeking out the light</p>
 *
 * <p>Making changes focuses on modifying the initialization method with a new
 * behavior tree.</p>
 */
public class Callie extends Vehicle {
    /**
     * Using our own State object to store our vehicles personal data
     */
    private MyState state;
    private behaviorFramework.Action action = new behaviorFramework.Action();

    /**
     * The Behavior Tree that will be executed
     */
    CompositeBehavior behaviorTree;

    public Callie() {
        super();
        state = new MyState();
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

                /*
                        -> Conditional(isHolding)
                                    |
              t ------------------------------------------  f
                |                                        |
          Conditional(isAtHome)                    HighestActivation
                |                                        |
        t -------------------- f           ------------------------------------------------
          |                  |             |             |            |          |        |
      Drop(Food)      SimplePriority   PickUp(Food)  GetUnstuck  AvoidObst  GotoX(Food) Wander
                             |
                 ----------------------------------
                 |           |           |        |
             GetUnstuck  GotoX(Home) AvoidObst  Wander

         */

        // Instantiate behaviorTree
        behaviorTree = new CompositeBehavior();

        // Set arbiter
        // ArbitrationUnit arbiter = new SimplePriority();
        ArbitrationUnit arbiter = new Conditional("isHolding", state);
        behaviorTree.setArbitrationUnit(arbiter);

        CompositeBehavior atHome = new CompositeBehavior(); // isHolding == true
        atHome.setArbitrationUnit((new Conditional("isAtHome", state)));
        behaviorTree.add(atHome);

        atHome.add(new Drop()); // isAtHome == true

        CompositeBehavior goHome = new CompositeBehavior(); // isAtHome == false
        goHome.setArbitrationUnit(new SimplePriority());
        atHome.add(goHome);

        GetUnstuck gu = new GetUnstuck();
        AvoidObstacle ao = new AvoidObstacle();
        Wander w1 = new Wander();
        Wander w2 = new Wander();

        goHome.add(gu);
        goHome.add(ao);
        goHome.add(new GotoX("Home"));
        goHome.add(w1);

        CompositeBehavior comp = new CompositeBehavior(); // isHolding == false
        comp.setArbitrationUnit(new HighestActivation(new ArrayList<Double>(Arrays.asList(0.5,0.2,0.1,0.1,0.1))));
        behaviorTree.add(comp);

        comp.add(new PickUp("Food"));
        comp.add(gu);
        comp.add(ao);
        comp.add(new GotoX("Food"));
        comp.add(w2);

        // Add behaviors
        //GotoXX gotox = new GotoXX();
        //ArrayList<String> params = new ArrayList<String>() { { add("Light");}};
        //gotox.setParameters(params);
        //behaviorTree.add(gotox);
        // OR:

        /*
        behaviorTree.add(new Drop());
        //behaviorTree.add(new GotoHome()); // don't like this because we should be able to use GotoX("Home"), but
                                          // its vote is conditional on only once food is held. This is where swapping
                                          // trees or having a game BehaviorTree state machine like activity happens
        behaviorTree.add(new PickUp("Food"));
        behaviorTree.add(new GotoX("Food"));
        //behaviorTree.add(new Love());
        behaviorTree.add(new AvoidObstacle());
        behaviorTree.add(new Wander());

        behaviorTree.add(new MyNoOp());
         */

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
        // Must update the base sensors first
        super.sense();

        // Place any other state updating you would like to do here:
        state.setMyMemory(6);

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

