package Sample.behaviors;

import behaviorFramework.Action;
import behaviorFramework.Behavior;

import Braitenburg.State;
import Sample.MyState; // If you want to use your state in a behavior, you need
                       // to include it as an import and also cast state to it
                       // to make calls (see below).

/**
 * This is a do nothing sample behavior.  It will always vote to stop the
 * motion of the vehicle.
 * <p>
 * This also shows how to load and store memory into your own state object
 *
 */

public class MyNoOp extends Behavior {

    /**
     * Does nothing but places a value into the Sample vehicles
     * state memory.
     *
     * @param state MyState an extension of State
     * @return an empty action
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "MyNoOp";

        // This is a do nothing behavior...what did you expect!
        action.setLeftWheelVelocity(0.0);
        action.setRightWheelVelocity(0.0);

        // But if you want, you can still get and put things in your State for later
        // Don't forget the import ^^^
        ((MyState)state).setMyMemory(45);

        // Make sure to vote if you want a chance to be picked.
        action.setVote(1);

        return action;
    }
}