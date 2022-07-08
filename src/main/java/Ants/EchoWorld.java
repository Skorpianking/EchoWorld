package Ants;

import Vehicles.Action;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import framework.SimulationBody;
import framework.SimulationFrame;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.geometry.*;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.StepListener;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.math.BigDecimal;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EchoWorld extends SimulationFrame {

    private static final long serialVersionUID = -8518496222422955267L;
    private static JsonObject worldJSON;
    public ArrayList<Ant> antColonies;
    private Map<Integer, SimulationBody> keyBoundItemList;
    private SimulationBody keyBoundItem = null;

    public ArrayList<SimulationBody> toRemove = new ArrayList<>();
    public ArrayList<Resource> resources = new ArrayList<Resource>();

    int generation = 0; // helps track when ants were added to the world
    int timeSteps = 10000; // how long the simulation will last.  S & B did 10^6, we are doing 10^3 right now
    int resourceFlow = 10; // add up to this many resources to the world at a time P(addRes) = 0.20
    double mutationRate = 0.0001; // how one may change a genome

    // Vars for writing to a file
    String directory = System.getProperty("user.dir");
    String filename = directory + File.separator + "echo_pop_50_genome_4_mutation_0001_" + 1 + ".csv";
    PrintWriter outputStream = null; // file writer


    /**
     * Constructor.
     *
     * @param scale pixels per meter
     */
    public EchoWorld(int scale) {
        super("EchoWorld", scale); // At the end of construction calls initializeWorld()

        // Custom listener for the simulation update loop
        EchoWorld.CustomStepListener listen = new EchoWorld.CustomStepListener();
        listen.setUpdateRate(3);
        this.world.addStepListener(listen);

        // Custom key listener for user interactions
        KeyListener listener = new EchoWorld.CustomKeyListener();
        this.addKeyListener(listener);
        this.canvas.addKeyListener(listener);


        // Establish the file output stream
        try {
            String tacticalAgents = filename;
            outputStream = new PrintWriter( new FileOutputStream(tacticalAgents, true));
            outputStream.write("timestep,id,tag,offense,defense,mating,generation,life,death,parent,numCombats,numTrades,numReproductions,didIFight,didITrade,didIReproduce,predators" +
                    "\n"); // writes header to csv file
        }
        catch (FileNotFoundException e){
            System.out.println("Error opening the file: " + filename);
            System.exit(0);
        }

    }


    @Override
    protected void initializeWorld() {
        this.world.setGravity(World.ZERO_GRAVITY);
        keyBoundItemList = new HashMap<Integer, SimulationBody>();
        antColonies = new ArrayList<Ant>();

        // scale is associated with camera, pixels per meter
        // Frame is hard coded in the framework to 800x1600. this means for a scale of 20, it should be a 40x80 world
        // With the origin at the center, it becomes:  -20..20, -40..40
        // so canvas.width & canvas.height / camera.scale

        // add bounding shapes to the world, these are the walls
        SimulationBody right = new SimulationBody();
        right.setColor(Color.black);
        right.addFixture(Geometry.createRectangle(0.2, canvas.getHeight() / camera.scale));
        right.setMass(MassType.INFINITE);
        right.translate((canvas.getWidth()/ (2* camera.scale)) - 0.1, 0);
        right.setUserData(new String("Obstacle"));
        world.addBody(right);

        SimulationBody left = new SimulationBody();
        left.setColor(Color.black);
        left.addFixture(Geometry.createRectangle(0.2, canvas.getHeight() / camera.scale));
        left.setMass(MassType.INFINITE);
        left.translate(-(canvas.getWidth()/ (2* camera.scale)) + 0.1, 0);
        left.setUserData(new String("Obstacle"));
        world.addBody(left);

        SimulationBody top = new SimulationBody();
        top.setColor(Color.black);
        top.addFixture(Geometry.createRectangle(canvas.getWidth() / camera.scale, 0.2));
        top.setMass(MassType.INFINITE);
        top.translate(0, (canvas.getHeight()/(2*camera.scale)) - 0.1);
        top.setUserData(new String("Obstacle"));
        world.addBody(top);

        SimulationBody bottom = new SimulationBody();
        bottom.setColor(Color.black);
        bottom.addFixture(Geometry.createRectangle(canvas.getWidth() / camera.scale, 0.2));
        bottom.setMass(MassType.INFINITE);
        bottom.translate(0, -(canvas.getHeight()/(2*camera.scale)) + 0.1);
        bottom.setUserData(new String("Obstacle"));
        world.addBody(bottom);

        // Add Lights (polygons)
        ArrayList<BigDecimal> position;
        double x = 0.0;
        double y = 0.0;
        ArrayList<JsonObject> lights = (ArrayList<JsonObject>)worldJSON.get("lights");
        for (JsonObject item: lights) {
            try {
                position = (ArrayList<BigDecimal>) (item.get("position"));
                x = position.get(0).doubleValue();
                y = position.get(1).doubleValue();
            } catch (Exception e) {
                System.out.println("Lights must have a position [x, y]!");
                System.exit(0);
            }

            SimulationBody Light = new SimulationBody();
            Light.setColor(Color.yellow);
            Light.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
            Light.translate(new Vector2(x, y));
            Light.setMass(MassType.NORMAL);
            Light.setUserData(new String("Light"));
            try {
                BigDecimal key = (BigDecimal)item.get("bound_key");
                if (key.intValue() >= 1 && key.intValue() <=5) {
                    keyBoundItemList.put(key.intValue(), Light);
                }
            } catch (Exception e) { // Fall through, it is optional to have a key binding for a light
            }
            this.world.addBody(Light);
        }

        // Add agents
        ArrayList<JsonObject> agents = (ArrayList<JsonObject>)worldJSON.get("agents");
        String antName = null;
        for (JsonObject item: agents) {
            try {
                antName = (String) item.get("interactionTag");
            } catch (Exception e) {
                System.out.println("Agents must have an interactionTag!");
                System.exit(0);
            }
            String[] tags = new String[8];
            tags[0] = (String) item.get("interActionTag");
            tags[1] = (String) item.get("matingTag");
            tags[2] = (String) item.get("offenseTag");
            tags[3] = (String) item.get("defenseTag");
            tags[4] = (String) item.get("tradingTag");
            tags[5] = (String) item.get("combatCondition");
            tags[6] = (String) item.get("tradeCondition");
            tags[7] = (String) item.get("matingCondition");

            Ant tempAnt = new Ant(this.world, tags);
            insertAnt(tempAnt, item);
        }

        // Add Obstacles (rectangles)
        try {
            ArrayList<JsonObject> obstacles = (ArrayList<JsonObject>) worldJSON.get("obstacles");
            double width = 0.0;
            double height = 0.0;
            for (JsonObject item : obstacles) {
                try {
                    position = (ArrayList<BigDecimal>) (item.get("position"));
                    x = position.get(0).doubleValue();
                    y = position.get(1).doubleValue();
                } catch (Exception e) {
                    System.out.println("Obstacles must have a position [x, y]!");
                    System.exit(0);
                }
                try {
                    position = (ArrayList<BigDecimal>) (item.get("size"));
                    width = position.get(0).doubleValue();
                    height = position.get(1).doubleValue();
                } catch (Exception e) {
                    System.out.println("Obstacles must have a size [width, height]!");
                    System.exit(0);
                }

                SimulationBody Obstacle = new SimulationBody();
                Obstacle.setColor(Color.black);
                Obstacle.addFixture(Geometry.createRectangle(width, height));
                Obstacle.translate(new Vector2(x, y));
                Obstacle.setUserData(new String("Obstacle"));
                try {
                    BigDecimal key = (BigDecimal) item.get("bound_key");
                    if (key.intValue() >= 1 && key.intValue() <= 5) {
                        keyBoundItemList.put(key.intValue(), Obstacle);
                    }
                    Obstacle.setMass(MassType.NORMAL);
                } catch (Exception e) { // It is optional to have a key binding for an obstacle.
                    Obstacle.setMass(MassType.INFINITE); // Default to immovable
                }
                this.world.addBody(Obstacle);
            }
        } catch (Exception e) {} // Obstacles are optional
    }

    /**
     *
     * @param vehicle
     * @param item
     */
    private void insertAnt(Ant vehicle, JsonObject item) {
        try {
            ArrayList<BigDecimal> position = (ArrayList<BigDecimal>) (item.get("position"));
            double x = position.get(0).doubleValue();
            double y = position.get(1).doubleValue();
            vehicle.setPosition(new Vector2(x,y));
        } catch (Exception e) { // Position will be random if not given
            int max = (int)(canvas.getHeight()/ (2* camera.scale) - 2);
            int min = (int)-(canvas.getHeight()/ (2* camera.scale) + 2);
            vehicle.translate(Math.floor(Math.random()*(max-min+1)+min),Math.floor(Math.random()*(max-min+1)+min));
        }
        antColonies.add(vehicle);
        world.addBody(vehicle);
    }


    /* (non-Javadoc)
     * @see org.dyn4j.samples.SimulationFrame#render(java.awt.Graphics2D, double)
     */
    protected void render(Graphics2D g, double elapsedTime) {
        super.render(g, elapsedTime);
        // Paint resources as dots on the screen
        for(Resource res : resources) {
            Vector2 point = res.location;
            g.setColor(res.color);
            g.fillRect((int)(point.x*this.camera.scale),(int)(point.y*this.camera.scale),3,3);
        }
    }

    /**
     * Entry point for the example application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String filename = new String("echoworld_50agents_4lengthtags.json");

        // Read in the JSON world file
        try (FileReader fileReader = new FileReader((filename))) {
            worldJSON = (JsonObject) Jsoner.deserialize(fileReader);
        } catch (Exception e) {
            System.out.println("FAILED to Load:" + filename +"/n" + e);
            System.exit(0);
        }

        // Get world scale factor (pixels per meter)
        BigDecimal scale = (BigDecimal)worldJSON.get("pixels_per_meter");

        try {
            EchoWorld simulation = new EchoWorld(scale.intValue());
            simulation.run();
        } catch(Exception e) {
            System.out.println("FAILURE in Main():" + e);
        }
    }

    /**
     *
     */
    private class CustomStepListener implements StepListener {
        private int UPDATE_RATE = 3;
        private int updateCounter = 0;

        @Override
        public void begin(TimeStep timeStep, PhysicsWorld physicsWorld) {
            if ((updateCounter++)%UPDATE_RATE != 0)
                return;

            if(timeSteps <= 0) {
                smoothClose();
            }
            try {
                writeToFile(); // Before we do anything, we will write out all the ants to a file
            }
            catch (Exception e) {
                System.out.println("Check file location?");
                smoothClose();
            }
            generation++; // increment our generation counter
            timeSteps--; // time steps go down :-)
            addResources(); // adds resources to the world with a small probability

            // See if any can reproduce
            tryToReproduce();

            // Now move ants - action loop
            for(SimulationBody v : antColonies) {
                if((((Ant)v).isAlive())) {
                    ((Ant) v).decide(antColonies, resources);
                    ((Ant) v).decLife();
                }
                else {
                    toRemove.add(v);
                }
            } // action loop complete
            cleanUp();
        }

        @Override
        public void updatePerformed(TimeStep timeStep, PhysicsWorld physicsWorld) {
        }

        @Override
        public void postSolve(TimeStep timeStep, PhysicsWorld physicsWorld) {
        }

        @Override
        public void end(TimeStep timeStep, PhysicsWorld physicsWorld) {
        }

        public void setUpdateRate(int rate) {
            UPDATE_RATE = rate;
        }
    }

    /**
     * Keyboard input to move the Light around.
     */
    private class CustomKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            Vector2 up_down = new Vector2(0.0, 1.0);
            Vector2 left_right = new Vector2(1.0, 0.0);
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                    up_down = up_down.multiply(3);
                    if (keyBoundItem != null)
                        keyBoundItem.applyForce(up_down);
                    break;
                case KeyEvent.VK_S:
                    up_down = up_down.multiply(-3);
                    if (keyBoundItem != null)
                        keyBoundItem.applyForce(up_down);
                    break;
                case KeyEvent.VK_A:
                    left_right = left_right.multiply(-3);
                    if (keyBoundItem != null)
                        keyBoundItem.applyForce(left_right);
                    break;
                case KeyEvent.VK_D:
                    left_right = left_right.multiply(3);
                    if (keyBoundItem != null)
                        keyBoundItem.applyForce(left_right);
                    break;
                case KeyEvent.VK_X:
                    if (keyBoundItem != null)
                        keyBoundItem.setLinearVelocity(0.0, 0.0);
                    break;
                case KeyEvent.VK_1:
                    keyBoundItem = keyBoundItemList.get(1);
                    break;
                case KeyEvent.VK_2:
                    keyBoundItem = keyBoundItemList.get(2);
                    break;
                case KeyEvent.VK_3:
                    keyBoundItem = keyBoundItemList.get(3);
                    break;
                case KeyEvent.VK_4:
                    keyBoundItem = keyBoundItemList.get(4);
                    break;
                case KeyEvent.VK_5:
                    keyBoundItem = keyBoundItemList.get(5);
                    break;
            }
        }
    }

    // ---------------------------------------------------------------------- //
    // ---------------------- Capability function area ---------------------- //
    // ---------------------------------------------------------------------- //

    /**
     * Writes ant properties to file.
     */
    private void writeToFile() {
        for(SimulationBody v : antColonies) {
            Ant temp = (Ant)v;
            if(temp.isAlive()) {
                outputStream.write(generation + ","+ temp.id + ","+ temp.tag +"," + temp.offenseTag + "," + temp.defenseTag + "," + temp.matingTag + "," +
                        temp.getGeneration() + "," + temp.getLife() + "," + "0" + "," + temp.parent + "," + temp.numCombats + "," +
                        temp.numTrades + "," + temp.numReproductions + "," + temp.fight + "," + temp.trade + "," + temp.reproduce +
                        "," + temp.predator + "\n");
            }
            else {
                outputStream.write(generation + "," + temp.id + "," + temp.tag + "," + temp.offenseTag + "," + temp.defenseTag + "," + temp.matingTag + "," +
                        temp.getGeneration() + "," + temp.getLife() + "," + generation + "," + temp.parent + "," + temp.numCombats + "," +
                        temp.numTrades + "," + temp.numReproductions + "," + temp.fight + "," + temp.trade + "," + temp.reproduce +
                        "," + temp.predator + "\n");
            }
            temp.reset();
        }
    }

    /**
     * Adds up to 10 random resources to the scenario at each time step.  Resources are added to random locations.
     */
    private void addResources() {
        Random rand = new Random();
        for(int i = 0; i < resourceFlow; i++) {
            int prob = rand.nextInt(100);
            if (prob < 2) {
                resources.add(new Resource());
            }
        }
    }

    /**
     * Removes dead ants from the simulation
     */
    private void cleanUp() {
        // Let's try to remove and then add
        for(SimulationBody all : antColonies) {
            try {
                world.removeBody(all);
            }
            catch(Exception e) {
                System.out.println("Constraint graph issue.  Time step: " + timeSteps);
                // I believe this is a double delete or double add issue for uniqueness?
            }
        }
        // Remove any dead ants from the simulation
        for(SimulationBody dead: toRemove) {
            dustToDust(dead); // gives resources, if any, back to the environment.
            antColonies.remove(dead);
        }
        toRemove = new ArrayList<>(); // reset the dead
        for(SimulationBody all : antColonies) { // add the bodies back in
            world.addBody(all);
        }
        if(antColonies.size() == 0) {
            System.out.println("They are all dead Dave! What did you do?");
            smoothClose();
        }
    }

    /**
     * Returns resources to the world.
     *
     * @param dead
     */
    private void dustToDust(SimulationBody dead) {
        for(Resource res : ((Ant)dead).reservoir) {
            res.color = Color.RED;
            resources.add(res);
        }
    }

    /**
     * Goes through the ants, replicates those that can.
     */
    private void tryToReproduce() {
        ArrayList<SimulationBody> newAnts = new ArrayList<SimulationBody>();
        for(SimulationBody alive: antColonies) {
            if (((Ant) alive).isAlive()) {
                Ant temp = new Ant((Ant) alive);
                //System.out.println(temp.id + " " + temp.getLinearVelocity());
                if (temp.replicate()) { // If this ant can replicate, it will
                    ((Ant) alive).numReproductions++;
                    ((Ant) alive).reproduce = true;
                    Ant newA = new Ant(this.world);
                    newA.matingTag = temp.matingTag;
                    newA.matingCondition = temp.matingCondition;
                    newA.tradeCondition = temp.tradeCondition;
                    newA.combatCondition = temp.combatCondition;
                    newA.tradingTag = temp.tradingTag;
                    newA.defenseTag = temp.defenseTag;
                    newA.offenseTag = temp.offenseTag;
                    newA.interActionTag = temp.interActionTag;
                    newA.mutate(mutationRate); // mutation
                    newA.id = newA.tag + generation + " " + new UID();
                    //System.out.println("Hello! My name is: " + newA.id);
                    newA.setParent(temp.id);
                    newA.setGeneration(generation);
                    newA.setHome(temp.getHome()); // same home as its parent
                    newA.setColor(temp.getColor()); // same color as its parent
                    newA.setPosition(temp.getLocalCenter());
                    newA.reservoir = new ArrayList<>(); // resets the reservoir
                    newAnts.add(newA); // adding after this loop to avoid concurrency issues
                    this.world.addBody(newA); // add to the world
                }
            }
        }
        // Add new ants to the simulation
        for(SimulationBody temp: newAnts) {
            antColonies.add((Ant)temp);
        }
    }

    /**
     * Flush the buffers and exit.
     */
    private void smoothClose() {
        outputStream.flush();
        outputStream.close();
        System.exit(0); // end the sim
    }

}

