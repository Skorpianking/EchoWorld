package Sample.behaviors;

import Vehicles.PathState;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.ArrayList;

/**
 * <p>A goto TARGET behavior. <br>
 * Will try to minimize the distance between the vehicle and the
 * nearest TARGET.</p>
 */
public class FollowPath extends Behavior {

    private int pathIndex;
    private int counter;
    private static int COUNTER_THRESHOLD = 30;
    private double[] targetHeading = {1.57, 0.78, 0.0, -0.78, -1.57, -2.356, 3.1415, 2.356}; // N, NE, E, SE, S, SW, W, NW


    // Vote = 1
    // Angle limit = 90 deg
    // motor outputs are 0.9 and 0.9 - angle in radians

    public FollowPath() {
        super();
        pathIndex = -1;
        counter = 0;
    }

    /**
     * Set FollowPath's target parameter.
     *
     * @param params a single String that is the target type's label
     */
    public void setParameters(ArrayList<String> params) {
//        super.setParameters(params);
//        this.target = params.get(0);
    }

    /**
     * <p>.</p>
     *
     * <p>Vote = 1 if path is valid and vehicle has had time to move away from home.<br>
     * Vote = 0 otherwise.</p>
     *
     * @param state the current state
     * @return an action to move toward
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "FollowPath";

        if (!((PathState)state).isFollowPathValid()) {
            action.setVote(0);
            counter = 10;
            pathIndex = -1;
        } else if (pathIndex == -1) {
            // Path is valid, want to wander away from home before following
            action.setVote(0);
        } else {
            // Should be away from home. Follow the path.
            int step = ((PathState)state).getStep(pathIndex);
            if (step >= 0 && step <= 8) { // If this is a valid step follow it.
                action.setVote(1);

                if (Math.abs(targetHeading[step] - state.getHeading()) < 0.1 ) { // Headed in the correct direction
                    action.setRightWheelVelocity(0.9);
                    action.setLeftWheelVelocity(0.9);
                } else if ((targetHeading[step] - state.getHeading()) < 0 ) { // Need to turn Right
                    action.setRightWheelVelocity(0.2);
                    action.setLeftWheelVelocity(0.9);
                } else if ((targetHeading[step] - state.getHeading()) > 0 ) { // Need to turn Left
                    action.setRightWheelVelocity(0.9);
                    action.setLeftWheelVelocity(0.2);
                }
            } else { // Reached end of path or path is not valid, stop following.
                action.setVote(0);
                counter = 0;
            }
        }

        counter++;
        if (counter >= COUNTER_THRESHOLD)
            pathIndex++;

        return action;
    }
}

