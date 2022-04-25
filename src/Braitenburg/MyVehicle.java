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

    public MyVehicle() {
        state = new State();
    }

    public void initialize(World<SimulationBody> myWorld) {
        super.initialize(myWorld);
        baseVehicle.setColor(new Color(160,0,255));
    }

    /**
     * Every vehicle senses the world
     * @param g
     */
    public boolean sense(Graphics2D g){
        // Must update the base sensors first
        boolean result = super.sense(g);
        if (!result)
            return result;

        // Place any other state updating you would like to do here:

        return true;
    }

    /**
     * For the UBF...
     */
    public void decideAction() { //Graphics2D g) {
        super.decideAction();

    }
}
