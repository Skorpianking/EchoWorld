package Sample.behaviors;

import Vehicles.SensedObject;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A Pick Up TARGET behavior. <br>
 * If there is a TARGET object directly in front, it will
 * output an action to pickup the object.</p>
 */
public class PickUp extends Behavior {
    private final double DISTANCE_TO_PICKUP = 0.3;       // Braitenburg = 0.4
    private final double ANGLE_CONSIDERED_CENTER = 0.09; // Braitenburg = 15.0
    private String target;

    /**
     * Pickup needs to know what type of object to pickup.
     * Otherwise, it will pickup anything it encounters.
     *
     * @param target A string name of the object to move toward
     */
    public PickUp(String target) {
        super();
        this.target = target;
    }
    public PickUp() {
        super();
    }

    /**
     * Set Pickup's target parameter.
     *
     * @param params a single String that is the target type's label
     */
    public void setParameters(ArrayList<String> params) {
        super.setParameters(params);
        this.target = params.get(0);
    }

    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "PickUp";

        List<SensedObject> sensedObjects = state.getSensedObjects();
        double angle = 0;
        SensedObject bestObj = new SensedObject(null, 0, 1000, null, null, null);

        for (SensedObject obj : sensedObjects) {
            angle = (obj.getAngle() * 180) / Math.PI; // conversion from radians to degrees

            // Locate "Food" and return the hit closest to the centerline of the vehicle
            if (obj.getType().equals(target)) {
                // If this is a hit from the center
                if ( angle > -ANGLE_CONSIDERED_CENTER && angle < ANGLE_CONSIDERED_CENTER ) {
                    // And it is the closest
                    if (obj.getDistance() < bestObj.getDistance())
                        bestObj = obj;
                }
            }
        }

        // We will pickup the food if it is within 1.0 meter, and is in the middle (not on the sides)
        if (bestObj.getDistance() <= DISTANCE_TO_PICKUP) {
            //System.out.println("Pick It UP!!!!!");
            action.setPickup(bestObj.getBody());
            action.setVote(1);
        }

        return action;
    }
}
