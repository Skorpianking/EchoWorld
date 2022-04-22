package Braitenburg;

import framework.SimulationBody;
import org.dyn4j.world.World;

import java.awt.*;

/**
 * Student template vehicle
 *
 */

public class MyVehicle extends Vehicle {
    private State state;

    public MyVehicle(World<SimulationBody> myWorld) {
        super(myWorld);
        state = new State();
    }

    /**
     * Every vehicle senses the world
     * @param g
     */
    public void sense(Graphics2D g){
        // Must update the base sensors first
        super.sense(g);

        // Place any other state updating you would like to do here:
    }

    /**
     * For the UBF...
     * @param g
     */
    public void decideAction(Graphics2D g) {
        super.decideAction(g);
    }
}
