package behaviorFramework;

import behaviorFramework.arbiters.SimplePriority;
import behaviorFramework.behaviors.SittingDuck;
import behaviorFramework.behaviors.Wander;

/**
 * This factory is the main access point for robots using the behaviorFramwork
 * package.  The BehaviorFactory accepts a robot's name or identifier and then
 * retrieves and builds a behavior tree from the persistent behavior.XML file. 
 *  
 * @author Brian Woolley - for use by AFIT/ENG - CSCE623 and CSCE723
 */
public class BehaviorFactory {
	
	// Singleton Object Construction
	private static BehaviorFactory uniqueInstance = new BehaviorFactory();
	private BehaviorFactory() {}
	
	public static BehaviorFactory getInstance () {
		return uniqueInstance;
	}
	
	public synchronized Behavior getMyBehavior(String id) {
		assert (id != null);
		
		if (id.equalsIgnoreCase("myRobot")) {
			ArbitrationUnit arbiter = new SimplePriority();
			CompositeBehavior cb = new CompositeBehavior();

			cb.setArbitrationUnit(arbiter);
			cb.add(new Wander());
			cb.add(new SittingDuck());
			return cb;
		}
		else return new SittingDuck();
	}
}
