package Braitenburg;

import framework.SimulationBody;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.joint.WeldJoint;
import org.dyn4j.geometry.*;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic vehicle class.
 *
 * David King
 * Started:  8 Apr 2022
 * Last Update:  20 Apr
 */

public class Vehicle extends SimulationBody {
    // Basic vehicle build
    private SimulationBody baseVehicle;
    private SimulationBody leftWheel;
    private SimulationBody rightWheel;

    private Vector2 leftWheelLocation = new Vector2(-0.5, -0.5);
    private Vector2 rightWheelLocation = new Vector2( 0.5, -0.5);

    private double MAX_VELOCITY = 2; // arbitrary right now
    private int MAX_TORQUE = 1; // how fast we can turn
    private int SENSOR_RANGE = 20; // how far the line casts go
    private int TOLERANCE = 0; // how far "off" the target can be, allows us to home in on a target

    private State state;

    // Creates the world
    protected World<SimulationBody> myWorld;

    // Testing variables
    boolean FEAR = false;
    private double[] motors = {0.5,0.5}; // how much power the wheels have, capped between 0,1!

    public Vehicle() {
    }

    private void bulkInit(World<SimulationBody> myWorld) {
        this.myWorld = myWorld;

        // Create our vehicle
        baseVehicle = new SimulationBody();
        baseVehicle.addFixture(Geometry.createRectangle(1.0, 1.5));
        baseVehicle.addFixture(Geometry.createCircle(0.35));
        baseVehicle.setMass(MassType.NORMAL);
        myWorld.addBody(baseVehicle);

        // -- grabbers
        Convex leftGrabber = Geometry.createRectangle(.1, .2);
        Convex rightGrabber = Geometry.createRectangle(.1, .2);
        leftGrabber.translate(-.25,.8);
        rightGrabber.translate(.25, .8);

        // -- sensors
        Convex leftSensor = Geometry.createCircle(0.1);
        Convex rightSensor = Geometry.createCircle(0.1);
        leftSensor.translate(-.50,.8);
        rightSensor.translate(.50, .8);

        // add to the vehicle -- note: these are like the portholes on a '57 chevy, they are just for looks
        baseVehicle.addFixture(leftGrabber);
        baseVehicle.addFixture(rightGrabber);
        baseVehicle.addFixture(leftSensor);
        baseVehicle.addFixture(rightSensor);
        baseVehicle.setColor(Color.CYAN);

        // -- "wheels"
        leftWheel = new SimulationBody();
        leftWheel.addFixture(Geometry.createRectangle(.33, .5));
        rightWheel = new SimulationBody();
        rightWheel.addFixture(Geometry.createRectangle(.33, .5));
        // if we add these as-is to the body they will both be positioned at the center, so we have to offset them
        leftWheel.translate(-.5,-.50);
        rightWheel.translate(.5, -.50);
        // Set their mass to be light so that they move with the vehicle
        leftWheel.setMass(MassType.NORMAL);
        rightWheel.setMass(MassType.NORMAL);

        myWorld.addBody(leftWheel);
        myWorld.addBody(rightWheel);

        // Add weld joints between wheels and body so that everything moves together
        WeldJoint<SimulationBody> lw = new WeldJoint<>(baseVehicle, leftWheel, leftWheelLocation);
        this.myWorld.addJoint(lw);

        WeldJoint<SimulationBody> rw = new WeldJoint<>(baseVehicle, rightWheel, rightWheelLocation);
        this.myWorld.addJoint(rw);

        //Random rand = new Random();
        int max = 15;
        int min = -15;
        Math.floor(Math.random()*(max-min+1)+min);
        baseVehicle.translate(Math.floor(Math.random()*(max-min+1)+min),Math.floor(Math.random()*(max-min+1)+min));
    }

    public void initialize(World<SimulationBody> myWorld) {
        bulkInit(myWorld);
        state = new State();
    }

    public void initialize(World<SimulationBody> myWorld, State s) {
       // baseVehicle.setMass(new Mass(baseVehicle.getWorldCenter(),0.5,0.5)); // work in progress
        bulkInit(myWorld);
        state = s;
    }

    /**
     *
     */
    public boolean sense() {
        state.tick();
        // Following code block draws Rays out from each sensor and stores data into objectsDetected
        rayCasting(-0.50, 0.8, 0); // left sensor
        rayCasting(0.50, 0.8, 1); // right sensor

//        state.setHeading(baseVehicle.getWorldCenter()); // WorldCenter is the center of the vehicle
        state.setVelocity(baseVehicle.getLinearVelocity()); // LinearVelocity captures heading and speed

        return true;
    }

    /**
     * Must be called before render to show action in the current time step.
     *
     * I anticipate that this will become the "intelligence" function
     *
     */
    public Action decideAction() { //Graphics2D g) {
        Action result = new Action();
        // add intelligent code here -- for now, we are just doing Braitenberg things


        if (true) // Basically causing the entire block to get skipped. Just saving it in case we need it later....
            return result;

        // The objectsDetected array contains all the detected objects for this time step.
        // As we iterate through the array, we can bias the vehicles direction
        double angle = 0;
        int i = 0;
        List<SensedObject> sensedObjects = state.getSensedObjects();

        for (SensedObject obj : sensedObjects) {
            if(obj.getType().equals("Light") ) {
                angle = obj.getAngle()* 180 / Math.PI;;// conversion from radians to degrees

                // Get the vector between the target and the vehicle's center of mass.  We also need
                // the angle to be able to apply the right torque.
/*                Vector2 headed = new Vector2(baseVehicle.getWorldCenter(), obj.getHit()); //((SimulationBody)obj).getWorldCenter());
                angle = headed.getAngleBetween(baseVehicle.getLinearVelocity()); // radians
                angle = angle * 180 / Math.PI; // degrees
*/
                // Get angles from respective "sensors" -- since this isn't something dyn4j will let you do, we
                // have to make an assumption.  If the sensors do not overlap, then we can say that the angle
                // returned correlates to the side of the sensor.  Example. Positive angle (> 2) means the right side
                // sensor detected the object, negative angle (<2) indicates the left side sensor detected it.
                // getMagnitude() tells me how "far" the object is away from center of mass, this could help
                // with any type of intensity settings for light, e.g. closer = stronger pull, or stronger resistance
                //System.out.println("angle: " + angle);
                //System.out.println("magnitude: " + headed.getMagnitude());

                Vector2 headed = obj.getHeading();
               if(angle > TOLERANCE) {
                    // Roughly:  right = motors[right]*headed.magnitude)
                    //System.out.println("Turn right");
                    if(!FEAR)
                        baseVehicle.applyTorque(-motors[0]*headed.getMagnitude());
                    else
                        baseVehicle.applyTorque(motors[0]*headed.getMagnitude());
                }
                else if(angle < -TOLERANCE) {
                    // Roughly:  right = motors[left]*headed.magnitude)
                    //System.out.println("Turn left");
                    if(!FEAR)
                        baseVehicle.applyTorque(motors[0]*headed.getMagnitude());
                    else
                        baseVehicle.applyTorque(-motors[0]*headed.getMagnitude());
                }

                baseVehicle.setLinearVelocity(headed.add(baseVehicle.getLinearVelocity()));
            }
            else {
                // -- Ideally this would be some type of small vector added to the overall vehicle's movement
                // this would prevent fighting between going towards/away from a light source and the obstacle
                // itself.  Also, ideally, we would only care if the obstacle is truly in the way, like a collision.

                // This is where we want to add some deflection (obstacle avoidance code)
                // Get the vector between the target and the vehicle's center of mass.  We also need
                // the angle to be able to apply the right torque.
/*                Vector2 headed = new Vector2(baseVehicle.getWorldCenter(), ((SimulationBody)obj).getWorldCenter());
                angle = headed.getAngleBetween(baseVehicle.getLinearVelocity()); // radians
                angle = angle * 180 / Math.PI; // degrees
*/
                Vector2 heading = obj.getHeading();
                // Get angles from respective "sensors" -- since this isn't something dyn4j will let you do, we
                // have to make an assumption.  If the sensors do not overlap, then we can say that the angle
                // returned correlates to the side of the sensor.  Example. Positive angle (> 2) means the right side
                // sensor detected the object, negative angle (<2) indicates the left side sensor detected it.
                // getMagnitude() tells me how "far" the object is away from center of mass, this could help
                // with any type of intensity settings for light, e.g. closer = stronger pull, or stronger resistance
                //System.out.println("angle: " + angle);
                //System.out.println("magnitude: " + headed.getMagnitude());

                if(angle > 0 && angle < 180 && heading.getMagnitude() < 5) {
                    // Roughly:  right = motors[right]*headed.magnitude)
                    baseVehicle.applyTorque(-Math.PI/4);
                }
                else if(angle < 0 && angle > -180 && heading.getMagnitude() < 5) {
                    // Roughly:  right = motors[left]*headed.magnitude)
                    baseVehicle.applyTorque(Math.PI/4);
                }
            }

            // -- Right now ignore other objects, although this allows for obstacle avoidance in the future.
            // And, fun fact, dyn4j already allows rays to be blocked by other objects which is pretty cool.
            i++;
        }
        // System.out.println("I made a decision");
        return result;
    }

    /**
     * Called from render.  Must provide it the coordinates of the specific sensor you want to ray cast
     * from. -- still very much a work in progress.
     *
     * @param sensor_x
     * @param sensor_y
     * @param sensor_dir
     */
    private void rayCasting(double sensor_x, double sensor_y, int sensor_dir) {
        final double r = 4.0;
        final double length = SENSOR_RANGE;
        final double rayScale = 20;//48; // <-- this has to match the world scale, otherwise you get wonky results

        Vector2 start = baseVehicle.getTransform().getTransformed(new Vector2(sensor_x, sensor_y));

        // array of values to "sweep" across -- hand jammed to get a reasonable sweep that doesn't eat too much processing time
        double[] sweepValues = {0.0, 0.0001, 0.0010, 0.0020, 0.0040, 0.0060, 0.0080, 0.0100, 0.0110, 0.0120, 0.0140, 0.0160, 0.0180,
        0.0200, 0.0220, 0.240, 0.260, 0.280, 0.300, 0.320, 0.340, 0.360, 0.380, 0.400, 0.420, 0.440, 0.460, 0.480, 0.500,
        0.0520, 0.0540, 0.560, 0.580, 0.600, 0.620, 0.640, 0.660, 0.680, 0.700, 0.720, 0.740, 0.760, 0.780, 0.800, 0.820,
        0.0840, 0.0860, 0.880, 0.900, 0.920, 0.940, 0.960, 0.980, 1.00};

        double x = 0.0;
        double y = 0.01;
        SensedObject obj;
        for(double i : sweepValues) { //sweepValues) {
            // +- i is determined by directionality... so, we will add that as a temp variable until I discuss
            // some of these choices with Bert.
            double j = i;
            if(sensor_dir < 1) {
                j = i * -1;
            }
            Vector2 direction = baseVehicle.getTransform().getTransformedR(new Vector2(x + j, y - i));
            Ray ray = new Ray(start, direction);

            List<RaycastResult<SimulationBody, BodyFixture>> results = myWorld.raycast(ray, length, new DetectFilter<SimulationBody, BodyFixture>(true, true, null));
            for (RaycastResult<SimulationBody, BodyFixture> result : results) {
                // Get the direction between the center of the vehicle and the impact point
                Vector2 heading = new Vector2(baseVehicle.getWorldCenter(), result.getRaycast().getPoint());
                double angle = heading.getAngleBetween(baseVehicle.getLinearVelocity()); // radians
                double distance = result.getRaycast().getDistance();
                String type;
                if (result.getBody() instanceof Light) { // TODO: DAVE: Has to be a better way to do the light versus other object tagging
                    type = new String("Light");
                } else {
                    type = new String("Obstacle");
                }

                obj = new SensedObject(heading, angle, distance, type, result.getRaycast().getPoint());
                state.addSensedObject(obj);
            }
        }
    }

    @Override
    public void render(Graphics2D g, double scale) {
        super.render(g, scale);

        // draw the sensor intersections
        final double r = 4.0;
        final double length = SENSOR_RANGE;
        final double rayScale = 20;//48; // <-- this has to match the world scale, otherwise you get wonky results
        List<SensedObject> sensedObjects = state.getSensedObjects();
        for (SensedObject obj : sensedObjects) {
                Vector2 point = obj.getHit(); //result.getRaycast().getPoint();
                g.setColor(Color.GREEN);
                g.fill(new Ellipse2D.Double(
                        point.x * rayScale - r * 0.5,
                        point.y * rayScale - r * 0.5,
                        r,
                        r));
        }
    }

    /**
     *  Applies the action to the vehicle. Calculates the torque and force from the left and right wheel velocity
     *  requests and then clamps them into a performance range.
     *
     * @param a Action being applied to the vehicle
     */
    public void act(Action a) {
        double left = a.getLeftWheelVelocity();
        double right = a.getRightWheelVelocity();

        // Calculate Torque
        Vector2 applyLeft = new Vector2(0.0, 1.0);
        applyLeft.multiply(left);
        Vector2 applyRight = new Vector2(0.0,1.0);
        applyRight.multiply(right);
        double torqueL = leftWheelLocation.cross(applyLeft);
        double torqueR = rightWheelLocation.cross(applyRight);

        System.out.println("Left Torque: " + torqueL + " Right Torque: " + torqueR);

        // Apply torque to baseVehicle
        double baseTorque = torqueL + torqueR;
        baseVehicle.applyTorque(baseTorque);

        // Constrain the vehicle to prevent spinning out of control
        // Apply the forces in the direction the baseVehicle is facing
        Vector2 baseNormal = baseVehicle.getTransform().getTransformedR(new Vector2(0.0,1.0));
        baseNormal.multiply(left+right);
        System.out.println(baseNormal);
        baseVehicle.setLinearVelocity(baseNormal);

        // Get the linear velocity in the direction of the baseVehicle's front
        Vector2 clamp = this.baseVehicle.getTransform().getTransformedR(new Vector2(0.0, 1.0));
        double defl = baseVehicle.getLinearVelocity().dot(clamp);

        // clamp the velocity
        defl = Interval.clamp(defl, 0.0, MAX_VELOCITY);
        baseVehicle.setLinearVelocity(baseNormal.multiply(defl));

        // clamp the angular velocity
        double av = baseVehicle.getAngularVelocity();
        av = Interval.clamp(av, -MAX_TORQUE, MAX_TORQUE);
        baseVehicle.setAngularVelocity(av);
    }

    /**
     * Set the color of the vehicle
     *
     * @param c Color
     */
    public void setColor(Color c) {
        baseVehicle.setColor(c);
    }
}







