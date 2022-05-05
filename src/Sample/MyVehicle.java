package Sample;

import Braitenburg.Action;
import Braitenburg.SensedObject;
import Braitenburg.State;
import Braitenburg.Vehicle;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.CompositeBehavior;
import behaviorFramework.arbiters.SimplePriority;
import behaviorFramework.behaviors.NoOp;
import behaviorFramework.behaviors.Wander;
import framework.SimulationBody;
import org.dyn4j.world.World;

import java.awt.*;
import java.util.List;


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
        behaviorTree.add(new Wander());
        behaviorTree.add(new NoOp());
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
     * For the UBF...
     */
    public Action decideAction() {
        action.clear();

        // Get an action from the behaviorTree
        action = behaviorTree.genAction(state);

        System.out.println("Action: " + action.getLeftWheelVelocity() + " " + action.getRightWheelVelocity());
        double angle;
        List<SensedObject> sensedObjects = state.getSensedObjects();

        for (SensedObject obj : sensedObjects) {
            if(obj.getType().equals("Light") ) {
                angle = obj.getAngle()* 180 / Math.PI;;// conversion from radians to degrees
                state.getVelocity();

//                System.out.println("Velocity: " + state.getVelocity() + " Angle: " + angle);
            }
        }

        return action;
    }
}

