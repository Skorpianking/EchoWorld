package Boid.behaviors;

import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.Random;

/**
 * <p>Returns an action that has the robot wander.<br>
 * Action returned is always go forward and randomly
 * sampled via a Gaussian around the last velocity
 * to limit large velocity changes.</p>
 */
public class BoidWander extends Behavior {
    private final double ST_DEV = 0.15; // Size of the normal distribution
    private final double DIR_COUNTER_LIMIT = 15; // How many time steps before picking a new direction

    private int directionCounter = 0;

    private double lastSteer = 0.0;

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

        action.name = new String("BoidWander");

        double steer = lastSteer;
        double thrust = 1.0;
        if (directionCounter >= DIR_COUNTER_LIMIT) {
            // Adjust the current steer velocity via a normal distribution
            steer = rand.nextGaussian() * ST_DEV + lastSteer;
            if (thrust < 0.0) // Behavior: we want the vehicle always moving forward
                thrust = 0.1;
            directionCounter = 0;
        }
        lastSteer = steer;
        directionCounter++;

        action.setLeftWheelVelocity(steer);
        action.setRightWheelVelocity(thrust);
        action.setVote( 1 ) ;

		return action;
	}
}