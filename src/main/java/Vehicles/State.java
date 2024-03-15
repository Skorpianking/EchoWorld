package Vehicles;

import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.List;
import com.github.cliftonlabs.json_simple.*;

/**
 * State object for vehicle's memory
 */
public class State {
    protected ArrayList<SensedObject> sensedObjects = new ArrayList<SensedObject>();
    protected double heading; // radians
    protected Vector2 velocity = new Vector2();
    protected double angularVelocity;
    protected double leftWheelVelocity;
    protected double rightWheelVelocity;
    protected boolean holding = false;
    protected boolean atHome = false;
    protected double deltaPosition;

    // Braitenberg Vehicle specific data (for Love, Fear, Hate)
    protected double leftLightStrength;
    protected double rightLightStrength;
    protected int lightDecayLeft; // These decays provide persistence on light detection
    protected int lightDecayRight;// if a raycast misses between steps.

    public State() {
        leftLightStrength = rightLightStrength = 0.0;
    }

    /**
     * Each clock tick, clear the ray cast detected objects and update
     * LightStrengths.
     */
    public void tick() {
        sensedObjects.clear();
        updateLightStrengths();
    }

    public void setVelocity(Vector2 v) {
        velocity = v;
    }

    /*
     * Returns vehicles current velocity.
     * Call .getDirection() on this to get the current heading
     * (vel.getDirection()*180)/Math.PI will provide heading in degrees
     */
    public Vector2 getVelocity() {
        return velocity;
    }

    public void addSensedObject(SensedObject obj) {
        this.sensedObjects.add(obj);
    }

    public List<SensedObject> getSensedObjects() {
        return sensedObjects;
    }

    public void setRightWheelVelocity(double rightWheelVelocity) {
        this.rightWheelVelocity = rightWheelVelocity;
    }

    public void setLeftWheelVelocity(double leftWheelVelocity) {
        this.leftWheelVelocity = leftWheelVelocity;
    }

    public double getRightWheelVelocity() {
        return rightWheelVelocity;
    }

    public double getLeftWheelVelocity() {
        return leftWheelVelocity;
    }

    public void setHolding(boolean holding) {
        this.holding = holding;
    }

    public boolean isHolding() {
        return holding;
    }

    public void setLeftLightStrength(double leftLightStrength) {
        this.leftLightStrength = leftLightStrength;
    }

    public double getLeftLightStrength() {
        return leftLightStrength;
    }

    public void setRightLightStrength(double rightLightStrength) {
        this.rightLightStrength = rightLightStrength;
    }

    public double getRightLightStrength() {
        return rightLightStrength;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setDeltaPosition(double dPos) {this.deltaPosition = dPos; }

    public double getDeltaPosition() { return deltaPosition; }

    /**
     *
     * Convert 'Light' SensedObjects into a left and right light
     * sensor strength
     */
    public void updateLightStrengths() {
        double length = 20; // This is the same as SENSOR_RANGE in the Vehicle class

        if (lightDecayLeft <= 0)
            leftLightStrength = 0.0;
        if (lightDecayRight <= 0)
            rightLightStrength = 0.0;

        lightDecayLeft--;
        lightDecayRight--;
        for (SensedObject obj : sensedObjects) {
            if (obj.getSide().equals("Right") && obj.getType().equals("Light")) { // Obstacle on right
                rightLightStrength = length - obj.getDistance();
                lightDecayRight = 5;
            } else if (obj.getSide().equals("Left") && obj.getType().equals("Light")) { // Obstacle on left
                leftLightStrength = length - obj.getDistance();
                lightDecayLeft = 5;
            }
        }
    }

    public void setAtHome(boolean aH) { atHome = aH; }

    public boolean isAtHome() { return atHome; }

    public void dropAtHome(Home h) {
        holding = false;
    }

    public JsonObject toJson() {
        /* Since this example matches the JsonObject format we're just using the JsonObject's implementation. */
        final JsonObject json = new JsonObject();

        json.put("heading", String.valueOf(heading));
        json.put("velocity", String.valueOf(velocity));
        json.put("angularVelocity", String.valueOf(angularVelocity));
        json.put("leftWheelVelocity", String.valueOf(leftWheelVelocity));
        json.put("rightWheelVelocity", String.valueOf(rightWheelVelocity));
        json.put("holding", String.valueOf(holding));
        json.put("atHome", String.valueOf(atHome));
        json.put("deltaPosition", String.valueOf(deltaPosition));

        json.put("leftLightStrength", String.valueOf(leftLightStrength));
        json.put("rightLightStrength", String.valueOf(rightLightStrength));

        JsonArray objectsJson = new JsonArray();
        for (SensedObject obj : sensedObjects) {
            objectsJson.addChain(obj.toJson());
        }
        json.put("sensedObjects",objectsJson);

        System.out.println(json.toJson());

        return json;
    }
}
