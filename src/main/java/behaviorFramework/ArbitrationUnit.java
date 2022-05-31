package behaviorFramework;

import java.util.ArrayList;
import java.util.Collection;

/**
 * All interchangable Arbitration Units must conform to this standard interface
 * to be usable within the behavior framework.
 *  
 * @author Brian Woolley - for use by AFIT/ENG - CSCE623 and CSCE723
 */
public abstract class ArbitrationUnit {

	protected ArrayList<Double> w = new ArrayList<Double>();

	public ArrayList<Double> getWeights () {
		return w;
	}

	public void setWeights (ArrayList<Double> weights) {
		if (weights == null) {
			for (int i=0; i<4; i++)
				w.add(0.25);
			return;
		}

		// Normalize the list of values and add them to w
		double sum = 0.0;
		for (double d : weights)
			sum += d;

		w.clear();
		for (double d : weights) {
			w.add(weights.indexOf(d), d/sum);
		}
	}
	public abstract Action evaluate(Collection<Action> ActionSet);
}