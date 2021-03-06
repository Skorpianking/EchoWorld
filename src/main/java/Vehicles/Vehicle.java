package Vehicles;

import framework.SimulationBody;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.World;
import org.dyn4j.world.result.RaycastResult;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * Basic vehicle class.
 *
 */

public class Vehicle extends SimulationBody {

    private final Vector2 leftWheelLocation = new Vector2(-0.5, -0.5);
    private final Vector2 rightWheelLocation = new Vector2( 0.5, -0.5);

    protected final double MAX_VELOCITY = 2; // arbitrary right now
    protected final double MAX_ANGULAR_VELOCITY = 1; // how fast we can turn
    protected final int SENSOR_RANGE = 20; // how far the line casts go
    protected final double ANGULAR_DAMPENING = 0.1;

    protected final double K_p = 10;   // PID: Proportional Control Constant

    private State state;

    // array of values to "sweep" across -- hand jammed to get a reasonable sweep that doesn't eat too much processing time
    // -10 to 120 degrees in steps of 5 degrees (added +/- 7.5, +/- 2.5
    double[] sweepValues = {
            -0.1745, -0.1309, -0.0873, -0.4363, 0.0000, 0.4363, 0.0873, 0.1309, 0.1745, 0.2618, 0.3491, 0.4363, 0.5236, 0.6109,
            0.6981,  0.7854, 0.8727, 0.9599, 1.0472, 1.1344, 1.2217, 1.3090, 1.3962, 1.4833,
            1.5707,  1.6580, 1.7453, 1.8325, 1.9198, 2.0071, 2.0944};

    // Creates the world
    protected World<SimulationBody> myWorld;

    private boolean drawScanLines = false;

    public Vehicle() {
    }

    private void bulkInit(World<SimulationBody> myWorld) {
        this.myWorld = myWorld;

        // Create our vehicle
        this.addFixture(Geometry.createRectangle(1.0, 1.5));
        this.addFixture(Geometry.createCircle(0.35));
        this.setMass(MassType.NORMAL);
        this.setAngularDamping(ANGULAR_DAMPENING);

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

        // -- "wheels"
        Convex leftWheel = Geometry.createRectangle(.33, .5);
        Convex rightWheel = Geometry.createRectangle(.33, .5);
        // if we add these as-is to the body they will both be positioned at the center, so we have to offset them
        leftWheel.translate(leftWheelLocation);
        rightWheel.translate(rightWheelLocation);

        // add to the vehicle -- note: these are like the portholes on a '57 chevy, they are just for looks
        this.addFixture(leftGrabber);
        this.addFixture(rightGrabber);
        this.addFixture(leftSensor);
        this.addFixture(rightSensor);
        this.addFixture(leftWheel);
        this.addFixture(rightWheel);
        this.setColor(Color.CYAN);

/*        // -- "wheels"
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
*/
    }

    public void initialize(World<SimulationBody> myWorld) {
        bulkInit(myWorld);
        state = new State();
    }

    public void initialize(World<SimulationBody> myWorld, State s) {
        bulkInit(myWorld);
        state = s;
    }

    /**
     * Converts sensor data into percepts stored in state. <br>
     * Overload to add additional sensor parsing.
     *
     * @return boolean success/failure
     */
    public boolean sense() {
        state.tick();

        state.setHeading(convertTransformToHeading());

        // Following code block draws Rays out from each sensor and stores returns in state
        rayCasting(-0.50, 0.8, 0); // left sensor
        rayCasting(0.50, 0.8, 1); // right sensor

        state.setVelocity(this.getLinearVelocity()); // LinearVelocity captures heading and speed
        state.setAngularVelocity(this.getAngularVelocity());
        state.updateLightStrengths();

        return true;
    }

    /**
     * Must be overloaded. Current action is no op.<br>
     * Called before render to get action for the current time step.<br>
     *
     * @return return a noop action
     */
    public Action decideAction() { //Graphics2D g) {
        Action result = new Action();

        return result;
    }

    /**
     * Called from render.  Must provide it the coordinates of the specific sensor you want to ray cast
     * from.
     *
     * @param sensor_x
     * @param sensor_y
     * @param sensor_dir
     */
    private void rayCasting(double sensor_x, double sensor_y, int sensor_dir) {
        final double length = SENSOR_RANGE;

        Vector2 start = this.getTransform().getTransformed(new Vector2(sensor_x, sensor_y));
        SensedObject obj;

        for(double i : sweepValues) { //sweepValues) {
            if (sensor_dir == 1) { // Right side, reverse the table.
                i = i * -1;
            }
            Ray ray = new Ray(start,(i + state.getHeading())); //baseVehicle.getLinearVelocity().getDirection()));

            List<RaycastResult<SimulationBody, BodyFixture>> results = myWorld.raycast(ray, length, new DetectFilter<SimulationBody, BodyFixture>(true, true, null));
            for (RaycastResult<SimulationBody, BodyFixture> result : results) {
                // Get the direction between the center of the vehicle and the impact point
                Vector2 heading = new Vector2(this.getWorldCenter(), result.getRaycast().getPoint());
                double angle = heading.getAngleBetween(state.getHeading()); // baseVehicle.getLinearVelocity()); // radians
                double distance = result.getRaycast().getDistance();
                String type = "UNKNOWN";
                if (result.getBody().getUserData() != null) { // If not set, just make Unknown
                    if (result.getBody().getUserData().equals("Light")) {
                        type = "Light";
                    } else {
                        type = "Obstacle";
                    }
                }

                String side = "Left";
                if (sensor_x > 0.0) {
                    side = "Right";
                }

                obj = new SensedObject(heading, angle, distance, type, side, result.getRaycast().getPoint());
                state.addSensedObject(obj);
            }
        }
    }

    /**
     * Override the render loop to allow for drawing additional information.
     * In this case, the sensor scan lines.
     *
     * @param g the graphics object to render to
     * @param scale the scaling factor
     */
    @Override
    public void render(Graphics2D g, double scale) {
        super.render(g, scale);

        if (drawScanLines) {
            // draw the sensor intersections
            final double r = 4.0;
            final double rayScale = scale; // <-- this has to match the world scale, otherwise you get wonky results
            int x, y;
            List<SensedObject> sensedObjects = state.getSensedObjects();
            for (SensedObject obj : sensedObjects) {
                Vector2 point = obj.getHit(); //result.getRaycast().getPoint();
                g.setColor(Color.GREEN);
                g.fill(new Ellipse2D.Double(
                        point.x * rayScale - r * 0.5,
                        point.y * rayScale - r * 0.5,
                        r,
                        r));

                if (obj.getSide() == "Left") {
                    Vector2 v = this.getTransform().getTransformed(new Vector2(-0.5, 0.8));
                    x = (int) (v.x * rayScale);
                    y = (int) (v.y * rayScale);
                    g.setColor(Color.ORANGE);
                    g.drawLine((int) (point.x * rayScale), (int) (point.y * rayScale), x, y);
                }
                if (obj.getSide() == "Right") {
                    Vector2 v = this.getTransform().getTransformed(new Vector2(0.5, 0.8));
                    x = (int) (v.x * rayScale);
                    y = (int) (v.y * rayScale);
                    g.setColor(Color.MAGENTA);
                    g.drawLine((int) (point.x * rayScale), (int) (point.y * rayScale), x, y);
                }
            }
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

        state.setLeftWheelVelocity(left);
        state.setRightWheelVelocity(right);

        // Calculate Torque
        Vector2 applyLeft = new Vector2(0.0, 1.0);
        applyLeft.multiply(left);
        Vector2 applyRight = new Vector2(0.0,1.0);
        applyRight.multiply(right);
        double torqueL = leftWheelLocation.cross(applyLeft);
        double torqueR = rightWheelLocation.cross(applyRight);
        double baseTorque = torqueL + torqueR;

        // Proportional Controller
        double error = baseTorque - this.getAngularVelocity(); // SetPoint - ProcessVariable (e(t) = r(t)-y(t))
        double u = K_p * error; // Control variable

        // Apply Torque
        this.applyTorque(u);

        // System.out.println("Current: " + baseVehicle.getAngularVelocity() + "; Applied: " + u + "; Target: " + baseTorque);

        // Apply the forces in the direction the baseVehicle is facing
        Vector2 baseNormal = this.getTransform().getTransformedR(new Vector2(0.0,1.0));
        baseNormal.multiply(left+right);
        this.setLinearVelocity(baseNormal);

        // Constrain the vehicle to prevent spinning out of control
        // Get the linear velocity in the direction of the baseVehicle's front
        Vector2 clamp = this.getTransform().getTransformedR(new Vector2(0.0, 1.0));
        double defl = this.getLinearVelocity().dot(clamp);

        // clamp the velocity
        defl = Interval.clamp(defl, -MAX_VELOCITY, MAX_VELOCITY);
        if (defl < 0.0)
            this.setLinearVelocity(baseNormal.multiply(-defl));
        else
            this.setLinearVelocity(baseNormal.multiply(defl));

        // clamp the angular velocity
        double av = this.getAngularVelocity();
        av = Interval.clamp(av, -MAX_ANGULAR_VELOCITY, MAX_ANGULAR_VELOCITY);
        this.setAngularVelocity(av);
    }

    /**
     * Converts the baseVehicle's Transform matrix (cos and sin) into a
     * vehicle heading. <br>
     * Matches the return from baseVehicle.getLinearVelocity.getDirection()<br>
     * 0 = x, counter-clockwise +0 to PI, clockwise -0 to PI
     *
     * @return vehicle heading
     */
    public double convertTransformToHeading() {
        double sin = this.getTransform().getSint();
        int ssign = (0>sin)?-1:1;
        double cos = this.getTransform().getCost();
        int csign = (0>cos)?-1:1;

        double asin = Math.asin(sin);
        double acos = Math.acos(cos);
        double dir = Math.PI/2.0;

        if (csign == 1 && ssign == -1) { // Quadrant I
            dir = (Math.PI/2.0) - acos;
        } else if (csign == 1 && ssign == 1) { // Quadrant II
            dir = acos + (Math.PI/2.0);
        } else if (csign == -1 && ssign == -1) { //Quadrant IV
            dir = (Math.PI/2.0) - acos;
        } else if (csign == -1 && ssign == 1) { // Quadrant IV
            dir = -(Math.PI/2.0)-asin;
        }

        return dir;
    }

    /**
     * Turn on sensor scan line drawing for visual debugging purposes.
     * @param val
     */
    void setDrawScanLines(boolean val) {drawScanLines = val;}
}







