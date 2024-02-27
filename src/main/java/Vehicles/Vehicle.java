package Vehicles;

import framework.SimulationBody;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.joint.WeldJoint;
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

    private Vector2 leftWheelLocation = new Vector2(-0.5, -0.5);
    private Vector2 rightWheelLocation = new Vector2( 0.5, -0.5);
    private Vector2 leftSensorLocation = new Vector2( -0.27, 0.86);
    private Vector2 rightSensorLocation = new Vector2( 0.27, 0.86);
    protected WeldJoint<SimulationBody> gripper;

    protected final double MAX_VELOCITY = 5; // arbitrary right now
    protected final double MAX_ANGULAR_VELOCITY = 2; // how fast we can turn
    protected final int SENSOR_RANGE = 20; // how far the line casts go
    protected final double ANGULAR_DAMPENING = 1;

    protected double K_p = 10;   // PID: Proportional Control Constant
    protected double K_i = 0.0;
    protected double K_d = 0.0;

    protected State state;

    protected Home home;

    protected String name;
    protected String treeDesc;

    protected String lastAction;

    // array of values to "sweep" across -- hand jammed to get a reasonable sweep that doesn't eat too much processing time
    // -10 to 120 degrees in steps of 5 degrees (added +/- 7.5, +/- 2.5
    double[] sweepValues = {
            -0.1745, -0.1309, -0.0873, -0.4363, 0.0000, 0.4363, 0.0873, 0.1309, 0.1745, 0.2618, 0.3491, 0.4363, 0.5236, 0.6109,
            0.6981,  0.7854, 0.8727, 0.9599, 1.0472, 1.1344, 1.2217, 1.3090, 1.3962, 1.4833,
            1.5707,  1.6580, 1.7453, 1.8325, 1.9198, 2.0071, 2.0944};

    // The World the vehicle is placed in
    protected World<SimulationBody> myWorld;

    protected boolean drawScanLines = false;

    protected double energy;
    protected double energyUsage;

    public Vehicle() {
        energy = 100;
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

        // gripper
        gripper = null;
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

    public void initialize(World<SimulationBody> myWorld, String vehicleType) {
        this.myWorld = myWorld;
        if (vehicleType.equals("Ant"))
            initAntVehicle();
        else
            initBraitenbergVehicle();

        state = new State();
    }

    public void initialize(World<SimulationBody> myWorld,  State s, String vehicleType) {
        this.myWorld = myWorld;
        if (vehicleType.equals("Braitenberg"))
            initBraitenbergVehicle();
        else
            initAntVehicle();
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
        rayCasting( leftSensorLocation.x, leftSensorLocation.y, 0); // left sensor
        rayCasting( rightSensorLocation.x, rightSensorLocation.y, 1); // right sensor

        // One scan from front of vehicle
        Vector2 start = this.getTransform().getTransformed(new Vector2(0.0, 0.8));// this.getWorldCenter();
        SensedObject obj;
        Ray ray = new Ray(start,(state.getHeading())); //baseVehicle.getLinearVelocity().getDirection()));

        List<RaycastResult<SimulationBody, BodyFixture>> results = myWorld.raycast(ray, SENSOR_RANGE/2, new DetectFilter<SimulationBody, BodyFixture>(true, true, null));
        for (RaycastResult<SimulationBody, BodyFixture> result : results) {
            // First check if this is our Home and if we are Holding something.
            // If so, do not add it because it is added separately and we don't want to treat it like an obstacle.
            if (home != null) {
                if (result.getBody().getUserData().equals(home.name) && state.isHolding())
                    break;
            }
            // Get the direction between the center of the vehicle and the impact point
            Vector2 heading = new Vector2(this.getWorldCenter(), result.getRaycast().getPoint());
            double angle = heading.getAngleBetween(state.getHeading()); // baseVehicle.getLinearVelocity()); // radians
            double distance = result.getRaycast().getDistance();
            String type = "UNKNOWN";
            if (result.getBody().getUserData() != null) { // If not set, will be UNKNOWN
                type = result.getBody().getUserData().toString();
            }
            if (home != null ) {
                if (type.equals(home.name)) // Not holding anything treat home like an obstacle.
                    type = "Obstacle";
            }
            obj = new SensedObject(heading, angle, distance, type, "Center", result.getRaycast().getPoint());
            if (obj.getType().equals("Food")) {
                obj.setBody(result.getBody());
            }
            state.addSensedObject(obj);
        }

        // Add this vehicle's home to the list of SensedObjects. Also, set 'atHome' if next to it.
        if (home != null)
            state.addSensedObject(senseHome());

        state.setVelocity(this.getLinearVelocity()); // LinearVelocity captures heading and speed
        state.setAngularVelocity(this.getAngularVelocity());

        state.setDeltaPosition(this.getChangeInPosition().getMagnitude()+this.getChangeInOrientation());

        if (gripper != null)
            state.setHolding(true);
        else
            state.setHolding(false);

        //Decaying energy in sense because decideAction will be overloaded.
        energy = energy - (energyUsage * 0.0025);

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
            if (results.size() == 0)
                continue;

            RaycastResult<SimulationBody, BodyFixture> closest=results.get(0);
            for (RaycastResult<SimulationBody, BodyFixture> result : results) {
                if (result.getRaycast().getDistance() < closest.getRaycast().getDistance())
                    closest = result;
            }

                // First check if this is our Home and if we are holding something do not add it because it is added separately.
                if (home != null) {
                    if (closest.getBody().getUserData().equals(home.name) && state.isHolding())
                        break;
                }
                // Get the direction between the center of the vehicle and the impact point
                Vector2 heading = new Vector2(this.getWorldCenter(), closest.getRaycast().getPoint());
                double angle = heading.getAngleBetween(state.getHeading()); // baseVehicle.getLinearVelocity()); // radians
                double distance = closest.getRaycast().getDistance();
                String type = "UNKNOWN";
                if (closest.getBody().getUserData() != null) { // If not set, will be UNKNOWN
                    type = closest.getBody().getUserData().toString();
                }
                if (home != null) {
                    if (type.equals(home.name))
                        type = "Obstacle";
                }

                String side = "Left";
                if (sensor_x > 0.0) {
                    side = "Right";
                }

                obj = new SensedObject(heading, angle, distance, type, side, closest.getRaycast().getPoint());

                // HARDCODE: "Food" is considered an "Obstacle" if already holding something
                // TODO: HARDCODE: Other entities considered as "Obstacle"?
                // Sensing becomes Perception: Situation impacts processing: future work can we create these tags
                if (obj.getType().equals("Food")) {
                    if (state.isHolding()) {
                        obj.setType(new String("Obstacle"));
                    } else
                        // If this is a hit from the center
                        obj.setBody(closest.getBody());
                }

                state.addSensedObject(obj);

        }
    }

    /**
     * Vehicles sense their relationship to their home irrespective of obstacles and distance.\
     *
     * @return SensedObject vehicles home
     */
    private SensedObject senseHome() {
        SensedObject obj;

         double deltaX = home.position.x-this.getTransform().getTranslationX();
        double deltaY = home.position.y-this.getTransform().getTranslationY();
        double angle = Math.atan2(deltaY, deltaX);
        double distance = Math.sqrt((deltaX*deltaX)+(deltaY*deltaY));
        double offsetAngle = Math.atan2(Math.sin(angle-state.getHeading()), Math.cos(angle-state.getHeading()));
        String side = "Left";
        if (offsetAngle < 0.0)
            side = "Right";
        obj = new SensedObject(null, -offsetAngle, distance, "Home", side, home.position);

        // If vehicle is within 3.32m, set atHome
        if (distance <= 3.15) // Was 3.32 for Braitenberg
            state.setAtHome(true);
        else
            state.setAtHome(false);

        return obj;
    }

    /**
     *
     */
    private void senseOtherHomes() {
        Home allHomes;

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

        Vector2 start = this.getTransform().getTransformed(leftSensorLocation);
        g.setColor(Color.black);
        g.drawOval((int)(start.x*scale),(int)(start.y*scale),2,2);
        start = this.getTransform().getTransformed(rightSensorLocation);
        g.drawOval((int)(start.x*scale),(int)(start.y*scale),2,2);

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

                if (obj.getType().equals("Home")) {
                    Vector2 v = this.getTransform().getTranslation();
                    x = (int) (v.x * rayScale);
                    y = (int) (v.y * rayScale);
                    g.setColor(Color.BLUE);
                    g.drawLine((int) (point.x * rayScale), (int) (point.y * rayScale), x, y);
                } else {
                    if (obj.getSide().equals("Left")) {
                        Vector2 v = this.getTransform().getTransformed(leftSensorLocation);
                        x = (int) (v.x * rayScale);
                        y = (int) (v.y * rayScale);
                        g.setColor(Color.ORANGE);
                        g.drawLine((int) (point.x * rayScale), (int) (point.y * rayScale), x, y);
                    }
                    if (obj.getSide().equals("Right")) {
                        Vector2 v = this.getTransform().getTransformed(rightSensorLocation);
                        x = (int) (v.x * rayScale);
                        y = (int) (v.y * rayScale);
                        g.setColor(Color.MAGENTA);
                        g.drawLine((int) (point.x * rayScale), (int) (point.y * rayScale), x, y);
                    }
                }
            }
            Vector2 v = this.getWorldCenter();
            x = (int) (v.x * rayScale);
            y = (int) (v.y * rayScale);
            g.setColor(Color.BLACK);

            Ray ray = new Ray(v, (state.getHeading())); //baseVehicle.getLinearVelocity().getDirection()));
            List<RaycastResult<SimulationBody, BodyFixture>> results = myWorld.raycast(ray, SENSOR_RANGE / 2, new DetectFilter<SimulationBody, BodyFixture>(true, true, null));
            for (RaycastResult<SimulationBody, BodyFixture> result : results) {
                g.drawLine((int) (result.getRaycast().getPoint().x * rayScale), (int) (result.getRaycast().getPoint().y * rayScale), x, y);
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

        state.setLeftWheelVelocity(left*MAX_VELOCITY);
        state.setRightWheelVelocity(right*MAX_VELOCITY);

        if(Double.isNaN(this.getTransform().getTranslationY()))
            System.out.println("NaN");
        if (Math.abs(this.getTransform().getTranslationX()) > 40 || Math.abs(this.getTransform().getTranslationY()) >20)
            System.out.println("Offscreen");
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

        // pickup an object only if not already holding something
        if (a.getPickup() != null) {
            if (this.gripper == null) {

                SimulationBody food = a.getPickup();

                // Am I close enough to pickup the object?
                double dist = this.getTransform().getTranslation().distance(food.getTransform().getTranslation());
                if (dist < 1.5) {
                    // Create a joint between the vehicle and the object, and change the object's mass so we can move it
                    gripper = new WeldJoint(this, food, new Vector2(0.0, 0.75));
                    this.myWorld.addJoint(gripper);
                    food.setMass(MassType.NORMAL);
                    food.getFixture(0).setDensity(0.5); // Density is default to 1.0. This halves the density and mass.
                    food.updateMass();
                    food.setUserData("PickedUpFood"); // This will make it so that other vehicles won't target it
                    state.setHolding(true);
                } else {
                    System.out.println(this.getUserData() + ": Cannot Pickup, too far away!");
                }
            } else {
                System.out.println(this.getUserData() + ": Cannot Pickup. Already holding an object: " + gripper.getBody2().getUserData());
            }
        }

        // drop the object being held
        if (a.getDrop()) {
            if (gripper != null) {
                SimulationBody food = gripper.getBody2();
                this.myWorld.removeJoint(gripper);
                food.setUserData("Garbage"); // Renaming the object for testing.
                food.setAtRest(true);
                food.setMassType(MassType.INFINITE);
                gripper = null;
                //HARDCODED distance from home check here and in Vehicles on food collection.
                //HARDCODED vehicle gains 10 energy from dropoff.
                if (home.position.distance(food.getTransform().getTranslation()) < 3.32) {
                    energy += 10;
                    // Dropoff and pickup path?
                    state.dropAtHome( home );
                }
                // state.setHolding(false);
            } else {
                System.out.println(this.getUserData() + ": Cannot Drop. Not holding anything");
            }
        }
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

    void setHome(Home home) {
        this.home = home;
    }

    double getEnergy() { return this.energy; }

    public String getHomeName() { return this.home.name; }

    /**
     * Provides vehicles status update in a string for logging
     * name, energy, ___
     * @return status String
     */
    public String statusString() {
        String result ="";
        if (home != null) {
            result = new String(this.getUserData() + "," + lastAction + "," + energy + "," + home.name + "," +
                this.getTransform().getTranslationX() + "," + this.getTransform().getTranslationY() + "," +
                state.getHeading() + "," + state.getLeftWheelVelocity() + "," + state.getRightWheelVelocity() + "," +
                state.isHolding() + "," + state.isAtHome() + "," + state.getDeltaPosition()); //+","+treeDesc);
        }
        else {
            result = new String(this.getUserData() + "," + lastAction + "," + energy + "," +
                this.getTransform().getTranslationX() + "," + this.getTransform().getTranslationY() + "," +
                state.getHeading() + "," + state.getLeftWheelVelocity() + "," + state.getRightWheelVelocity() + "," +
                state.isHolding() + "," + state.isAtHome() + "," + state.getDeltaPosition()); //+","+treeDesc);
        }


        return result;
    }

    private void initBraitenbergVehicle() {
        leftWheelLocation = new Vector2(-0.5, -0.5);
        rightWheelLocation = new Vector2( 0.5, -0.5);
        leftSensorLocation = new Vector2( -0.50, 0.80);
        rightSensorLocation = new Vector2( 0.50, 0.80);

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

        // gripper
        gripper = null;
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

    private void initAntVehicle() {
        leftWheelLocation = new Vector2(-0.5, -0.5);
        rightWheelLocation = new Vector2( 0.5, -0.5);
        leftSensorLocation = new Vector2( -0.27, 0.86);
        rightSensorLocation = new Vector2( 0.27, 0.86);

        // Create our vehicle
        this.addFixture(Geometry.createEllipse(1, 1.5));
        this.setMass(MassType.NORMAL);
        this.setAngularDamping(ANGULAR_DAMPENING);

        // -- grabbers
        Convex leftGrabber = Geometry.createRectangle(.1, .2);
        Convex rightGrabber = Geometry.createRectangle(.1, .2);
        leftGrabber.translate(-.22,.75);
        rightGrabber.translate(.22, .75);

        this.addFixture(leftGrabber);
        this.addFixture(rightGrabber);
        this.setColor(Color.CYAN);

        // gripper
        gripper = null;
    }

    private void initSphereVehicle() {

    }
}
