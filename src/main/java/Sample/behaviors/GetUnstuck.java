package Sample.behaviors;

import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.Random;

public class GetUnstuck extends Behavior {

    private int stuckCounter = 0;
    private int getUnstuckCounter = 0;
    private int leftORright = 0;

    /**
     * <p>If the sum of the linear velocities is close to 0, increment
     *  stuck counter.
     *   else set stuck counter to 0
     *  if stuck counter is >= 5, getUnstuckCounter = 23, and pick left or right, and set stuckCounter to 0
     *  if getUnstuckCounter > 3
     *   backup and getUnstuckCounter--
     *  if getUnstuckCounter >0 && < 4
     *   turn to prior selected direction
     *   and getUnstuckCounter--</p>
     *
     * @param state current vehicle state
     * @return an action to turn away from obstacles
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = new String("GetUnstuck");

        if(Math.abs(state.getVelocity().x) + Math.abs(state.getVelocity().y) < 0.05) {
            stuckCounter++;
        } else
            stuckCounter = 0;

        if (stuckCounter >= 5) {
            getUnstuckCounter = 23;
            Random rand = new Random();
            leftORright = rand.nextInt(2);
            stuckCounter = 0;
        }

        if (getUnstuckCounter > 3) {
            action.setRightWheelVelocity(-0.8);
            action.setLeftWheelVelocity(-0.8);
            action.setVote(1);
            getUnstuckCounter--;
        } else if (getUnstuckCounter > 0) {
            if (leftORright == 0) {
                action.setRightWheelVelocity(-0.05);
                action.setLeftWheelVelocity(0.7);
            } else {
                action.setRightWheelVelocity(0.7);
                action.setLeftWheelVelocity(-0.05);
            }
            action.setVote(1);
            getUnstuckCounter--;
        }

        return action;
    }
}
