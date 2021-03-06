package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This arbiter builds an action from a sum of all voting sub-actions
 * scaled by the actions utility:
 * (i.e. velocity += velocity[i] * action.getVote() * w[i])
 *
 * The new action is given the highest overall vote value.
 *
 * Action components: LeftWheelVelocity, RightWheelVelocity, Grab
 *
 */
public class CommandFusion extends ArbitrationUnit {

	public CommandFusion (ArrayList<Double> weightList) {
		super();
		this.setWeights(weightList);
	}
	
	// Overloaded constructor that allows for default weight values
	public CommandFusion() {
		this(null);
	}

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();
		int actionIndex = 0;

		assert (w.size() >= actionSet.size());
		
		double maxVote = 0.0;
		
		double leftWheel = 0.0;
		double rightWheel = 0.0;
		boolean grab = false;

		double uLeftWheel 	= 0.0;
		double uRightWheel	= 0.0;
		double uGrab 	= 0.0;

		for(Action a : actionSet) {
			double utility =  a.getVote() * w.get(actionIndex);
			if (a.getVote() > maxVote) maxVote = a.getVote();

			if (a.getLeftWheelVelocity() != 0.0)
			{
				leftWheel += a.getLeftWheelVelocity() * utility;
				uLeftWheel += utility;
			}
			if (a.getRightWheelVelocity() != 0.0)
			{
				rightWheel += a.getRightWheelVelocity() * utility;
				uRightWheel += utility;
			}
			if (a.isGrab())
			{
				grab = true;
				uGrab += utility;
			}
			actionIndex++;
		}
		
		action.setVote(maxVote);
		action.setLeftWheelVelocity(leftWheel/uLeftWheel);
		action.setRightWheelVelocity(rightWheel/uRightWheel);
		action.setGrab(grab);

		return action;
	}
}
