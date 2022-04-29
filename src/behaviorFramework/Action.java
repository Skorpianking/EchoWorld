package behaviorFramework;

/**
 * Action objects are used by behaviors to pass their recommended actions
 * up the behavior framework.  The vote field is used by arbiters to select
 * an action for execution or submission to a higher level of the framework.
 * 
 * @author Brian Woolley - for use by AFIT/ENG - CSCE623 and CSCE723
 */
public class Action extends Braitenburg.Action {
	
	//--------------------------------------------------Vote
	private double f_vote = 0.0;
	public double getVote() {
		return f_vote;
	}
	public void setVote(double i) {
		f_vote = i;
	}

	//--------------------------------------------------Misc Actions
	private boolean allStopRequested = false;

	public boolean isAllStopSet() {
		return allStopRequested;
	}

	public void setAllStop() {
		allStopRequested = true;		
	}

	/**
	 *
	 */
	public void execute() { // AdvancedRobot advRobot) {

	}
}