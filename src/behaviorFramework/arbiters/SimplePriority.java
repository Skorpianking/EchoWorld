package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.Collection;

/**
 * This is a priority Based Arbiter.  Any non-zero value of vote will
 * allow a higher priority action to preempt a lower priority action.
 * 
 * @author Brian Woolley - for use by AFIT/ENG - CSCE623 and CSCE723
 */
public class SimplePriority extends ArbitrationUnit {

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();

		// Accept the action with the higest priority * a non-zero vote
		//
		for(Action a : actionSet) {
			if (a.getVote() != 0) action = a;
		}
		// returns a noOp if no action in the set votes
	    return action;
	}
}
