package Vehicles;

import framework.SimulationBody;

/**
 * The action includes all of the vehicles actuators.
 *
 * Action components: LeftWheelVelocity, RightWheelVelocity, Pickup, Drop
 *
 * NOTE: Any additions to this require the 4 behaviorFramework
 * fusion arbiters to be updated (ActivationFusion, CommandFusion,
 * PriorityFusion, and UtilityFusion)
 *
 */
public class Action {
    private double leftWheelVelocity; // {-1..1}
    private double rightWheelVelocity; // {-1..1}
    private SimulationBody pickup;
    private boolean drop;

    public Action() {
        leftWheelVelocity = rightWheelVelocity = 0.0;
        pickup = null;
        drop = false;
    }

    public Action(Action a) {
        this.leftWheelVelocity = a.leftWheelVelocity;
        this.rightWheelVelocity = a.rightWheelVelocity;
        this.pickup = a.pickup;
        this.drop = a.drop;
    }

    public void setLeftWheelVelocity(double leftWheelVelocity) {
        if (leftWheelVelocity > 1.0)
            leftWheelVelocity = 1.0;
        if (leftWheelVelocity <-1.0)
            leftWheelVelocity = -1.0;
        this.leftWheelVelocity = leftWheelVelocity;
    }

    public void setRightWheelVelocity(double rightWheelVelocity) {
        if (rightWheelVelocity > 1.0)
            rightWheelVelocity = 1.0;
        if (rightWheelVelocity < -1.0)
            rightWheelVelocity = -1.0;
        this.rightWheelVelocity = rightWheelVelocity;
    }

    public double getLeftWheelVelocity() {
        return leftWheelVelocity;
    }

    public double getRightWheelVelocity() {
        return rightWheelVelocity;
    }

    public void setPickup(SimulationBody pickup) {
        this.pickup = pickup;
    }

    public SimulationBody getPickup() {
        return pickup;
    }

    public void setDrop(boolean p) { drop = p;}

    public boolean getDrop() { return drop; }
}
