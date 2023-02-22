package Vehicles;

import Vehicles.Action;
import Vehicles.State;
import Vehicles.Vehicle;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.Behavior;
import behaviorFramework.CompositeBehavior;

import framework.SimulationBody;
import org.dyn4j.world.World;

public class NetworkVehicle extends Vehicle {
    /**
     * Using our own State object to store our vehicles personal data
     */
    private State state;
    private behaviorFramework.Action action = new behaviorFramework.Action();
    private String name;

    public NetworkVehicle() {
        state = new State();
    }

    /**
     * <p>The intialization method receives
     *
     * @param myWorld the simulation world passed to Vehicle to maintain connection
     */
    public void initialize(World<SimulationBody> myWorld) {
        //TODO: Write network socket initialization
    }

    /**
     * Every vehicle senses the world
     *
     * NetworkVehicle doesn't include an extended state. It is assumed the external
     * agent will maintain what it needs.
     *
     * @return true
     */
    public boolean sense(){
        // Update the base sensors first
        super.sense();

        return true;
    }

    /**
     * Call the networked agent and get an action to execute
     *
     * @return the action to execute
     */
    public Action decideAction() {
        action.clear();

        // Get an action from the networked agent
        //TODO: Write call and return as well as protocol parsing

        System.out.println(name + ": " + action.name + " " + action.getLeftWheelVelocity() + " " + action.getRightWheelVelocity());

        return action;
    }

}
