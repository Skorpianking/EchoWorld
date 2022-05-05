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

    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        List<SensedObject> sensedObjects = state.getSensedObjects();

        action.name = new String("AvoidObstacle");

        double angle = 0;
        double heading = 0;
        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees
            heading = state.getVelocity().getDirection();

            if (angle > 0 && angle < 75 && obj.getDistance() < 3) { // Obstacle on right
                action.setRightWheelVelocity(0.7);
                action.setLeftWheelVelocity(0.01);
                action.setVote(1);
            } else if (angle < 0 && angle > -75 && obj.getDistance() < 3) { // Obstacle on left
                action.setRightWheelVelocity(0.01);
                action.setLeftWheelVelocity(0.7);
                action.setVote(1);
            }
        }

        return action;
    }
}
