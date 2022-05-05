package behaviorFramework.behaviors;

import behaviorFramework.Action;
import behaviorFramework.Behavior;

import Braitenburg.State;

import java.util.Random;

/**
 * Returns an action that has the robot wander.
 * Action returned is always go forward and randomly
 * sampled via a Gaussian around the last velocity
 * to limit large velocity changes.
 *
 */

public class Wander extends Behavior {
    private double ST_DEV = 0.2;

    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        Random rand = new Random();

        // Adjust the current wheel velocities via a normal distribution around
        // their current velocity: rand.nextGaussian()*st_dev+mean;
        double left = rand.nextGaussian()*ST_DEV+state.getLeftWheelVelocity();
        double right = rand.nextGaussian()*ST_DEV+state.getRightWheelVelocity();
        if (left < 0.0)  // Behavior: we want the robot always moving forward
            left = 0.05;
        if (right < 0.0)
            right = 0.05;
        action.setLeftWheelVelocity(left);
        action.setRightWheelVelocity(right);
        action.setVote( 10 ) ;

		return action;
	}
}