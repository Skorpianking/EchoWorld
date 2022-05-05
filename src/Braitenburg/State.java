package Braitenburg;

import org.dyn4j.geometry.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * State object for vehicle's memory
 */
public class State {

    private ArrayList<SensedObject> sensedObjects = new ArrayList<SensedObject>();
    private Vector2 velocity = new Vector2();
    private double leftWheelVelocity;
    private double rightWheelVelocity;
    private boolean holding;


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
}
