package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.Collection;

/**
 * This arbiter builds an action that executes the sub-action with the
 * highest utility value where: (utility = action.getVote).
 * 
 * The new action is given the highest overall utility value.
 * 
 * @author Brian Woolley, Jeff Duffy - for use by AFIT/ENG
 */
public class UtilityFusion extends ArbitrationUnit {

	public UtilityFusion () {
		super();
	}

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();
		double maxUtility 			= 0.0;
		double utilityVelocity	 	= 0.0;
		double utilityTurnRate		= 0.0;
		double utilityGunRotation 	= 0.0;
		double utilityFirePower		= 0.0;
		double utilityRadarRotation = 0.0;
		double utilityScan 			= 0.0;
		boolean scan_set			= false;
/*
		for(Action a : actionSet) {
			double utility = a.getVote();

			// Set the overall vote equal to the maxUtility
			if (utility > maxUtility){
				maxUtility = utility;
				action.setVote(a.getVote());
			}
			
			// Set as the higest utility for velocity so far
			if (a.isVelocitySet() && utility > utilityVelocity)
			{
				action.setVelocity(a.getVelocity());
				utilityVelocity = utility;
			}
			
			// Set as the highest utility for turnRate so far
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
			//But we only want to set the scan at the end
			//when we find out if the scan was set for the highest utility
			if ( utility > utilityScan)
			{		
				scan_set = a.isScanSet(); 
				utilityScan = utility;
				
			}		
		}	
		
		if(scan_set){
			action.scan();
		}
 */
		
		return action;
	}
}
