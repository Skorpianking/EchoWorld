package Ants.Behaviors;

import Vehicles.SensedObject;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.List;

/**
 * Moves ant towards a resource.
 */

public class GotoResource extends Behavior {
    // Vote = 2
    // Angle limit = 75 deg
    // motor outputs are 1 and 1 - angle in radians
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "GotoResource";

        List<SensedObject> sensedObjects = state.getSensedObjects();

        double angle;
        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees
            if (obj.getType().equals("Resource")) {
                if (angle > 0 && angle < 75) { // Light on right
                    action.setRightWheelVelocity(1.0 - ((angle * Math.PI) / 180));
                    action.setLeftWheelVelocity(1.0);
                    action.setVote(1);
                } else if (angle < 0 && angle > -75) { // Light on left
                    action.setRightWheelVelocity(1.0);
                    action.setLeftWheelVelocity(1.0 + ((angle * Math.PI) / 180));
                    action.setVote(1);
                }
            }
        }
        return action;
    }

}
