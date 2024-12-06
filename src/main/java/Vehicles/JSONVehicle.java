package Vehicles;

import behaviorFramework.ArbitrationUnit;
import behaviorFramework.Behavior;
import behaviorFramework.CompositeBehavior;

import com.github.cliftonlabs.json_simple.*;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

public class JSONVehicle extends Vehicle {
    /**
     * Using our own State object to store our vehicles personal data
     */
    private behaviorFramework.Action action = new behaviorFramework.Action();

    /**
     * The Behavior Tree that will be executed
     */
    CompositeBehavior behaviorTree;

    public JSONVehicle() {
        energyUsage = 0;
        energy = 100;
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
    public void initialize(Vehicles myWorld, String filename, String vehicleType) {
        super.initialize(myWorld, state, vehicleType);

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

        // Get vehicle state
        try {
            String name = (String) deserialize.get("state");
            state = (State) Class.forName(new String(name)).newInstance();
            System.out.println("State Name:" + state.getClass().getName());
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            System.exit(0);
        }
        catch (Exception e) { // State not set, use default state
            state = new State();
        }

        // Build behaviorTree
        try {
            treeDesc = new String();
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

        System.out.println(name + ": " + action.name + " " + action.getLeftWheelVelocity() + " " + action.getRightWheelVelocity() + " " + this.getTransform().getRotationAngle());
        lastAction = action.name;

        return action;
    }

    @Override
    public void render(Graphics2D g, double scale) {
        super.render(g, scale);
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
    public CompositeBehavior treeFromJSON(JsonObject json, CompositeBehavior tree) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        JsonArray array;
        boolean arbiterB;

        array = (JsonArray) json.get("behaviorTree");
        for (Object item : array) {
            energyUsage+=0.25;
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

                // Get arbitrator weights and convert into an ArrayList<Double>
                ArrayList<BigDecimal> weightArray = (ArrayList)jsonBehavior.get("weights");
                Iterator<BigDecimal> itr = weightArray.iterator();
                ArrayList<Double> weights = new ArrayList<Double>();
                while (itr.hasNext()){
                    weights.add(itr.next().doubleValue());
                }

                // Create the arbiter
                ArbitrationUnit arbiter = null;

                // If this is the Conditional arbiter, we need the condition
                String condition = null;
                try {
                    condition = (String) jsonBehavior.get("condition");
                } catch (Exception e) { } // Fall through

                if (condition != null) {
                    try{
                        arbiter = (ArbitrationUnit) Class.forName(new String(name)).getDeclaredConstructor(String.class, State.class).newInstance(condition, state);
                        treeDesc = treeDesc.concat(new String("Composite_Conditional("+condition.toString()+")"));
                    } catch (Exception e) {
                        System.out.println("FAILED TO LOAD JSON: Conditional Arbiter with incorrect condition");
                        System.exit(0);
                    }
                } else {
                    arbiter = (ArbitrationUnit) Class.forName(new String(name)).newInstance();
                    treeDesc = treeDesc.concat(new String("Composite_"+name));
                }
                arbiter.setWeights(weights);
                treeDesc = treeDesc.concat("["+ weights.toString().replace(","," ")+"];");

                // Add arbiter to the tree
                composite.setArbitrationUnit(arbiter);

                // Build the sub-tree
                composite = treeFromJSON(jsonBehavior, composite);
                if (tree == null)
                    tree = composite;
                else
                    tree.add(composite);
            } else {
                // Create and add the behavior to the tree
                Behavior behavior = (Behavior) Class.forName(new String(name)).newInstance();
                System.out.println("Classname:" + behavior.getClass().getName());
                tree.add(behavior);
                ArrayList<String> parameters;
                try {
                    parameters = (ArrayList) jsonBehavior.get("parameters");
                    behavior.setParameters(parameters);
                    treeDesc = treeDesc.concat(name+"("+parameters+");");
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
