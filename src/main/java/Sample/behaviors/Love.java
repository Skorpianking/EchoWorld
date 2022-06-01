package Sample.behaviors;

import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

/**
 * Braitenberg's Love behavior
 *
 * <p>Approach the light and try and stay 2 distance units from it.</p>
 *
 */
public class Love extends Behavior {

    /**
     * <p>If the light is on the left or right, turns toward the light.<br>
     * If the light is in the middle, move toward it until within
     * 2 distance units.</p>
     *
     * <p>Vote = 1 if light is seen 0 otherwise</p>
     *
     * @param state a Braitenberg.State object
     * @return action
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "Love";

        double left = state.getLeftLightStrength();
        double right = state.getRightLightStrength();

        if (left > 0.0 || right > 0.0) {
            double maintainDistance = Math.min(left, right);
            if (left > 0.0 && right > 0.0 && maintainDistance < 18.0) { // Light is in the Middle
                action.setLeftWheelVelocity(0.75*maintainDistance);
                action.setRightWheelVelocity(0.75*maintainDistance);
            } else if (left > 0.0 && right > 0.0 && maintainDistance >= 18.0) { // Light is in front of us
                action.setLeftWheelVelocity(0.0);
                action.setRightWheelVelocity(0.0);
            } else if (left > 0.0) { // Light is on the Left
                action.setLeftWheelVelocity(-0.5);
                action.setRightWheelVelocity(0.5);
            } else if (right > 0.0) { //Light is on the Right
                action.setLeftWheelVelocity(0.5);
                action.setRightWheelVelocity(-0.5);
            }
            action.setVote(1);
        }

        return action;
    }

}
