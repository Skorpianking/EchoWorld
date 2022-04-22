package Braitenburg;

import framework.Graphics2DRenderer;
import framework.SimulationBody;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
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
    private double[] motors = {0.5,0.5}; // how much power the wheels have, capped between 0,1!
    private double leftSpeed = 0;
    private double rightSpeed = 0;

    private double maxVelocity = 2; // arbitrary right now
    private int SENSOR_RANGE = 20; // how far the line casts go
    private int maxTorque = 1; // how fast we can turn
    private int TOLERANCE = 2; // how far "off" the target can be, allows us to home in on a target

    private State state;

    // Update loop settings. Set separate sensing and acting rates
    private int sensingUpdateCounter = 0;
    private int SENSING_UPDATE_RATE = 10;

    private int actingUpdateCounter = 0;
    private int ACTING_UPDATE_RATE = 5;

    // These next items (combined) create the motion. Really, rotate is the important one, drive just
    // pushes the vehicle forward at a constant speed.
    private final AtomicBoolean driveVehicle = new AtomicBoolean(true);
    private final AtomicBoolean rotateVehicle = new AtomicBoolean(true);

    // Creates the world
    World<SimulationBody> myWorld;

    // Testing variables
    boolean FEAR = false;

    public Vehicle(World<SimulationBody> myWorld) {
        this.myWorld = myWorld;

        // Create our vehicle
        baseVehicle = new SimulationBody();
        baseVehicle.addFixture(Geometry.createRectangle(1.0, 1.5));
        baseVehicle.addFixture(Geometry.createCircle(0.35));
        baseVehicle.setMass(MassType.NORMAL);
        myWorld.addBody(baseVehicle);

        // -- "wheels"
        Convex leftWheel = Geometry.createRectangle(.33, .5);
        Convex rightWheel = Geometry.createRectangle(.33, .5);
        // if we add these as-is to the body they will both be positioned at the center, so we have to offset them
        leftWheel.translate(-.5,-.50);
        rightWheel.translate(.5, -.50);

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
        baseVehicle.addFixture(leftWheel);
        baseVehicle.addFixture(rightWheel);
        baseVehicle.addFixture(leftGrabber);
        baseVehicle.addFixture(rightGrabber);
        baseVehicle.addFixture(leftSensor);
        baseVehicle.addFixture(rightSensor);
        baseVehicle.setColor(Color.CYAN);

        //Random rand = new Random();
        int max = 15;
        int min = -15;
        Math.floor(Math.random()*(max-min+1)+min);
        baseVehicle.translate(Math.floor(Math.random()*(max-min+1)+min),Math.floor(Math.random()*(max-min+1)+min));

       // baseVehicle.setMass(new Mass(baseVehicle.getWorldCenter(),0.5,0.5)); // work in progress
        state = new State();
    }

    /**
     *
     */
    public void sense(Graphics2D g) {
        if ((sensingUpdateCounter++)%SENSING_UPDATE_RATE != 0)
            return;

        state.tick();
        // Following code block draws Rays out from each sensor and stores data into objectsDetected
        rayCasting(g, -0.50, 0.8, 0); // left sensor
        rayCasting(g, 0.50, 0.8, 1); // right sensor
    }

    /**
     * Must be called before render to show action in the current time step.
     *
     * I anticipate that this will become the "intelligence" function
     *
     */
    public void decideAction(Graphics2D g) {
        // add intelligent code here -- for now, we are just doing Braitenberg things

        if ((actingUpdateCounter++)%ACTING_UPDATE_RATE != 0)
            return;

        // The objectsDetected array contains all the detected objects for this time step.
        // As we iterate through the array, we can bias the vehicles direction
        double angle = 0;
        int i = 0;
        // Cheesing this right now to make fewer code changes - just asking the state for the entire list
        Set<SimulationBody> objectsDetected = state.getAllDetectedObjects();

        for (SimulationBody obj : objectsDetected) {
            if (obj instanceof Light) {
                // Get the vector between the target and the vehicle's center of mass.  We also need
                // the angle to be able to apply the right torque.
                Vector2 headed = new Vector2(baseVehicle.getWorldCenter(), ((SimulationBody)obj).getWorldCenter());
                angle = headed.getAngleBetween(baseVehicle.getLinearVelocity()); // radians
                angle = angle * 180 / Math.PI; // degrees

                // Get angles from respective "sensors" -- since this isn't something dyn4j will let you do, we
                // have to make an assumption.  If the sensors do not overlap, then we can say that the angle
                // returned correlates to the side of the sensor.  Example. Positive angle (> 2) means the right side
                // sensor detected the object, negative angle (<2) indicates the left side sensor detected it.
                // getMagnitude() tells me how "far" the object is away from center of mass, this could help
                // with any type of intensity settings for light, e.g. closer = stronger pull, or stronger resistance
                //System.out.println("angle: " + angle);
                //System.out.println("magnitude: " + headed.getMagnitude());

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
                Vector2 headed = new Vector2(baseVehicle.getWorldCenter(), ((SimulationBody)obj).getWorldCenter());
                angle = headed.getAngleBetween(baseVehicle.getLinearVelocity()); // radians
                angle = angle * 180 / Math.PI; // degrees

                // Get angles from respective "sensors" -- since this isn't something dyn4j will let you do, we
                // have to make an assumption.  If the sensors do not overlap, then we can say that the angle
                // returned correlates to the side of the sensor.  Example. Positive angle (> 2) means the right side
                // sensor detected the object, negative angle (<2) indicates the left side sensor detected it.
                // getMagnitude() tells me how "far" the object is away from center of mass, this could help
                // with any type of intensity settings for light, e.g. closer = stronger pull, or stronger resistance
                //System.out.println("angle: " + angle);
                //System.out.println("magnitude: " + headed.getMagnitude());

                if(angle > 0 && angle < 180 && headed.getMagnitude() < 5) {
                    // Roughly:  right = motors[right]*headed.magnitude)
                    baseVehicle.applyTorque(-Math.PI/4);
                }
                else if(angle < 0 && angle > -180 && headed.getMagnitude() < 5) {
                    // Roughly:  right = motors[left]*headed.magnitude)
                    baseVehicle.applyTorque(Math.PI/4);
                }
            }

            // -- Right now ignore other objects, although this allows for obstacle avoidance in the future.
            // And, fun fact, dyn4j already allows rays to be blocked by other objects which is pretty cool.
            i++;
        }
        // System.out.println("I made a decision");
    }

    /**
     * Called from render.  Must provide it the coordinates of the specific sensor you want to ray cast
     * from. -- still very much a work in progress.
     *
     * @param g
     * @param sensor_x
     * @param sensor_y
     * @param sensor_dir
     */
    private void rayCasting(Graphics2D g, double sensor_x, double sensor_y, int sensor_dir) {
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
                // draw the intersection
                //Vector2 point = result.getRaycast().getPoint();
             //   g.setColor(Color.GREEN);
             //   g.fill(new Ellipse2D.Double(
             //           point.x * rayScale - r * 0.5,
             //           point.y * rayScale - r * 0.5,
             //           r,
             //           r));
                // objectsDetected.add(result.getBody());
                // detectingRay.add(ray);
                state.addDetectedObject(result.getBody(),ray);
            }
        }
    }

    @Override
    public void render(Graphics2D g, double scale) {
        super.render(g, scale);
        // Following code block moves the vehicle
        if (this.driveVehicle.get()) {
            Vector2 normal1 = this.baseVehicle.getTransform().getTransformedR(new Vector2(0.0, 1.0));
            normal1.multiply(maxVelocity); // add to our position
            baseVehicle.applyForce(normal1);
        }

        // make sure the linear velocity is already in the direction of the tank front
        Vector2 normal = this.baseVehicle.getTransform().getTransformedR(new Vector2(0.0, 1.0));
        double defl = baseVehicle.getLinearVelocity().dot(normal);

        // clamp the velocity
        defl = Interval.clamp(defl, 0.5, maxVelocity);
        baseVehicle.setLinearVelocity(normal.multiply(defl));

        // clamp the angular velocity
        double av = baseVehicle.getAngularVelocity();
        av = Interval.clamp(av, -maxTorque, maxTorque);
        baseVehicle.setAngularVelocity(av);

        // reset detected objects array
        // objectsDetected.clear();
    }

}







