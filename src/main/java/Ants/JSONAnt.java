package Ants;

import Vehicles.Action;
import Vehicles.State;
import Vehicles.Vehicle;
import behaviorFramework.ArbitrationUnit;
import behaviorFramework.Behavior;
import behaviorFramework.CompositeBehavior;

import framework.SimulationBody;
import org.dyn4j.world.World;

import com.github.cliftonlabs.json_simple.*;

import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

public class JSONAnt extends Ant {
    /**
     * Using our own State object to store our vehicles personal data
     */
    private State state;
    private behaviorFramework.Action action = new behaviorFramework.Action();
    private String name;

    /**
     * The Behavior Tree that will be executed
     */
    CompositeBehavior behaviorTree;

    public JSONAnt() {
        state = new State();
    }

    /**
     * <p>The intialization method receives a filename to a JSON instantiated
     * behavior tree. The file is loaded and the behaviors dynamically loaded
     * for execution. the construction of the behavior tree
     *
     * JSONVehicle does not include a mechanism to allow for a unique State.
     *
     * @param myWorld the simulation world passed to Vehicle to maintain connection
     * @param filename the json filename to load
     */
    public void initialize(World<SimulationBody> myWorld, String filename) {
        super.initialize(myWorld, state);
        JsonObject deserialize = null;

        // Read in the vehicle's JSON file
        try (FileReader fileReader = new FileReader((filename))) {
            deserialize = (JsonObject) Jsoner.deserialize(fileReader);
        } catch (Exception e) {
            System.out.println("FAILED to Load:" + filename +"/n" + e);
            System.exit(0);
        }

        System.out.println(deserialize);

        // Get vehicle name
        name = (String)deserialize.get("vehicleName");
        setUserData(name);

        // Get vehicle color
        try {
            ArrayList<BigDecimal> color = (ArrayList<BigDecimal>) deserialize.get("color");
            setColor(new Color(color.get(0).intValue(), color.get(1).intValue(), color.get(2).intValue()));
        } catch (Exception e) { } // Color wasn't set, use default

        // Build behaviorTree
        try {
            behaviorTree = treeFromJSON(deserialize, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Loaded!");
    }

    /**
     * Every vehicle senses the world
     *
     * JSONVehicle doesn't have a way to include an extended state.
     *
     * @return true
     */
    public boolean sense(){
        // Update the base sensors first
        super.sense();

        return true;
    }

    /**
     * Call the behavior framework and get an action to execute
     *
     * @return the action to execute
     */
    public Action decideAction() {
        action.clear();

        // Get an action from the behaviorTree
        action = behaviorTree.genAction(state);

        System.out.println(name + ": " + action.name + " " + action.getLeftWheelVelocity() + " " + action.getRightWheelVelocity());

        return action;
    }


    /**
     * Traverses the JSON behavior tree and loads each class into a behavior tree
     * that will then be executed.
     *
     * @param json Behavior tree in JSON
     * @param tree Current node in the tree
     * @return executable behavior tree
     * @throws ClassNotFoundException Behavior not located
     * @throws InstantiationException Behavior wasn't created
     * @throws IllegalAccessException Call to class loader failed
     */
    public CompositeBehavior treeFromJSON(JsonObject json, CompositeBehavior tree) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        JsonArray array;
        boolean arbiterB;

        array = (JsonArray) json.get("behaviorTree");
        for (Object item : array) {
            JsonObject jsonBehavior = (JsonObject) item;
            try {
                arbiterB = (boolean) jsonBehavior.get("arbiter");
            } catch (Exception e) {
                arbiterB = false;
            }
            String name = (String) jsonBehavior.get("name");
            System.out.println("name: " + name);

            if (arbiterB) {
                System.out.println("Composite");
                CompositeBehavior composite = new CompositeBehavior();

                if (tree == null)
                    tree = composite;

                // Get arbitrator weights and convert into an ArrayList<Double>
                ArrayList<BigDecimal> weightArray = (ArrayList)jsonBehavior.get("weights");
                Iterator<BigDecimal> itr = weightArray.iterator();
                ArrayList<Double> weights = new ArrayList<Double>();
                while (itr.hasNext()){
                    weights.add(itr.next().doubleValue());
                }

                // Create the arbiter
                ArbitrationUnit arbiter = (ArbitrationUnit) Class.forName(new String(name)).newInstance();
                arbiter.setWeights(weights);

                // Add arbiter to the tree
                composite.setArbitrationUnit(arbiter);

                // Build the sub-tree
                composite = treeFromJSON(jsonBehavior, composite);
            } else {
                // Create and add the behavior to the tree
                Behavior behavior = (Behavior) Class.forName(new String(name)).newInstance();
                System.out.println("Classname:" + behavior.getClass().getName());
                tree.add(behavior);
                ArrayList<String> parameters;
                try {
                    parameters = (ArrayList) jsonBehavior.get("parameters");
                    behavior.setParameters(parameters);
                } catch (Exception e) { // Fall through, parameters are optional
                }
            }
        }
        return tree;
    }

    /**
     *  Stub for future writing of the behavior tree into a JSON file.
     *
     * @param filename the filename to save into
     */
    public void treeToJSON(String filename) {
    }
}
