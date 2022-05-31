package Sample.behaviors;

import Braitenburg.SensedObject;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import Braitenburg.State;

import java.util.List;

public class GotoX extends Behavior {
    private double lastSeenAngle = 0.0;
    private int lastSeenCounter = 0; // Set to 5 when seen
    private String target;


    // Vote = 1
    // Angle limit = 90 deg
    // motor outputs are 0.9 and 0.9 - angle in radians
    // Keeps track of last time seen and moves toward it for 5 ticks

    public GotoX(String target) {
        super();
        this.target = target;
    }

    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        List<SensedObject> sensedObjects = state.getSensedObjects();

        action.name = "Goto" + target;

        double angle = 0;
        lastSeenCounter--;
        double bestAngle = 360;
        SensedObject bestObj = null;

        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees

            // Locate the target return that is closest to the centerline of the vehicle
            if (obj.getType().equals(target)) {
                if (Math.abs(angle) < Math.abs(bestAngle))
                    bestAngle = angle;
                bestObj = obj;
            }
        }

        if (bestObj != null) {
            if (bestAngle > 0  && bestObj.getSide() == "Right") { // Light on Right
                action.setRightWheelVelocity(0.9 - Math.abs(bestObj.getAngle()));
                action.setLeftWheelVelocity(0.9);
            } else if (bestAngle < 0  && bestObj.getSide() == "Left") { // Light on Left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.9 - Math.abs(bestObj.getAngle()));
            } else if ( bestAngle > 0 && bestObj.getSide() == "Left" ) { // Middle Left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.9 - Math.abs(bestObj.getAngle()) / 3);
            } else if ( bestAngle < 0 && bestObj.getSide() == "Right" ) { // Middle Right
                action.setRightWheelVelocity(0.9 - Math.abs(bestObj.getAngle()) / 3);
                action.setLeftWheelVelocity(0.9);
            }
            action.setVote(1);
            lastSeenCounter = 5;
            lastSeenAngle = bestAngle;
        }

        // We don't see the light now, but we saw it recently.
        if (lastSeenCounter > 0 && action.getVote() == 0) {
            angle = lastSeenAngle;
            if (angle > 0) { // Light on Right
                action.setRightWheelVelocity(0.9-Math.abs(((angle*Math.PI)/180)));
                action.setLeftWheelVelocity(0.9);
                action.setVote(1);
            } else if (angle < 0) { // Light on Left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.9-Math.abs(((angle*Math.PI)/180)));
                action.setVote(1);
            }
        }

        return action;
    }

}
