package Sample.behaviors;

import Braitenburg.SensedObject;
import Braitenburg.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.List;

/**
 * <p>A goto TARGET behavior. <br>
 * Will try to minimize the distance between the vehicle and the
 * nearest TARGET.</p>
 */
public class GotoXX extends Behavior {
    private double lastSeenAngle = 0.0;
    private int lastSeenCounter = 0; // Set to 5 when seen
    private String target;


    // Vote = 1
    // motor outputs are 0.9 and 0.9 - angle in radians
    // Keeps track of last time seen and moves toward it for 5 ticks

    /**
     * Goto needs to know what type of object to go to.
     *
     * @param target A string name of the object to move toward
     */
    public GotoXX(String target) {
        super();
        this.target = target;
    }

    /**
     * <p>Will identify the closest SensedObject return matching the
     * target string. Then calculate and action to move toward the target.</p>
     *
     * <p>It also moves toward the last seen target for 5 ticks after seeing
     * one.</p>
     *
     * <p>Vote = 1 if target is seen or was seen within 5 ticks.<br>
     * Vote = 0 otherwise.</p>
     *
     * @param state the current state
     * @return an action to move toward the closest TARGET
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "Goto" + target;

        List<SensedObject> sensedObjects = state.getSensedObjects();

        double angle = 0;
        lastSeenCounter--;
        double bestAngle = 360;
        SensedObject bestObj = null;

        double maxVelocity = 0.9;

        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees

            // Locate the TARGET return that is closest to the centerline of the vehicle
            if (obj.getType().equals(target)) {
                if (Math.abs(angle) < Math.abs(bestAngle))
                    bestAngle = angle;
                bestObj = obj;
            }
        }

        if (bestObj != null) {
            if (bestObj.getDistance() <= 3.0)
                maxVelocity = maxVelocity * (bestObj.getDistance() / 3.0);

            if (bestAngle > 0  ) { // TARGET on Right
                action.setRightWheelVelocity(maxVelocity - Math.abs(bestObj.getAngle()));
                action.setLeftWheelVelocity(maxVelocity);
            } else if (bestAngle < 0  ) { // TARGET on Left
                action.setRightWheelVelocity(maxVelocity);
                action.setLeftWheelVelocity(maxVelocity - Math.abs(bestObj.getAngle()));
            }
            action.setVote(1);
            lastSeenCounter = 5;
            lastSeenAngle = bestAngle;
        }

        // We don't see the TARGET now, but we saw it recently.
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
