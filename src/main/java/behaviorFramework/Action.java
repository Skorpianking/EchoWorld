package behaviorFramework;

/**
 * Action objects are used by behaviors to pass their recommended actions
 * up the behavior framework.  This Action class wrappers an existing one to
 * provide a vote field used by arbiters to select an action for execution
 * or submission to a higher level of the framework.
 * 
 * @author Brian Woolley - for use by AFIT/ENG - CSCE623 and CSCE723
 */
public class Action extends Vehicles.Action {

	public String name;

	//--------------------------------------------------Vote
	private double f_vote = 0.0;
	public double getVote() {
		return f_vote;
	}
	public void setVote(double i) {
		f_vote = i;
	}

	//--------------------------------------------------Clear/Reset
	public void clear() {
		this.setRightWheelVelocity(0.0);
		this.setLeftWheelVelocity(0.0);
		this.setVote(0.0);
	}

	public Action () {
		f_vote = 0.0;
	}

	public Action(Action a) {
		//super(a);
		this.f_vote = a.f_vote;
	}
	private boolean allStopRequested = false;

}