package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.Collection;

/**
 * This is a priority Based Arbiter.  Any non-zero value of vote will
 * allow a higher priority action to preempt all other actions.
 * In this case, priority is based on ordering in the arbiter (first
 * to last).
 *
 */
public class SimplePriority extends ArbitrationUnit {

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();

		// Accept the first action with  a non-zero vote
		//
		for(Action a : actionSet) {
			if (a.getVote() != 0) action = a;
			break;
		}

		// returns a noOp if no action in the set votes
	    return action;
	}
}
