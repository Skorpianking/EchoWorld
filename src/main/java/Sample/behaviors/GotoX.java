package Sample.behaviors;

import Vehicles.SensedObject;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A goto TARGET behavior. <br>
 * Will try to minimize the distance between the vehicle and the
 * nearest TARGET.</p>
 */
public class GotoX extends Behavior {
    private double lastSeenAngle = 0.0;
    private int lastSeenCounter = 0; // Set to 5 when seen
    private String target;

    // Vote = 1
    // Angle limit = 90 deg
    // motor outputs are 0.9 and 0.9 - angle in radians
    // Keeps track of last time seen and moves toward it for 5 ticks

    /**
     * Goto needs to know what type of object to go to.
     *
     * @param target A string name of the object to move toward
     */
    public GotoX(String target) {
        super();
        this.target = target;
    }

    public GotoX() { super(); }

    /**
     * Set Goto's target parameter.
     *
     * @param params a single String that is the target type's label
     */
    public void setParameters(ArrayList<String> params) {
        super.setParameters(params);
        this.target = params.get(0);
    }

    /**
     * <p>Will identify the closest SensedObject return matching the
     * target string. Then calculate an action to move toward the target.</p>
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

        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees

            // Locate the TARGET return that is closest to the centerline of the vehicle
            if (obj.getType().equals(target)) {
                if (Math.abs(angle) < Math.abs(bestAngle)) {
                    bestAngle = angle;
                    bestObj = obj;
                }
            }
        }

        if (bestObj != null) {
               double bestHeading = bestObj.getAngle();
            if (Math.abs(bestHeading) < 0.07 || bestObj.getSide().equals("Center")) { // Object is directly ahead.
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.9);
            } else if (bestHeading > 2.8 || bestHeading < -2.8) { // directly behind, go left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.0 - Math.abs(bestObj.getAngle()));
            } else if (bestAngle < 0.0) {
                action.setRightWheelVelocity(0.0 - Math.abs(bestObj.getAngle()));
                action.setLeftWheelVelocity(0.9);
            } else if (bestAngle > 0.0) { // Object on Left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.0 - Math.abs(bestObj.getAngle()));
            }
            action.setVote(Math.max(1.1, 2-bestObj.getDistance()));
            lastSeenCounter = 5;
            lastSeenAngle = bestAngle;
        }

        // We don't see the TARGET now, but we saw it recently.
        else if (lastSeenCounter > 0 && action.getVote() == 0) {
            angle = lastSeenAngle;
            if (angle < 0) { // Light on Right
                action.setRightWheelVelocity(0.9-Math.abs(((angle*Math.PI)/180)));
                action.setLeftWheelVelocity(0.9);
                action.setVote(1);
            } else if (angle > 0) { // Light on Left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.9-Math.abs(((angle*Math.PI)/180)));
                action.setVote(1);
            }
        }

        return action;
    }

}
