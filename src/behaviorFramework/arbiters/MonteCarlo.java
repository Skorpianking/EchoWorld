package behaviorFramework.arbiters;

import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * The MonteCarlo arbitration method randomly selects actions from a
 * weighted distribution of the action's vote * an internal weight.
 * The selected action is ecacted for 20 game turns.
 * 
 * @author Brian Woolley - for use by AFIT/ENG
 */
public class MonteCarlo extends ArbitrationUnit {
	Random rand = new Random();
	double count = 0, turnCounter = 20;

	public MonteCarlo (ArrayList<Double> weightList) {
		super();
		this.setWeights(weightList);
	}

	// Overloaded constructor that allows for default weight values
	public MonteCarlo() {
		this(null);
	}

	public Action evaluate(Collection<Action> actionSet) {
		Action action = new Action();
		int actionIndex = 0;
		
		if (count < 1)
		{
			count = turnCounter;
		}
		else
		{
			double ratio = 0.0;
			for(Action a : actionSet) {
				ratio += (a.getVote() * w.get(actionIndex));
				actionIndex++;
			}

			// Random selection of an Action, weighted by Vote
			//
			double selector = (rand.nextDouble() * ratio); 	
			for(Action a : actionSet) 
			{
				if (a.getVote() > selector) {
					action = a;
					break;
				}
				selector -= a.getVote();
			}
		}
		return action;
	}
}
