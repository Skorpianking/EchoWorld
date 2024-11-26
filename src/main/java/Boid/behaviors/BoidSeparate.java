package Boid.behaviors;

import Boid.BoidState;
import Vehicles.SensedObject;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;
import org.dyn4j.geometry.Vector2;

import java.util.List;
import java.util.Map;

/**
 * <p>Causes the vehicle to veer away from obstacles.</p>
 */
public class BoidSeparate extends Behavior {

    /**
     * Activation threshold (distance < DISTANCE_LIMIT)
     */
    private final int DISTANCE_LIMIT = 3;
    // Vote = 1

    /**
     * <p>Turn away from an obstacle if it is too close. <br>
     * Action outputs are for steer and thrust.</p>
     *
     * <p>Vote = 1 if vehicle is within the distance threshold.<br>
     * Vote = 0 otherwise</p>
     *
     *     /**
     *    * Method checks for nearby basicBoids and steers away
     *    *
     *    * @param allDots
     *    * @return
     *
     * @param state current vehicle state
     * @return an action to turn away from obstacles
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        List<SensedObject> sensedObjects = state.getSensedObjects();

        action.name = new String("BoidSeparate");
        action.setVote(0);

        Vector2 SoN = new Vector2();
        int neighborCount = 0;

        // Iterate through our Neighbors and calculate Separate of Neighbor force
        for(Map.Entry<Integer, BoidState.Neighbor> neighbor: ((BoidState)state).neighborList.entrySet() ) {
            if (neighbor.getValue().distance < DISTANCE_LIMIT) {
                neighborCount++;
                Vector2 nVec = Vector2.create(neighbor.getValue().distance, neighbor.getValue().angle);
                nVec.rotate(Math.PI);
                SoN.add(nVec);
            }
        }

        double thrust = state.getVelocity().getMagnitude();
        double steer = state.getHeading();
        if(neighborCount > 0) { // Only if there are neighbors
            SoN.divide(neighborCount);
            thrust = SoN.getMagnitude();
            steer = SoN.getDirection();
//            System.out.println(SoN +", " + thrust + ", " + steer + ", " + state.getHeading());
            action.setVote(1);
        }

        action.setRightWheelVelocity(thrust);
        action.setLeftWheelVelocity(steer);

        return action;
    }
}
