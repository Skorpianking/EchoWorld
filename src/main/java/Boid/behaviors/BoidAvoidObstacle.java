package Boid.behaviors;

import Vehicles.SensedObject;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.List;

/**
 * <p>Causes the vehicle to veer away from obstacles.</p>
 */
public class BoidAvoidObstacle extends Behavior {

    /**
     * Activation threshold (distance < DISTANCE_LIMIT)
     */
    private final int DISTANCE_LIMIT = 4;
    /**
     * Limit the angles of concern to those in front
     */
    private final int ANGLE_LIMIT = 75;
    // Vote = 1
    // Motor outs are 0.8 and 0.05

    /**
     * <p>Turn away from an obstacle if it is too close. <br>
     * Action outputs are for steer and thrust.</p>
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

        action.name = new String("BoidAvoidObstacle");
        action.setVote(0);

        double angle = 0;
        double smallestDistance = DISTANCE_LIMIT;
        for (SensedObject obj : sensedObjects) {
            if (obj.getType().equals("Obstacle")) {
                angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees
                // slow down if very close to obstacle
                double thrust = 1 - ((DISTANCE_LIMIT - obj.getDistance())/DISTANCE_LIMIT);

                if (angle >= 0 && angle < ANGLE_LIMIT && obj.getDistance() < smallestDistance) { // Obstacle on right
                    action.setRightWheelVelocity(thrust);
                    action.setLeftWheelVelocity((ANGLE_LIMIT-angle)/(ANGLE_LIMIT-5));
                    action.setVote(1);
                    smallestDistance = obj.getDistance();
                } else if (angle < 0 && angle > -ANGLE_LIMIT && obj.getDistance() < smallestDistance) { // Obstacle on left
                    action.setRightWheelVelocity(thrust);
                    action.setLeftWheelVelocity(-(ANGLE_LIMIT-angle)/(ANGLE_LIMIT-5));
                    action.setVote(1);
                    smallestDistance = obj.getDistance();
                }
            }
        }

        return action;
    }
}
