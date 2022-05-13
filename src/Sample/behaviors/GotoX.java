package Sample.behaviors;

import Braitenburg.SensedObject;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import Braitenburg.State;
import org.dyn4j.geometry.Vector2;

import java.util.List;

public class GotoX extends Behavior {
    private double lastSeenAngle = 0.0;
    private int lastSeenCounter = 0; // Set to 5 when seen
    private String target;


    // Vote = 1
    // Angle limit = 75 deg
    // motor outputs are 1 and 1 - angle in radians

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
        Vector2 heading;
        lastSeenCounter--;
        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees

            if (obj.getType().equals(target)) {
                if (angle > 0 && angle < 75 && obj.getSide() == "Right") { // Light on right
                    action.setRightWheelVelocity(1.0-((angle*Math.PI)/180));
                    action.setLeftWheelVelocity(1.0);
                    action.setVote(1);
                    lastSeenCounter = 5;
                } else if (angle < 0 && angle > -75 && obj.getSide() == "Left") { // Light on left
                    action.setRightWheelVelocity(1.0);
                    action.setLeftWheelVelocity(1.0+((angle*Math.PI)/180));
                    action.setVote(1);
                    lastSeenCounter = 5;
                } else if ( angle > 0 && obj.getSide() == "Left" ) { // Middle Left
                    action.setRightWheelVelocity(0.9 + ((angle*Math.PI)/180));
                    action.setLeftWheelVelocity(0.9);
                    action.setVote(1);
                    lastSeenCounter = 5;
                } else if ( angle < 0 && obj.getSide() == "Right" ) { // Middle Right
                    action.setRightWheelVelocity(0.9 + ((angle*Math.PI)/180));
                    action.setLeftWheelVelocity(0.9);
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
