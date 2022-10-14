package Vehicles;

import framework.SimulationBody;
import org.dyn4j.geometry.Vector2;

public class SensedObject {
    private Vector2 heading; // Unused
    private Vector2 hit;  // Not necessary for anything other than drawing
    private double angle;
    private double distance;
    private String type;
    private String side;
    private SimulationBody body;

    /**
     * Create a new instantiated SensedObject
     *
     * @param h Heading
     * @param a Angle
     * @param d Distance
     * @param t Type String
     * @param s Side {Left, Right}
     * @param h2 Scanline Hit location
     */
    public SensedObject(Vector2 h, double a, double d, String t, String s, Vector2 h2) {
        heading = h;
        angle = a;
        distance = d;
        type = t;
        side = s;
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

    public String getSide() { return side; }

    public Vector2 getHit() {
        return hit;
    }

    public SimulationBody getBody() {
        return body;
    }

    public void setBody(SimulationBody body) {
        this.body = body;
    }
}
