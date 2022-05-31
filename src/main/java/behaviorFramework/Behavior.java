package behaviorFramework;

import Braitenburg.State;

import java.util.ArrayList;

/**
 * All Behaviors must return an action based on the state. </cr>
 * If a behavior accepts parameters, overload the constructor
 * to accept the parameters AND create an empty constructor for
 * code that will then call setParameters.
 *
 */
public class Behavior {
	public Action genAction(State state) { return null; }
	public void setParameters(ArrayList<String> params) { }
}

