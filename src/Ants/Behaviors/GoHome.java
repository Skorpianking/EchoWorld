package Ants.Behaviors;

import Braitenburg.SensedObject;
import Braitenburg.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;
import org.dyn4j.geometry.Vector2;

import java.util.List;

/**
 * Sends the agent to a home vector that needs to be added to the this behavior when created.
 */

public class GoHome extends Behavior {
    // Vote = 10 -- highest priority right now
    public Action genAction(State state) {
        assert (state != null);
        Action action = new Action();
        action.name = "GoHome";
        List<SensedObject> sensedObjects = state.getSensedObjects();

        double angle = 0;
        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees
            if (obj.getType().equals("Home")) {
                if (angle > 0 && angle < 75) { // Light on right
                    action.setRightWheelVelocity(1.0 - ((angle * Math.PI) / 180));
                    action.setLeftWheelVelocity(1.0);
                    action.setVote(3);
                } else if (angle < 0 && angle > -75) { // Light on left
                    action.setRightWheelVelocity(1.0);
                    action.setLeftWheelVelocity(1.0 + ((angle * Math.PI) / 180));
                    action.setVote(4);
                }
            }
        }
        return action;
    }
}
