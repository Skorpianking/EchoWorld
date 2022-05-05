package Sample.behaviors;

import Braitenburg.SensedObject;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import Braitenburg.State;

import java.util.List;

public class GotoLight extends Behavior {
    private double lastSeenAngle = 0.0;
    private int lastSeenCounter = 0;

    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        List<SensedObject> sensedObjects = state.getSensedObjects();

        action.name = "GotoLight";

        double angle = 0;
        lastSeenCounter--;
        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees

            if (obj.getType().equals("Light")) {
                if (angle > 0 && angle < 75 ) { // Light on right
                    action.setRightWheelVelocity(1.0-((angle*Math.PI)/180));
                    action.setLeftWheelVelocity(1.0);
                    action.setVote(1);
                    lastSeenCounter = 5;
                } else if (angle < 0 && angle > -75 ) { // Light on left
                    action.setRightWheelVelocity(1.0);
                    action.setLeftWheelVelocity(1.0+((angle*Math.PI)/180));
                    action.setVote(1);
                    lastSeenCounter = 5;
                }
                lastSeenAngle = angle;
            }
        }

        // We don't see the light now, but we had seen it recently.
        if (lastSeenCounter > 0 && action.getVote() == 0) {
            angle = lastSeenAngle;
            if (angle > 0 && angle < 75 ) { // Light on right
                action.setRightWheelVelocity(1.0-((angle*Math.PI)/180));
                action.setLeftWheelVelocity(1.0);
                action.setVote(1);
            } else if (angle < 0 && angle > -75 ) { // Light on left
                action.setRightWheelVelocity(1.0);
                action.setLeftWheelVelocity(1.0+((angle*Math.PI)/180));
                action.setVote(1);
            }
        }

        return action;
    }

}
