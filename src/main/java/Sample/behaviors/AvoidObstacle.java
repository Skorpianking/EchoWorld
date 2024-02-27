package Sample.behaviors;

import Vehicles.SensedObject;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.List;

/**
 * <p>Causes the vehicle to veer away from obstacles.</p>
 */
public class AvoidObstacle extends Behavior {

    /**
     * Activation threshold (distance < DISTANCE_LIMIT)
     */
    private final int DISTANCE_LIMIT = 3;
    /**
     * Limit the angles of concern to those in front
     */
    private final int ANGLE_LIMIT = 75;
    // Vote = 1
    // Motor outs are 0.8 and 0.05

    /**
     * <p>Turn away from an obstacle if it is too close. <br>
     * Action outputs are 0.7 and 0.05.</p>
     *
     * <p>Vote = 1 if vehicle is within the distance threshold.<br>
     * Vote = 0 otherwise</p>
     *
     * @param state current vehicle state
     * @return an action to turn away from obstacles
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        List<SensedObject> sensedObjects = state.getSensedObjects();

        action.name = new String("AvoidObstacle");

        double angle = 0;
        double smallestDistance = DISTANCE_LIMIT;
        for (SensedObject obj : sensedObjects) {
            if (obj.getType().equals("Obstacle")) {
                angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees

                if (angle >= 0 && angle < ANGLE_LIMIT && obj.getDistance() < smallestDistance) { // Obstacle on right
                    action.setRightWheelVelocity(0.8);
                    action.setLeftWheelVelocity(-0.05);
                    action.setVote(1);
                    smallestDistance = obj.getDistance();
                } else if (angle < 0 && angle > -ANGLE_LIMIT && obj.getDistance() < smallestDistance) { // Obstacle on left
                    action.setRightWheelVelocity(-0.05);
                    action.setLeftWheelVelocity(0.8);
                    action.setVote(1);
                    smallestDistance = obj.getDistance();
                }
            }
        }

        return action;
    }
}
