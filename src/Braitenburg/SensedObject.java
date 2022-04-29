package Braitenburg;

import org.dyn4j.geometry.Vector2;

public class SensedObject {
    private Vector2 heading = new Vector2();
    private Vector2 hit = new Vector2(); // Probably shouldn't be here, but is useful for drawing
    private double angle;
    private double distance;
    private String type;

    public SensedObject(Vector2 h, double a, double d, String t, Vector2 h2) {
        heading = h;
        angle = a;
        distance = d;
        type = t;
        hit = h2;
    }

    public double getAngle() {
        return angle;
    }

    public double getDistance() {
        return distance;
    }

    public String getType() {
        return type;
    }

    public Vector2 getHeading() {
        return heading;
    }

    public Vector2 getHit() {
        return hit;
    }
}
