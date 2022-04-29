package behaviorFramework.behaviors;

import behaviorFramework.Action;
import behaviorFramework.Behavior;
import behaviorFramework.State;

/**
 * This is a sitting duck behavior.  It will always vote to stop the
 * motion of the robot as well as its turret and radar.
 * 
 * @author Brian Woolley - for use by AFIT/ENG - CSCE623 and CSCE723
 */
 
public class SittingDuck extends Behavior {
	public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();

        // This is a sitting duck behavior...what did you expect!
        action.setLeftWheelVelocity(0);
        action.setRightWheelVelocity(0);
        
        // Make sure to vote if you want a chance to be picked.
		action.setVote(1);
		
		return action;
	}
}