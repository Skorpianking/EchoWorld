package Vehicles;

import framework.SimulationBody;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Home extends SimulationBody {
    String name;
    Vector2 position;
    double resource;
    SimulationBody body; // Just in case we want to modify the body in someway
    Vehicles vehicles;

    /**
     * Constructor require link back to World domain to spawn new Vehicles
     * @param v Parent
     */
    public Home(Vehicles v) {
        this.vehicles = v;
    }

    /**
     *
     */
    public void Step(){
        // TODO: Changed Home's resource decay to 0 for testing. Value must be tuned to world
        // resource *= 0.98;
        // If resource exceeds 100, Spawn a new Vehicle
        if (resource >= 100.0) {
            Spawn();
            resource -= 50.0; // Each Vehicle costs 50 energy.
        }
    }

    /**
     * Load a new vehicle
     * Instantiate vehicle
     * Add vehicle to VehicleList
     * Add vehicle to world
     */
    public void Spawn() {
        // TODO: Connect to Knowledge Domain here to get a new vehicle.
        String fileName = "data//Agent3.json";
        JSONVehicle vehicle = new JSONVehicle();
        vehicle.initialize(vehicles.getWorld(), fileName); // Halts on failure, needs world for State initialization

        // Not adding scan lines for new vehicles.
        vehicle.setDrawScanLines(false);

        // Setting new position near Home location... hard coded to be within 6.0m of center
        double xSpawn = 0.0;
        double ySpawn = 0.0;
        while (xSpawn >= -2.0 && xSpawn <= 2.0)
            xSpawn = Math.random()*(2*6.0)-6.0;
        while (ySpawn >= -2.0 && ySpawn <= 2.0)
            ySpawn = Math.random()*(2*6.0)-6.0;

        vehicle.translate(position.x+xSpawn,position.y+ySpawn);

        // Set this vehicles Home.
        vehicle.setHome(this);

        // Need to add the new Vehicle to the myVehicle list.
        vehicles.myVehicles.add(vehicle);
        vehicles.getWorld().addBody(vehicle);
    }
}
