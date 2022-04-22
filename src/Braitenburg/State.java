package Braitenburg;

import framework.SimulationBody;
import org.dyn4j.geometry.Ray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * State object for vehicle's memory
 */
public class State {

    // Using set to hold detectedObjects, set guarantees we will not have duplicates
    private Set<SimulationBody> objectsDetected = new HashSet<SimulationBody>();
    private ArrayList<Ray> detectingRay = new ArrayList<Ray>();

    /**
     * Each clock tick, clear the ray cast detected objects.
     */
    public void tick() {
        objectsDetected.clear();
        detectingRay.clear();
    }

    /**
     * An object has been seen, update the state
     * @param obj
     * @param ray
     */
    public void addDetectedObject(SimulationBody obj, Ray ray) {
        objectsDetected.add(obj);
        detectingRay.add(ray);
    }

    /**
     *
     */
    public Set<SimulationBody> getAllDetectedObjects() {
        return objectsDetected;
    }
}
