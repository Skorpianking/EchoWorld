package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This arbiter selects the action with the highest priority, (if it voted)
 * winner takes all.   --if (vote &gt; 0) priority = weight
 *
 */
public class HighestPriority extends ArbitrationUnit {

	public HighestPriority (ArrayList<Double> weightList) {
		super();
		this.setWeights(weightList);
	}

	// Overloaded constructor that allows for default weight values
	public HighestPriority() {
		this(null);
	}

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();
		double maxPriority = 0.0;
		int actionIndex = 0;
		
		// Return the highest priority action with a non-zero vote
		//
		for(Action a : actionSet)
		{
			if (a.getVote() > 0.0)
			{
				double priority = w.get( actionIndex);
				if (priority > maxPriority)
				{
					action = a;
					maxPriority = priority;
				}
			}
			actionIndex++;
		}
		// return a noOp if no action in the set votes
		return action;
	}
}
