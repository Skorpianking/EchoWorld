package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;
import framework.SimulationBody;

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
		SimulationBody pickup = null;
		boolean drop = false;

		double uLeftWheel 	= 0.0;
		double uRightWheel	= 0.0;

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
			if (a.getPickup() != null)
			{
				pickup = a.getPickup();
			}
			if (a.getDrop())
			{
				drop = a.getDrop();
			}
			actionIndex++;
		}

		action.setVote(maxVote);
		if(uLeftWheel > 0.0)
			action.setLeftWheelVelocity(leftWheel/uLeftWheel);
		if(uRightWheel > 0.0)
			action.setRightWheelVelocity(rightWheel/uRightWheel);
		action.setPickup(pickup);
		if (drop)
			action.setDrop(true);

		action.name = new String("CommandFusion"+Math.max(uLeftWheel,uRightWheel));

		if(action.getLeftWheelVelocity() == Double.NaN || action.getRightWheelVelocity() == Double.NaN)
			System.out.println("WHAT?");

		return action;
	}
}
