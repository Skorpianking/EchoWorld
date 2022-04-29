package Sample;

import Braitenburg.Action;
import Braitenburg.Light;
import Braitenburg.SensedObject;
import Braitenburg.Vehicle;
import framework.SimulationBody;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Interval;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Sample Vehicle
 *
 */

public class MyVehicle extends Vehicle {
    private MyState state;

    public MyVehicle() {
        state = new MyState();
    }

    public void initialize(World<SimulationBody> myWorld) {
        super.initialize(myWorld, state);
        setColor(new Color(160,0,255));
    }

    /**
     * Every vehicle senses the world
     */
    public boolean sense(){
        // Must update the base sensors first
        boolean result = super.sense();
        if (!result)
            return result;

        // Place any other state updating you would like to do here:
        state.setMyMemory(6);

        return true;
    }

    /**
     * For the UBF...
     */
    public Action decideAction() { //Graphics2D g) {
        Action result =  new Action();
        double angle;
        List<SensedObject> sensedObjects = state.getSensedObjects();

        for (SensedObject obj : sensedObjects) {
            if(obj.getType().equals("Light") ) {
                angle = obj.getAngle()* 180 / Math.PI;;// conversion from radians to degrees
                state.getVelocity();
/*                baseVehicle.applyTorque(0.1); // left
                Vector2 normal = baseVehicle.getTransform().getTransformedR(new Vector2(0.0, 1.0));
                normal.multiply(5);
                baseVehicle.applyForce(normal);
*/
//                System.out.println("Velocity: " + state.getVelocity() + " Angle: " + angle);
            }
        }

        result.setLeftWheelVelocity(1);
        result.setRightWheelVelocity(1);

        return result;
    }
}

