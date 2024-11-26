package Sample.behaviors;

import Vehicles.SensedObject;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;
import org.dyn4j.geometry.Rotation;
import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A goto TARGET behavior. <br>
 * Will try to minimize the distance between the vehicle and the
 * nearest TARGET.</p>
 */
public class GotoRelNav extends Behavior {
    private double targetDistance;
    private double targetAngle;

    // Vote = 1
    // Angle limit = 90 deg
    // motor outputs are 0.9 and 0.9 - angle in radians
    // Keeps track of last time seen and moves toward it for 5 ticks

    /**
     * Goto needs to know what type of object to go to.
     *
     * @param target A string name of the object to move toward
     */
    public GotoRelNav(String target) {
        super();
        parseTarget(target);
    }

    public GotoRelNav() { super(); }

    /**
     * Set Goto's target parameter.
     *
     * @param params a single String that is the target type's label
     */
    public void setParameters(ArrayList<String> params) {
        super.setParameters(params);
        parseTarget(params.get(0));
    }

    /**
     *
     * @param target String of two doubles "distance angle"
     */
    private void parseTarget(String target) {
        ArrayList < Double > myDoubles = new ArrayList < Double >();
        Matcher matcher = Pattern.compile( "[-+]?\\d*\\.?\\d+([eE][-+]?\\d+)?" ).matcher( target );

        while ( matcher.find() )
        {
            double element = Double.parseDouble( matcher.group() );
            myDoubles.add( element );
        }

        targetDistance = myDoubles.get(0);
        targetAngle = myDoubles.get(1);
    }

    /**
     * <p>Will identify "Home" in SensedObject. Then calculate an action
     * to move toward the target relative navigation point (distance, angle).</p>
     **
     * <p>Vote = 1 if relative navigation is not 0.0, 0.0.<br>
     * Vote = 0 otherwise.</p>
     *
     * @param state the current state
     * @return an action to move toward the relative navigation point
     */
    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "GotoRelNav" + targetDistance + "," + targetAngle;

        List<SensedObject> sensedObjects = state.getSensedObjects();

        SensedObject bestObj = null;

        // Locate the "Home" Sensed Object
        for (SensedObject obj : sensedObjects) {
            if (obj.getType().equals("Home")) {
                bestObj = obj;
                break;
            }
        }

        double distanceFromHome = 0.0;
        double angleFromHome = 0.0;

        if (bestObj != null) {
            // Convert the sensed information into the relative navigation frame
            // Polar coordinates, Home is center
            distanceFromHome = bestObj.getDistance();
            double angle = bestObj.getAngle();
            double heading = state.getHeading();
            Rotation rotAngle = new Rotation(angle);
            Rotation headingAngle = new Rotation(heading);
            Rotation rotTransformed = rotAngle.getRotated(headingAngle);
            rotTransformed.rotate180();
            angleFromHome = (rotTransformed.toRadians());

/*            double offsetAngle = Math.atan2(Math.sin(angle-heading), Math.cos(angle-heading));
            if (heading > 0.0)
                angleFromHome = Math.PI - (angle - heading);
            else
                angleFromHome = -Math.PI - (angle - heading);
*/

            // Set action that minimizes the current position from the target position
            Vector2 self = Vector2.create(distanceFromHome, angleFromHome);
            Vector2 dest = Vector2.create(targetDistance, targetAngle);

            Vector2 goalVector = new Vector2(self, dest);

            double bestHeading = goalVector.getDirection();
            double headingDelta = bestHeading - heading;
            if (Math.abs(headingDelta) < 0.1) { // Destination is ahead
                action.setRightWheelVelocity(1.0);
                action.setLeftWheelVelocity(1.0);
            } else if (headingDelta > 2.8 || bestHeading < -2.8) { // destination is behind, go left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.0); // - Math.abs(headingDelta/2));
            } else if (headingDelta < 0.0) { // destination is to the right
                action.setRightWheelVelocity(0.05); // - Math.abs(headingDelta/2));
                action.setLeftWheelVelocity(0.9);
            } else if (headingDelta > 0.0) { // destination is to the left
                action.setRightWheelVelocity(0.9);
                action.setLeftWheelVelocity(0.05);
            }

            // As we get closer, slow down
            action.setRightWheelVelocity(action.getRightWheelVelocity() * Math.min(1.0, goalVector.getMagnitude()/4));
            action.setLeftWheelVelocity(action.getLeftWheelVelocity() * Math.min(1.0, goalVector.getMagnitude()/4));

            action.setVote(1);
        }

        return action;
    }

}
