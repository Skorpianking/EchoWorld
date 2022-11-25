package Vehicles;

import framework.SimulationBody;
import org.dyn4j.geometry.Vector2;

public class Home extends SimulationBody {
    String name;
    Vector2 position;
    double resource;
    SimulationBody body; // Just in case we want to modify the body in someway

    public void Step(){
        resource *= 0.98;
        // This could be where we spawn a new Vehicle if needed
    }
}
