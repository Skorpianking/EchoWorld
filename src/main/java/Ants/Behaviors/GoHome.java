package Ants.Behaviors;

import Vehicles.SensedObject;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

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

        double angle;
        double bestAngle = 360;
        SensedObject bestObj = null;

        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees
            // Locate the target return that is closest to the centerline of the vehicle
            if (obj.getType().equals("Home")) {
                if (Math.abs(angle) < Math.abs(bestAngle))
                    bestAngle = angle;
                bestObj = obj;
            }
        }

        if (bestObj != null) {
            if (bestAngle > 0  ) { // Light on Right
                action.setRightWheelVelocity(0.9 - Math.abs(bestObj.getAngle()));
                action.setLeftWheelVelocity(0.9);
            } else if (bestAngle < 0  ) { // Light on Left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.9 - Math.abs(bestObj.getAngle()));
            }
            action.setVote(10);
        }

        return action;
    }
}
