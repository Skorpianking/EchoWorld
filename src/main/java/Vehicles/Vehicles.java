package Vehicles;

/* Profiler: -XX:+UnlockCommercialFeatures -agentlib:hprof=cpu=samples,interval=10 */

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
import java.util.logging.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Vehicles extends SimulationFrame {
    /** The serial version id */
    private static final long serialVersionUID = -8518496222422955267L;

    private static JsonObject worldJSON;

    public ArrayList<SimulationBody> myVehicles;
    public ArrayList<Home> homeList;
    public String vehicleType;
    public BlackBoard blackBoard;

    private Map<Integer, SimulationBody> keyBoundItemList;
    private SimulationBody keyBoundItem = null;

    private ArrayList<SimulationBody> foodList;

    private boolean drawScanLines = true;   // right now going to fall through and

    private int foodSpawnTimer;
    private int foodSpawnTimerSetting;
    private ArrayList<Vector2> foodLocationDistributionList;
    private ArrayList<Vector2> foodLocationList;

    private int MAX_FOODLIST_COUNT = 20; //HARDCODE: limit on total number of food
    public int MAX_VEHICLE_COUNT = 10;  //HARDCODE: limit on total number of vehicles

    public int ENERGY_REWARD = 10;
    public int ENERGY_REWARD_MAX = 20;
    public int ENERGY_REWARD_STEP = 1000;
    public int FOOD_ENERGY = 35;

    public int timestep = 0;
    private PrintWriter homeLogStream = null;
    private Logger vehicleLogStream = null;

    // let each vehicle be set to draw or not.

    /**
     * Constructor.
     *
     * @param scale pixels per meter
     */
    public Vehicles(int scale) {
        super("Vehicles", scale); // At the end of construction calls initializeWorld()

        // Custom listener for the simulation update loop
        CustomStepListener listen = new CustomStepListener();
        listen.setUpdateRate(3);
        this.world.addStepListener(listen);

        // Custom key listener for user interactions
        KeyListener listener = new CustomKeyListener();
        this.addKeyListener(listener);
        this.canvas.addKeyListener(listener);

        initializeLogFiles();
    }


    @Override
    protected void initializeWorld() {
        this.world.setGravity(World.ZERO_GRAVITY);
        keyBoundItemList = new HashMap<Integer, SimulationBody>();
        myVehicles = new ArrayList<SimulationBody>();
        homeList = new ArrayList<Home>();
        foodList = new ArrayList<SimulationBody>();

        // scale is associated with camera, pixels per meter
        // Frame is hard coded in the framework to 800x1600. this means for a scale of 20, it should be a 40x80 world
        // With the origin at the center, it becomes:  -20..20, -40..40
        // World size = canvas.width & canvas.height / camera.scale

        // Add bounding shapes to the world, these are the walls
        SimulationBody right = new SimulationBody();
        right.setColor(Color.black);
        right.addFixture(Geometry.createRectangle(0.5, canvas.getHeight() / camera.scale));
        right.setMass(MassType.INFINITE);
        right.translate((canvas.getWidth() / (2 * camera.scale)) - 0.1, 0);
        right.setUserData(new String("Obstacle"));
        right.getFixture(0).setFilter(Categories.ROOT);
        this.world.addBody(right);

        SimulationBody left = new SimulationBody();
        left.setColor(Color.black);
        left.addFixture(Geometry.createRectangle(0.5, canvas.getHeight() / camera.scale));
        left.setMass(MassType.INFINITE);
        left.translate(-(canvas.getWidth() / (2 * camera.scale)) + 0.1, 0);
        left.setUserData(new String("Obstacle"));
        left.getFixture(0).setFilter(Categories.ROOT);
        this.world.addBody(left);

        SimulationBody top = new SimulationBody();
        top.setColor(Color.black);
        top.addFixture(Geometry.createRectangle(canvas.getWidth() / camera.scale, 0.5));
        top.setMass(MassType.INFINITE);
        top.translate(0, (canvas.getHeight() / (2 * camera.scale)) - 0.1);
        top.setUserData(new String("Obstacle"));
        top.getFixture(0).setFilter(Categories.ROOT);
        this.world.addBody(top);

        SimulationBody bottom = new SimulationBody();
        bottom.setColor(Color.black);
        bottom.addFixture(Geometry.createRectangle(canvas.getWidth() / camera.scale, 0.5));
        bottom.setMass(MassType.INFINITE);
        bottom.translate(0, -(canvas.getHeight() / (2 * camera.scale)) + 0.1);
        bottom.setUserData(new String("Obstacle"));
        bottom.getFixture(0).setFilter(Categories.ROOT);
        this.world.addBody(bottom);

        // Get list of Homes
        ArrayList<BigDecimal> position;
        double x = 0.0;
        double y = 0.0;
        try {
            ArrayList<JsonObject> homes = (ArrayList<JsonObject>) worldJSON.get("homes");

            for (JsonObject item : homes) {
                String homeName = null;
                double energy = 0.0;
                try {
                    position = (ArrayList<BigDecimal>) (item.get("position"));
                    x = position.get(0).doubleValue();
                    y = position.get(1).doubleValue();
                } catch (Exception e) {
                    System.out.println("Homes must have a position [x, y]!");
                    System.exit(0);
                }
                try {
                    homeName = (String) item.get("name");
                } catch (Exception e) {
                    System.out.println("Homes must have a name!");
                    System.exit(0);
                }
                try{ // TODO: Currently have a starting energy. Should we add a spawn threshold parameter?
                    BigDecimal temp = (BigDecimal) (item.get("energy"));
                    energy = temp.doubleValue();
                } catch (Exception e) {
                    System.out.println("Homes having starting energy's are optional");
                }
                // Add the home to the Home List.
                Home h = new Home(this);
                h.position = new Vector2(x,y);
                h.name = homeName;
                h.energy = energy;
                h.color = Color.green;

                SimulationBody homeBody = new SimulationBody();
                homeBody.setColor(Color.green);
                homeBody.addFixture(Geometry.createPolygonalEllipse(8,2,2));
                homeBody.translateToOrigin();
                homeBody.translate(new Vector2(x, y));
                homeBody.setUserData(new String(homeName));
                homeBody.getFixture(0).setFilter(Categories.HOMES);
                this.world.addBody(homeBody);
                h.body = homeBody;
                homeList.add(h);
            }
        } catch (Exception e) { } // Homes are optional

        // Get Vehicle Type
        try {
            vehicleType = (String) worldJSON.get("vehicle_type");
        } catch (Exception e) {
            System.out.println("World need a Vehicle Type {Braitenberg, Ant, Boid}!");
            System.exit(0);
        }
        if (vehicleType.equals("Boid"))
            blackBoard = new BlackBoard();

        // Add Vehicles
        ArrayList<JsonObject> vehicles = (ArrayList<JsonObject>) worldJSON.get("vehicles");
        String vehicleName = null;
        for (JsonObject item : vehicles) {
            try {
                vehicleName = (String) item.get("name");
            } catch (Exception e) {
                System.out.println("Vehicles must have a name!");
                System.exit(0);
            }

            // Get vehicle's home name (optional)
            String vehicleHome = "";
            try {
                vehicleHome = (String) item.get("home");
            } catch (Exception e) { }// Having a home is optional

            boolean networking = false;
            try {
                String vehicleNetwork = "";
                vehicleNetwork = (String) item.get("network");
                if (vehicleNetwork.equals("true"))
                    networking = true;
            } catch (Exception e) {} // networking flag is only required if true

            String vehicleID = new String();
            try {
                String vehicleNetwork = "";
                vehicleID = (String) item.get("resource");
            } catch (Exception e) {
            } // resource only becomes an ID if added

            try {
                Vehicle vehicle = null;
                if (networking) {
                    vehicle = (Vehicle) Class.forName("Vehicles.NetworkVehicle").newInstance();
                    vehicle.setName(vehicleName);
                } else // If the vehicle is a named class try and load the class
                    vehicle = (Vehicle) Class.forName(new String(vehicleName)).newInstance();
                System.out.println("Classname:" + vehicle.getClass().getName());
                vehicle.initialize(this, vehicleType);
                if (vehicleID != null)
                    vehicle.setUserData(((String)vehicle.getUserData()).concat(vehicleID));
                if (vehicleHome != null) {
                    // Find the home in the homeList
                    for(Home h : homeList){
                        if (vehicleHome.equals(h.name)) {
                            vehicle.setHome(h);
                            h.color =vehicle.getColor();
                            h.body.setColor(vehicle.getColor());
                        }
                    }
                }
                insertVehicle(vehicle, item);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                // The name is likely a filename. Create a JSONVehicle and load the json file
                String fileName = "data//" + vehicleName + ".json";
                JSONVehicle vehicle = new JSONVehicle();
                vehicle.initialize(this, fileName, vehicleType); // Halts on failure
                insertVehicle(vehicle, item);
                if( vehicleID != null)
                    vehicle.setUserData(((String)vehicle.getUserData()).concat(vehicleID));
                if (vehicleHome != null) {
                    // Find the home in the homeList
                    for(Home h : homeList){
                        if (vehicleHome.equals(h.name)) {
                            vehicle.setHome(h);
                            h.color =vehicle.getColor();
                            h.body.setColor(vehicle.getColor());
                        }
                    }
                }
            }
        }

        // Add Lights (polygons)
        try {
            ArrayList<JsonObject> lights = (ArrayList<JsonObject>) worldJSON.get("lights");
            for (JsonObject item : lights) {
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
                Light.getFixture(0).setFilter(Categories.WORLD);
                try {
                    BigDecimal key = (BigDecimal) item.get("bound_key");
                    if (key.intValue() >= 1 && key.intValue() <= 5) {
                        keyBoundItemList.put(key.intValue(), Light);
                    }
                } catch (Exception e) { // Fall through, it is optional to have a key binding for a light
                }
                this.world.addBody(Light);
            }
        } catch (Exception e) {
        } // Lights are optional

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

                SimulationBody obstacle = new SimulationBody();
                obstacle.setColor(Color.black);
                obstacle.addFixture(Geometry.createRectangle(width, height));
                obstacle.translate(new Vector2(x, y));
                obstacle.setUserData("Obstacle");
                obstacle.getFixture(0).setFilter(Categories.WORLD);
                try {
                    BigDecimal key = (BigDecimal) item.get("bound_key");
                    if (key.intValue() >= 1 && key.intValue() <= 5) {
                        keyBoundItemList.put(key.intValue(), obstacle);
                    }
                    obstacle.setMass(MassType.NORMAL);
                } catch (Exception e) { // It is optional to have a key binding for an obstacle.
                    obstacle.setMass(MassType.INFINITE); // Default to immovable
                }
                this.world.addBody(obstacle);
            }
        } catch (Exception e) {
        } // Obstacles are optional

        // Add Food (squares)
        try {
            JsonObject jFood = (JsonObject) worldJSON.get("food");
            foodSpawnTimer = ((BigDecimal) jFood.get("timer")).intValue();
            foodSpawnTimerSetting = foodSpawnTimer;
            foodLocationList = new ArrayList<Vector2>();
            foodLocationDistributionList = new ArrayList<Vector2>();

            ArrayList<JsonObject> food = (ArrayList<JsonObject>) jFood.get("locations");
            for (JsonObject item : food) {
                try {
                    position = (ArrayList<BigDecimal>) (item.get("position"));
                    x = position.get(0).doubleValue();
                    y = position.get(1).doubleValue();
                    foodLocationList.add(new Vector2(x,y));

                    SimulationBody Food = new SimulationBody();
                    Food.setColor(Color.red);
                    Food.addFixture(Geometry.createEllipse(0.5, 0.5));
                    Food.translate(new Vector2(x, y));
                    Food.setUserData(new String("Food"));
                    Food.getFixture(0).setFilter(Categories.FOOD);
                    this.world.addBody(Food);
                    foodList.add(Food);

                } catch (Exception e) {
                    System.out.println("Food must have a position [x, y]!");
                    System.exit(0);
                }
                try {
                    position = (ArrayList<BigDecimal>) (item.get("distribution"));
                    x = position.get(0).doubleValue();
                    y = position.get(1).doubleValue();
                    foodLocationDistributionList.add(new Vector2(x,y));
                } catch (Exception e) {
                    System.out.println("Food spawn location must have a distribution [x,y]!");
                    System.exit(0);
                }
            }
        } catch (Exception e) {
        } // Food is optional
    }

    private void initializeLogFiles() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();

        LogManager logMan = LogManager.getLogManager();
        vehicleLogStream = Logger.getLogger("Vehicle"); //logMan.getLogger("Vehicle");
        vehicleLogStream.setLevel(Level.ALL);

        // Suppress output to Console
        Logger globalLogger = Logger.getLogger("");
        Handler[] handlers = globalLogger.getHandlers();
        for (Handler handler : handlers) {
            globalLogger.removeHandler(handler);
        }

        try
        {
            //Create Handler and Set Formatter
            FileHandler fh = new
                FileHandler("logs//vehicleLog"+dtf.format(now)+".csv",
                32000000, 20);
            fh.setFormatter(new SimpleFormatter());
            fh.setLevel(Level.FINE);

            // Add the File Handler to Logger
            vehicleLogStream.addHandler(fh);
            vehicleLogStream.log(Level.INFO, "timestep, name, lastAction, energy, home, x, y, heading, " +
                "leftWheelVelocity, rightWheelVelocity, isHolding, isAtHome, deltaPosition, trees_desc");
        }
        catch(IOException Ex)
        {
            System.out.println(Ex.getMessage());
            System.out.println("ERROR: Unable to open Vehicle Log File");
            System.exit(0);
        }

        try {
            String homeName = "logs//homeLog"+dtf.format(now)+".csv";
            homeLogStream = new PrintWriter( new FileOutputStream(homeName, true));
            homeLogStream.write("timestep,name,energy,vehicleCount,position_x,position_y" + "\n"); // writes header to csv file
        }
        catch (FileNotFoundException e){
            System.out.println("Error opening the file: homeLog[time].csv"+e);
            System.exit(0);
        }
/*        try {
            String vehicleName = "logs//vehicleLog"+dtf.format(now)+".csv";
            vehicleLogStream = new PrintWriter( new FileOutputStream(vehicleName, true));
            vehicleLogStream.write("timestep,name,lastAction, energy, home, x, y, heading, isHolding, isAtHome, " +
                "deltaPosition, trees_desc" + "\n"); // writes header to csv file
        }
        catch (FileNotFoundException e){
            System.out.println("Error opening the file: vehicleLog[time].csv");
            System.exit(0);
        }
 */
    }

    /**
     * Parse Vehicle details (draw_scan_lines and position) from the input
     * json and add the vehicle to the world and Vehicle pool.
     *
     * @param vehicle The vehicle object
     * @param item json (draw_scan_lines and position)
     */
    private void insertVehicle(Vehicle vehicle, JsonObject item) {
        try {
            String scanLines = (String)item.get("draw_scan_lines");
            vehicle.setDrawScanLines(scanLines.equals("true"));
        } catch (Exception e) { // drawing scan lines is not set. Default to false
            vehicle.setDrawScanLines(false);
        }

        try {
            ArrayList<BigDecimal> position = (ArrayList<BigDecimal>) (item.get("position"));
            double x = position.get(0).doubleValue();
            double y = position.get(1).doubleValue();
            vehicle.translateToOrigin();
            vehicle.translate(x, y);
        } catch (Exception e) { // Position will be random if not given
            int max = (int)(canvas.getHeight()/ (2* camera.scale) - 2);
            int min = (int)-(canvas.getHeight()/ (2* camera.scale) + 2);
            vehicle.translate(Math.floor(Math.random()*(max-min+1)+min),Math.floor(Math.random()*(max-min+1)+min));
        }

        try {
            BigDecimal rotation = (BigDecimal) (item.get("orientation"));
            double theta = rotation.doubleValue();
            vehicle.rotateAboutCenter(theta);
        } catch (Exception e) { // Position will be random if not given
            vehicle.rotate(Math.random()*(Math.PI));
        }
        myVehicles.add(vehicle);
        world.addBody(vehicle);
    }

    public World<SimulationBody> getWorld() {
        return this.world;
    }

    public SimulationBody getClosestFood(Vehicle v) {
        SimulationBody food = null;
        double closest = 50000;

        for (SimulationBody f : foodList) {
            double dist = v.getWorldCenter().distance(f.getTransform().getTranslation());
            // Get the direction between the center of the vehicle and the impact point
            Vector2 heading = new Vector2(v.getTransform().getTranslation(),f.getTransform().getTranslation());
            double angle = heading.getDirection();
            angle = angle - v.state.getHeading();
//            double angle =  v.getTransform().getTranslation().getAngleBetween(f.getTransform().getTranslation());
//            angle = angle - v.state.getHeading();
            if ( Math.abs(angle) < 0.225 ) {
                if (dist < closest) {
                    closest = dist;
                    food = f;
                }
            }
        }

        return food;
    }

    /* (non-Javadoc)
     * @see org.dyn4j.samples.SimulationFrame#render(java.awt.Graphics2D, double)
     */
    protected void render(Graphics2D g, double elapsedTime) {
        super.render(g, elapsedTime);

//        for(Home h : homeList) {
//            g.setColor(h.color);
//            g.fillOval((int)(h.position.x*camera.scale),(int)(h.position.y*camera.scale),(int)(2*camera.scale),(int)(2*camera.scale));
//        }

        if (drawScanLines) {
            for (SimulationBody v : myVehicles) {
                ((Vehicle) v).render(g, camera.scale);
            }
        }
    }

    /**
     * Entry point for the example application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Parse args to get World filename
        if (args.length != 1) {
            System.out.println("usage: world_filename");
            System.exit(0);
        }

        String filename = "data//" + args[0]; //world3.json";

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
            Vehicles simulation = new Vehicles(scale.intValue());
            simulation.run();
        } catch(Exception e) {
            System.out.println("FAILURE in World Loading and Starting Main():" + e);
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
        }

        @Override
        public void updatePerformed(TimeStep timeStep, PhysicsWorld physicsWorld) {
        }

        @Override
        public void postSolve(TimeStep timeStep, PhysicsWorld physicsWorld) {
        }

        @Override
        public void end(TimeStep timeStep, PhysicsWorld physicsWorld) {
            if ((updateCounter++)%UPDATE_RATE != 0)
                return;

            timestep++;

            if (timestep%ENERGY_REWARD_STEP == 0 && ENERGY_REWARD < ENERGY_REWARD_MAX) {
                ENERGY_REWARD++;
                System.out.println(ENERGY_REWARD);
            }

            // Now move vehicles
            for(SimulationBody v : myVehicles) {
                if ( !v.getUserData().equals("Dead") ) {
                    ((Vehicle) v).sense(); // call to sense the world and decay energy
                    Action act = ((Vehicle) v).decideAction(); // must cast it so we can call the decideAction function.
                    ((Vehicle) v).act(act);
                    double vEnergy = ((Vehicle)v).getEnergy();
                    //vehicleLogStream.println(timestep+","+((Vehicle)v).statusString());
                    vehicleLogStream.log(Level.INFO,timestep+","+((Vehicle)v).statusString() );
                    // Has the Vehicle run out of energy?
                    if (vEnergy <= 0) {
                        // If holding food, need to drop it.
                        if(((Vehicle)v).state.isHolding()) {
                            SimulationBody food = ((Vehicle)v).gripper.getBody2();
                            world.removeJoint(((Vehicle)v).gripper);
                            food.setUserData("Food"); // Renaming the object for testing.
                            food.setAtRest(true);
                            food.setMassType(MassType.INFINITE);
                            ((Vehicle)v).gripper = null;
                        }
                        // Rename it Dead, and teleport it off the screen.
                        v.setUserData("Dead");
                        v.setAtRest(true);  // zero all forces, accelerations and torques
                        v.setEnabled(false); // deactivate being part of collisions
                        v.translate(-(canvas.getWidth() + 50), -(canvas.getHeight() + 50));
                        v.setLinearVelocity(0,0);
                        v.setMass(MassType.INFINITE);
                        ((Vehicle)v).setDrawScanLines(false);
                    }
                }
            }

            /* Update Home and Food Spawn */
            foodSpawnTimer--;
            for (SimulationBody f : foodList) {
                // Home Update
                for (Home h : homeList) {
                    if (h.position.distance(f.getTransform().getTranslation()) < 2.5 && f.getUserData().equals("Garbage")) {
                        h.energy += FOOD_ENERGY-ENERGY_REWARD; // 25.0; // HARDCODE: energy gained from food.
                        // put the food off-screen // Translation is in relation to current position.
                        f.translate(-(canvas.getWidth() + 10), -(canvas.getHeight() + 10));
                        f.setLinearVelocity(0,0);
                        f.setMass(MassType.INFINITE);
                    }
                }
                // Food Spawn
                if (f.getUserData().equals("Garbage") && foodSpawnTimer <= 0) {
                    Vector2 spawnLocation = getFoodSpawnLocation();
                    if (spawnLocation.getXComponent().x != 0.0) {
                        f.setUserData("Food");
                        f.translateToOrigin();
                        f.translate(spawnLocation);
                    }
                    foodSpawnTimer = foodSpawnTimerSetting;
                }
            }
            // Did not have a Food object to move and we have less than MAX_FOODLIST_COUNT, create a new one.
            if(foodSpawnTimer <= 0 && foodList.size() < MAX_FOODLIST_COUNT && foodLocationList != null) {

                Vector2 spawnLocation = getFoodSpawnLocation();
                if (spawnLocation.getXComponent().x != 0.0) {
                    SimulationBody Food = new SimulationBody();
                    Food.setColor(Color.red);
                    Food.addFixture(Geometry.createEllipse(0.5, 0.5));
                    Food.setUserData("Food");
                    Food.translate(spawnLocation);
                    Food.getFixture(0).setFilter(Categories.FOOD);
                    world.addBody(Food);
                    foodList.add(Food);
                }

                foodSpawnTimer = foodSpawnTimerSetting;
            }
            for (Home h : homeList) {
                boolean colonyHealthy = h.Step(); // Update Home, includes Spawning new Vehicles
                homeLogStream.println(timestep+","+h.statusString());
                if (!colonyHealthy) {
                    // Receive collapse signal and when all homes have collapsed
                    // close logs end the simulation.
                    homeLogStream.close();
                    System.exit(0);
                }
            }
        }

        public void setUpdateRate(int rate) {
            UPDATE_RATE = rate;
        }
    }

    private Vector2 getFoodSpawnLocation() {
        Vector2 spawnLocation = new Vector2();

        int loc = (int)(Math.random()*foodLocationList.size());
        boolean separate = false;
        double xSpawn = 0.0;
        double ySpawn = 0.0;
        int attemptCounter = 0;
        while (!separate && attemptCounter < 4) {
            xSpawn = Math.random() * (2 * foodLocationDistributionList.get(loc).x) - foodLocationDistributionList.get(loc).x;
            ySpawn = Math.random() * (2 * foodLocationDistributionList.get(loc).y) - foodLocationDistributionList.get(loc).y;
            xSpawn += foodLocationList.get(loc).x;
            ySpawn += foodLocationList.get(loc).y;
            separate = true;
            for (SimulationBody foodIter : foodList) {
                Vector2 loc_test = foodIter.getWorldCenter();
                if (foodIter.getWorldCenter().distance(xSpawn, ySpawn) <= 0.75) {
                    separate = false;
                    attemptCounter++;
                    xSpawn = ySpawn = 0.0;
                }
            }
        }
        spawnLocation.set(xSpawn, ySpawn);

        return spawnLocation;
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
}
