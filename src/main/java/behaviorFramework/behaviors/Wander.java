package behaviorFramework.behaviors;

import behaviorFramework.Action;
import behaviorFramework.Behavior;

import Braitenburg.State;

import java.util.Random;

/**
 * <p>Returns an action that has the robot wander.<br>
 * Action returned is always go forward and randomly
 * sampled via a Gaussian around the last velocity
 * to limit large velocity changes.</p>
 */
public class Wander extends Behavior {
    private final double ST_DEV = 0.2; // Size of the normal distribution

    // Vote = 1

    /**
     * <p>Returns an action that is randomly sampled around the current
     * velocity.</p>
     *
     * <p>Vote = 1</p>
     *
     * @param state State uses stored past velocities
     * @return a random action
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        Random rand = new Random();

        action.name = new String("Wander");

        // Adjust the current wheel velocities via a normal distribution around
        // their current velocity: rand.nextGaussian()*st_dev+mean;
        double left = rand.nextGaussian()*ST_DEV+state.getLeftWheelVelocity();
        double right = rand.nextGaussian()*ST_DEV+state.getRightWheelVelocity();
        if (left < 0.0)  // Behavior: we want the vehicle always moving forward
            left = 0.05;
        if (right < 0.0)
            right = 0.05;
        action.setLeftWheelVelocity(left);
        action.setRightWheelVelocity(right);
        action.setVote( 1 ) ;

		return action;
	}
}