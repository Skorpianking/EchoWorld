package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This arbiter builds an action that executes the sub-action with the
 * highest utility value where: (utility = action.getVote * weight).
 * 
 * The new action is given the highest overall utility value.
 * A special case is the AllStop action, which is only requested if
 * it's requesting action has the highest utility overall. 
 * 
 * @author Brian Woolley - for use by AFIT/ENG
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
		
		double maxUtility 			= 0.0;
		double utilityVelocity	 	= 0.0;
		double utilityTurnRate		= 0.0;
		double utilityGunRotation 	= 0.0;
		double utilityFirePower		= 0.0;
		double utilityRadarRotation = 0.0;
		double utilityScan 			= 0.0;
		double utilityAllStop 		= 0.0;
/*
		for(Action a : actionSet) {
			double utility = a.getVote() * w.get(actionIndex);

			// Set the overall vote equal to the maxUtility
			if (utility > maxUtility) action.setVote(a.getVote());
			
			// Set as the higest utility for velocity so far
			if (a.isVelocitySet() && utility > utilityVelocity)
			{
				action.setVelocity(a.getVelocity());
				utilityVelocity = utility;
			}
			
			// Set as the higest utility for turnRate so far
			if (a.isTurnRateSet() && utility > utilityTurnRate)
			{
				action.setTurnRate(a.getTurnRate());
				utilityTurnRate = utility;
			}
			
			// Set as the higest utility for gunRotation so far
			if (a.isGunRotationSet() && utility > utilityGunRotation)
			{
				action.setGunRotation(a.getGunRotation());
				utilityGunRotation = utility;
			}
			
			// Set as the higest utility for firePower so far
			if (a.isFirePowerSet() && utility > utilityFirePower)
			{
				action.setFireGun(a.getFirePower());
				utilityFirePower = utility;
			}
			
			// Set as the higest utility for RadarRotation so far
			if (a.isRadarRotationSet() && utility > utilityRadarRotation)
			{
				action.setRadarRotation(a.getRadarRotation());
				utilityRadarRotation = utility;
			}
			
			// Set as the higest utility for scan so far
			if (a.isScanSet() && utility > utilityScan)
			{
				action.scan();
				utilityScan = utility;
			}
			
			// Set as the higest utility for AllStop so far
			if (a.isAllStopSet() && utility > utilityAllStop)
			{
				utilityAllStop = utility;
			}
			actionIndex++;
		}	
		
//		// AllStop is a special case
//		if (utilityAllStop == maxUtility)
//		{
//			action = new Action();
//			action.setVote(utilityAllStop);
//			action.setAllStop();
//		}
*
 */
		return action;
	}
}
