package behaviorFramework.behaviors;

import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

/**
 * This is a do nothing sample behavior.  It will always vote to stop the
 * motion of the vehicle.d
 *
 */
 
public class NoOp extends Behavior {

	public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();

        // This is a do nothing behavior...what did you expect!
        action.setLeftWheelVelocity(0);
        action.setRightWheelVelocity(0);

        // Make sure to vote if you want a chance to be picked.
		action.setVote(1);

		return action;
	}
}