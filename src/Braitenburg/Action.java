package Braitenburg;

public class Action {
    private double leftWheelVelocity; // {-1..1}
    private double rightWheelVelocity; // {-1..1}
    private boolean grab;

    public Action() {
        leftWheelVelocity = rightWheelVelocity = 0.0;
        grab = false;
    }

    public Action(Action a) {
        this.leftWheelVelocity = a.leftWheelVelocity;
        this.rightWheelVelocity = a.rightWheelVelocity;
        this.grab = a.grab;
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

    public void setGrab(boolean grab) {
        this.grab = grab;
    }

    public double getLeftWheelVelocity() {
        return leftWheelVelocity;
    }

    public double getRightWheelVelocity() {
        return rightWheelVelocity;
    }

    public boolean isGrab() {
        return grab;
    }
}
