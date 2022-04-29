package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This arbiter builds an action that executes the sub-actions with the
 * highest priority that voted. The new action is given a non-zero vote.
 * 
 * If a high priority behavior submits an action, the specified actions
 * are used and any unspecified sub-actions are taken from the behavior
 * with the next-highest priority.
 *  
 * @author Brian Woolley - for use by AFIT/ENG
 */
public class PriorityFusion extends ArbitrationUnit {

	public PriorityFusion (ArrayList<Double> weightList) {
		super();
		this.setWeights(weightList);
	}

	// Overloaded constructor that allows for default weight values
	public PriorityFusion() {
		this(null);
	}

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();
		int actionIndex = 0;

		double maxPriority			 = 0.0;
		double priorityVelocity		 = 0.0;
		double priorityTurnRate		 = 0.0;
		double priorityGunRotation 	 = 0.0;
		double priorityFirePower	 = 0.0;
		double priorityRadarRotation = 0.0;
		double priorityScan 		 = 0.0;
		double priorityAllStop		 = 0.0;
/*
		for(Action a : actionSet) {
			if (a.getVote() > 0.0) {
				double priority = w.get(actionIndex);
				
				// Set the overall vote equal to the maxPriority
				if (priority > maxPriority) action.setVote(a.getVote());
				
				// Set as the higest priority for velocity so far
				if (a.isVelocitySet() && priority > priorityVelocity)
				{
					action.setVelocity(a.getVelocity());
					priorityVelocity = priority;
				}
				
				// Set as the higest priority for turnRate so far
				if (a.isTurnRateSet() && priority > priorityTurnRate)
				{
					action.setTurnRate(a.getTurnRate());
					priorityTurnRate = priority;
				}
				
				// Set as the higest priority for gunRotation so far
				if (a.isGunRotationSet() && priority > priorityGunRotation)
				{
					action.setGunRotation(a.getGunRotation());
					priorityGunRotation = priority;
				}
				
				// Set as the higest priority for firePower so far
				if (a.isFirePowerSet() && priority > priorityFirePower)
				{
					action.setFireGun(a.getFirePower());
					priorityFirePower = priority;
				}
				
				// Set as the higest priority for RadarRotation so far
				if (a.isRadarRotationSet() && priority > priorityRadarRotation)
				{
					action.setRadarRotation(a.getRadarRotation());
					priorityRadarRotation = priority;
				}
				
				// Set as the highest priority for scan so far
				if (a.isScanSet() && priority > priorityScan)
				{
					action.scan();
					priorityScan = priority;
				}
				
				// Set as the highest priority for AllStop so far
				if (a.isAllStopSet() && priority > priorityAllStop)
				{
					priorityAllStop = priority;
				}
				actionIndex++;
			}
		}
		*
 */
		// return a noOp if no action in the set votes
		return action;
	}

		// Return the highest priority action with a non-zero vote
		//
}
