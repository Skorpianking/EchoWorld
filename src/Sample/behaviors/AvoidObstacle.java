package Sample.behaviors;

import Braitenburg.SensedObject;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import Braitenburg.State;

import java.util.List;
import java.util.Random;

/**
 * Returns an action that has the robot wander.
 * Action returned is always go forward and randomly
 * sampled via a Gaussian around the last velocity
 * to limit large velocity changes.
 *
 */
public class AvoidObstacle extends Behavior {

    private final int DISTANCE_LIMIT = 3;
    private final int ANGLE_LIMIT = 75;
    // Vote = 1
    // Motor outs are 0.7 and 0.05

    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        List<SensedObject> sensedObjects = state.getSensedObjects();

        action.name = new String("AvoidObstacle");

        double angle = 0;
        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees

            if (obj.getSide() == "Right" && angle > 0 && angle < ANGLE_LIMIT && obj.getDistance() < DISTANCE_LIMIT) { // Obstacle on right
                action.setRightWheelVelocity(0.7);
                action.setLeftWheelVelocity(0.05);
                action.setVote(1);
            } else if (obj.getSide() == "Left" && angle < 0 && angle > -ANGLE_LIMIT && obj.getDistance() < DISTANCE_LIMIT) { // Obstacle on left
                action.setRightWheelVelocity(0.05);
                action.setLeftWheelVelocity(0.7);
                action.setVote(1);
            }
        }

        return action;
    }
}
