package Sample;

import Braitenburg.Action;
import Braitenburg.Vehicle;
import Sample.behaviors.AvoidObstacle;
import Sample.behaviors.GotoX;
import Sample.behaviors.GotoXX;
import Sample.behaviors.Love;
import Sample.behaviors.MyNoOp;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.CompositeBehavior;
import behaviorFramework.arbiters.SimplePriority;
import behaviorFramework.behaviors.NoOp;
import behaviorFramework.behaviors.Wander;
import framework.SimulationBody;
import org.dyn4j.world.World;

import java.awt.*;

/**
 * Sample Vehicle
 *
 */

public class MyVehicle extends Vehicle {
    private MyState state;
    private behaviorFramework.Action action = new behaviorFramework.Action();
    CompositeBehavior behaviorTree;

    public MyVehicle() {
        state = new MyState();
    }

    public void initialize(World<SimulationBody> myWorld) {
        super.initialize(myWorld, state);
        setColor(new Color(160,0,255));

        // Instantiate behaviorTree
        ArbitrationUnit arbiter = new SimplePriority();
        behaviorTree = new CompositeBehavior();

        behaviorTree.setArbitrationUnit(arbiter);
        behaviorTree.add(new GotoXX("Light"));
        //behaviorTree.add(new Love());
        behaviorTree.add(new AvoidObstacle());
        behaviorTree.add(new Wander());
        behaviorTree.add(new MyNoOp());
    }

    /**
     * Every vehicle senses the world
     */
    public boolean sense(){
        // Must update the base sensors first
        super.sense();

        // Place any other state updating you would like to do here:
        state.setMyMemory(6);

        return true;
    }

    /**
     * Call the behavior framework
     *
     */
    public Action decideAction() {
        action.clear();

        // Get an action from the behaviorTree
        action = behaviorTree.genAction(state);

        System.out.println(action.name + " " + action.getLeftWheelVelocity() + " " + action.getRightWheelVelocity());

        return action;
    }
}

