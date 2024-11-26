package Vehicles;

import framework.SimulationBody;
import org.dyn4j.geometry.Vector2;
import java.util.Arrays;

public class Home extends SimulationBody {
    String name;
    Vector2 position;
    double energy;
    SimulationBody body; // Just in case we want to modify the body in someway
    Vehicles vehicles;
    int[] pathStore;
    private int vehicleCount;

    /**
     * Constructor require link back to World domain to spawn new Vehicles
     * @param v Parent
     */
    public Home(Vehicles v) {
        this.vehicles = v;
        pathStore = new int[50];
        Arrays.fill(pathStore,9);
    }

    /**
     *
     */
    public boolean Step(){
        // TODO: Changed Home's energy decay to 0 for testing. Value must be tuned to world
        // energy *= 0.99;
        // If energy exceeds 100, Spawn a new Vehicle
        if (energy >= 100.0) {
            Spawn();
            energy -= 50.0; // Each Vehicle costs 50 energy.
        }

        vehicleCount = 0;
        for(SimulationBody v:vehicles.myVehicles){
            if (name.equals(((Vehicle)v).getHomeName()) && !v.getUserData().equals("Dead")) {
                vehicleCount++;
            }
        }
        if (vehicleCount ==0) {
            System.out.println("COLONY COLLAPSE!");
            System.out.println("Timestep: " + this.vehicles.timestep);
            return false;
        }

        return true;
    }

    /**
     * Load a new vehicle
     * Instantiate vehicle
     * Add vehicle to VehicleList
     * Add vehicle to world
     */
    public void Spawn() {

        // TODO: Connect to Knowledge Domain here to get a new vehicle.
        String fileName = "data//Agent4.json";
        JSONVehicle vehicle =null;
        boolean reused = false;

        for (SimulationBody v: vehicles.myVehicles) {
            if (v.getUserData().equals("Dead")) {
                vehicle = ((JSONVehicle)v); // WARNING: If Spawning, all vehicles assumed to be JSON.
                reused = true;
                vehicle.energy = 100.0;
                vehicle.translateToOrigin();
                break;
            }
        }
        if (vehicle == null && vehicles.myVehicles.size() <= vehicles.MAX_VEHICLE_COUNT)
            vehicle = new JSONVehicle();
        if (vehicle == null) // No dead Vehicles and reached max number don't spawn
            return;

        vehicle.initialize(vehicles, fileName, "Ant"); // Halts on failure, needs world for State initialization

        // Not adding scan lines for new vehicles.
        vehicle.setDrawScanLines(false);

        //TODO: Will need an identifier from KDWorld
        String newName = vehicle.getUserData() + Integer.toString(vehicles.timestep);
        vehicle.setUserData(newName);

        // Setting new position near Home location...
        double distance = 2.6;
        double rotation = 0.0;
        rotation = Math.random()*(2*Math.PI)-Math.PI;

        vehicle.translate(distance, 0.0);
        vehicle.rotate(rotation, position.x, position.y);
        vehicle.setEnabled(true); // reactivate being part of collisions

        // Set this vehicles Home.
        vehicle.setHome(this);

        // Need to add the new Vehicle to the myVehicle list.
        if (!reused) {
            vehicles.myVehicles.add(vehicle);
            vehicles.getWorld().addBody(vehicle);
        }
    }

    /**
     * Provides a comma delimited string for logging on this home's
     * name, energy, and vehicle count
     *
     * @return status String
     */
    public String statusString() {
        String result = new String(name+","+energy+","+vehicleCount+","+position.x+","+position.y);

        return result;
    }

    public int[] receivePath(int [] incomingPath) {
        int [] sendPath;
        sendPath = Arrays.copyOf(pathStore,50);
        pathStore = Arrays.copyOf(incomingPath,50);

        return sendPath;
    }
}
