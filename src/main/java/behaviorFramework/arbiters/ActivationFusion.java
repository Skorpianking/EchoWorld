package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This arbiter builds an action from the sub-actions with the
 * highest utility value (utility = action.getVote * weight).
 * 
 * The new action is given the highest overall utility value.
 *
 * Action components: LeftWheelVelocity, RightWheelVelocity, Grab
 */
public class ActivationFusion extends ArbitrationUnit {

	public ActivationFusion (ArrayList<Double> weightList) {
		super();
		this.setWeights(weightList);
	}
	
	// Overloaded constructor that allows for default weight values
	public ActivationFusion() {
		this(null);
	}

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();
		int actionIndex = 0;

		double maxUtility         = 0.0;
		double utilityLeftWheel   = 0.0;
		double utilityRightWheel  = 0.0;
		double utilityGrab        = 0.0;

		for(Action a : actionSet) {
			double utility = a.getVote() * w.get(actionIndex);

			// Set the overall vote equal to the maxUtility
			if (utility > maxUtility) action.setVote(a.getVote());
			
			// Set as the highest utility for velocity so far
			if (a.getLeftWheelVelocity() != 0.0 && utility > utilityLeftWheel)
			{
				action.setLeftWheelVelocity(a.getLeftWheelVelocity());
				utilityLeftWheel = utility;
			}

			if (a.getRightWheelVelocity() != 0.0 && utility > utilityRightWheel)
			{
				action.setRightWheelVelocity(a.getRightWheelVelocity());
				utilityRightWheel = utility;
			}

			// Set as the highest utility for Grab so far
			if (a.isGrab() && utility > utilityGrab)
			{
				action.setGrab(a.isGrab());
				utilityGrab = utility;
			}

			actionIndex++;
		}	

		return action;
	}
}
