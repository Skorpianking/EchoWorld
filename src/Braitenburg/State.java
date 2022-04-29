package Braitenburg;

import framework.SimulationBody;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.result.RaycastResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * State object for vehicle's memory
 */
public class State {

    private ArrayList<SensedObject> sensedObjects = new ArrayList<SensedObject>();

    private Vector2 heading = new Vector2();
    private Vector2 velocity = new Vector2();

    /**
     * Each clock tick, clear the ray cast detected objects.
     */
    public void tick() {
        sensedObjects.clear();
    }

    public void setVelocity(Vector2 v) {
        velocity = v;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void addSensedObject(SensedObject obj) {
        this.sensedObjects.add(obj);
    }

    public List<SensedObject> getSensedObjects() {
        return sensedObjects;
    }
}
