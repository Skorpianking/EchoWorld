package behaviorFramework;

import behaviorFramework.arbiters.HighestPriority;

import Braitenburg.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * This is a composite behavior, following the composite pattern.  It can
 * have either elemental behaviors or other composite behaviors as its
 * child elements. It must have an arbitration unit as a way of selecting
 * the Action that will be passed up the hierarchy.
 * 
 * @author Brian Woolley - for use by AFIT/ENG - CSCE623 and CSCE723
 */
public class CompositeBehavior extends Behavior {
	private List<Behavior> behaviorSet = new ArrayList<Behavior>();
	private ArbitrationUnit arbiter = new HighestPriority();
	
	public Action genAction(State state) {
		assert (state != null);
		
		List<Action> actionSet = new ArrayList<Action>();
		
		// If the actionSet isEmpty return a noOp action 
		if (behaviorSet.isEmpty()) return new Action();

		for (Behavior b : behaviorSet)
			actionSet.add(b.genAction(state));
		return arbiter.evaluate(actionSet);
	}
	
	public void add(Behavior newBehavior) {
		assert(newBehavior != null);
		behaviorSet.add(newBehavior);
	}
	
	public boolean remove(Behavior existingBehavior) {
		assert(existingBehavior != null);
		return behaviorSet.remove(existingBehavior);
	}
	
	public ArbitrationUnit getArbiter() {
		return arbiter;
	}

	public Collection<Behavior> getBehaviorSet() {
		return behaviorSet;
	}

	public void setArbitrationUnit(ArbitrationUnit au) {
		assert(au != null);
		arbiter = au;
	}
}
