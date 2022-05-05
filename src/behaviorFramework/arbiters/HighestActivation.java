package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This arbiter selects the action with the highest utility value.
 *  (utility = action.getVote * weight)
 *
 */
public class HighestActivation extends ArbitrationUnit {

	public HighestActivation (ArrayList<Double> weightList) {
		super();
		this.setWeights(weightList);
	}
	
	// Overloaded constructor that allows for default weight values
	public HighestActivation() {
		this(null);
	}

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();
		double maxUtility = 0.0;
		int actionIndex = 0;
		
		// Return the action with the greatest utility
		// Each action's vote value is weighted
		//
		for(Action a : actionSet)
		{
			double utility = a.getVote() * w.get(actionIndex);
			if (utility > maxUtility)
			{
				action = a;
				maxUtility = utility;
			}
			actionIndex++;
		}
		// return a noOp if no action in the set votes
	    return action;
	}
}
