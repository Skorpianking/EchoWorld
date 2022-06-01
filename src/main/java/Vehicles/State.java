package Vehicles;

import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * State object for vehicle's memory
 */
public class State {
    private ArrayList<SensedObject> sensedObjects = new ArrayList<SensedObject>();
    private double heading;
    private Vector2 velocity = new Vector2();
    private double angularVelocity;
    private double leftWheelVelocity;
    private double rightWheelVelocity;
    private boolean holding;

    private double leftLightStrength;
    private double rightLightStrength;
    private int lightDecayLeft;
    private int lightDecayRight;

    public void State() {
        leftLightStrength = rightLightStrength = 0.0;
    }

    /**
     * Each clock tick, clear the ray cast detected objects.
     */
    public void tick() {
        sensedObjects.clear();
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
            if (obj.getSide() == "Right" && obj.getType() == "Light") { // Obstacle on right
                rightLightStrength = length - obj.getDistance();
                lightDecayRight = 5;
            } else if (obj.getSide() == "Left" && obj.getType() == "Light") { // Obstacle on left
                leftLightStrength = length - obj.getDistance();
                lightDecayLeft = 5;
            }
        }
    }


}
