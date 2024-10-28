package behaviorFramework;

import java.util.ArrayList;
import java.util.Collection;

/**
 * All interchangable Arbitration Units must conform to this standard interface
 * to be usable within the behavior framework.
 *
 * <table>
 * <caption> Included Arbiters.</caption>
 * <thead>
 *   <tr>
 *     <th><br>Arbiter</th>
 *     <th><br>Weight</th>
 *     <th><br>Vote</th>
 *     <th><br>Description</th>
 *   </tr>
 * </thead>
 * <tbody>
 *   <tr>
 *     <td><br>Activation Fusion</td>
 *     <td><br>Y</td>
 *     <td><br>real</td>
 *     <td><br>builds an action that selects each sub-actions basedon their highest utility value (utility = action.getVote * weight).</td>
 *   </tr>
 *   <tr>
 *     <td><br>Command Fusion</td>
 *     <td><br>Y</td>
 *     <td><br>binary</td>
 *     <td><br>builds an action from a sum of all voting sub-actionsscaled by the actions utility: (i.e. velocity += velocity[i] * action.getVote()* w[i])</td>
 *   </tr>
 *   <tr>
 *     <td><br>Highest Activation</td>
 *     <td><br>Y</td>
 *     <td><br>real</td>
 *     <td><br>selects the action with the highest utility value(utility = action.getVote * weight)</td>
 *   </tr>
 *   <tr>
 *     <td><br>Highest Priority</td>
 *     <td><br>Y</td>
 *     <td><br>binary</td>
 *     <td><br>selects the action with the highest priority(weight), (if voted)</td>
 *   </tr>
 *   <tr>
 *     <td><br>Monte Carlo</td>
 *     <td><br>Y</td>
 *     <td><br>real</td>
 *     <td><br>randomly selects actions from a distribution basedon the action's utility (utility = action.getVote * weight)</td>
 *   </tr>
 *   <tr>
 *     <td><br>Priority Fusion</td>
 *     <td><br>Y</td>
 *     <td><br>binary</td>
 *     <td><br>builds an action that selects each sub-action basedon the highest priority (weight), (if voted)</td>
 *   </tr>
 *   <tr>
 *     <td><br>Simple Priority</td>
 *     <td><br>N</td>
 *     <td><br>binary</td>
 *     <td><br>selects the first action that has voted</td>
 *   </tr>
 *   <tr>
 *     <td><br>Utility Fusion</td>
 *     <td><br>N</td>
 *     <td><br>real</td>
 *     <td><br>builds an action that selects each sub-action with <br><br>highest vote: (action.getVote)</td>
 *   </tr>
 * </tbody>
 * </table>
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
			w.add(d/sum);
		}
	}
	public abstract Action evaluate(Collection<Action> ActionSet);
}