package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This arbiter builds an action that represents a vector fusion of
 * all of the proposed actions into a single action object.
 * 
 * The strength of each contributing (non-zero) sub-action is scaled
 * the contributing actions weight: (i.e. velocity += velocity[i] * w[i])
 * 
 * The new action is given the highest overall utility value.
 * The AllStop command is meaningless and is ignored.
 * The Scan command is issued regardless.
 * 
 * @author Brian Woolley - for use by AFIT/ENG
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
		
		double velocity = 0.0;
		double turnRate = 0.0;
		double gunTurnRate = 0.0;
		double firePower = 0.0;
		double radarTurn = 0.0;

		double uVelocity 	= 0.0;
		double uTurnRate	= 0.0;
		double uGunTurn 	= 0.0;
		double uFirePower	= 0.0;
		double uRadarTurn 	= 0.0;
/*
		for(Action a : actionSet) {
			double utility =  a.getVote() * w.get(actionIndex);
			if (a.getVote() > maxVote) maxVote = a.getVote();

			if (a.isVelocitySet())
			{
				velocity += a.getVelocity() * utility;
				uVelocity += utility;
			}
			if (a.isTurnRateSet())
			{
				turnRate += a.getTurnRate() * utility;
				uTurnRate += utility;
			}
			if (a.isGunRotationSet())
			{
				gunTurnRate += a.getGunRotation() * utility;
				uGunTurn += utility;
			}
			if (a.isFirePowerSet())
			{
				firePower += a.getFirePower() * utility;
				uFirePower += utility;
			}
			if (a.isRadarRotationSet())
			{
				radarTurn += a.getRadarRotation() * utility;
				uRadarTurn += utility;			
			}
			if (a.isScanSet())
				action.scan();
			actionIndex++;
		}
		
		action.setVote(maxVote);
		action.setVelocity(velocity/uVelocity);
		action.setTurnRate(turnRate/uTurnRate);
		action.setGunRotation(gunTurnRate/uGunTurn);
		action.setFireGun(firePower/uFirePower);
		action.setRadarRotation(radarTurn/uRadarTurn);
	*/
		return action;
	}
}
