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
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Vehicles extends SimulationFrame {
    /** The serial version id */
    private static final long serialVersionUID = -8518496222422955267L;

    private static JsonObject worldJSON;

    public ArrayList<SimulationBody> myVehicles;
    public ArrayList<Home> homeList;

    private Map<Integer, SimulationBody> keyBoundItemList;
    private SimulationBody keyBoundItem = null;

    private boolean drawScanLines = true;   // right now going to fall through and
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
    }


    @Override
    protected void initializeWorld() {
        this.world.setGravity(World.ZERO_GRAVITY);
        keyBoundItemList = new HashMap<Integer, SimulationBody>();
        myVehicles = new ArrayList<SimulationBody>();
        homeList = new ArrayList<Home>();

        // scale is associated with camera, pixels per meter
        // Frame is hard coded in the framework to 800x1600. this means for a scale of 20, it should be a 40x80 world
        // With the origin at the center, it becomes:  -20..20, -40..40
        // World size = canvas.width & canvas.height / camera.scale

        // Add bounding shapes to the world, these are the walls
        SimulationBody right = new SimulationBody();
        right.setColor(Color.black);
        right.addFixture(Geometry.createRectangle(0.2, canvas.getHeight() / camera.scale));
        right.setMass(MassType.INFINITE);
        right.translate((canvas.getWidth() / (2 * camera.scale)) - 0.1, 0);
        right.setUserData(new String("Obstacle"));
        this.world.addBody(right);

        SimulationBody left = new SimulationBody();
        left.setColor(Color.black);
        left.addFixture(Geometry.createRectangle(0.2, canvas.getHeight() / camera.scale));
        left.setMass(MassType.INFINITE);
        left.translate(-(canvas.getWidth() / (2 * camera.scale)) + 0.1, 0);
        left.setUserData(new String("Obstacle"));
        this.world.addBody(left);

        SimulationBody top = new SimulationBody();
        top.setColor(Color.black);
        top.addFixture(Geometry.createRectangle(canvas.getWidth() / camera.scale, 0.2));
        top.setMass(MassType.INFINITE);
        top.translate(0, (canvas.getHeight() / (2 * camera.scale)) - 0.1);
        top.setUserData(new String("Obstacle"));
        this.world.addBody(top);

        SimulationBody bottom = new SimulationBody();
        bottom.setColor(Color.black);
        bottom.addFixture(Geometry.createRectangle(canvas.getWidth() / camera.scale, 0.2));
        bottom.setMass(MassType.INFINITE);
        bottom.translate(0, -(canvas.getHeight() / (2 * camera.scale)) + 0.1);
        bottom.setUserData(new String("Obstacle"));
        this.world.addBody(bottom);

        // Get list of Homes
        ArrayList<BigDecimal> position;
        double x = 0.0;
        double y = 0.0;
        try {
            ArrayList<JsonObject> homes = (ArrayList<JsonObject>) worldJSON.get("homes");

            for (JsonObject item : homes) {
                String homeName = null;
                double resource = 0.0;
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
                try{
                    BigDecimal temp = (BigDecimal) (item.get("resource"));
                    resource = temp.doubleValue();
                } catch (Exception e) {
                    System.out.println("Homes having resources are optional?");
                }
                // Add the home to the Home List.
                Home h = new Home();
                h.position = new Vector2(x,y);
                h.name = homeName;
                h.resource = resource;
                homeList.add(h);
            }
        } catch (Exception e) { } // Homes are optional

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
            String vehicleHome = null;
            try {
                vehicleHome = (String) item.get("home");
            } catch (Exception e) { } // Having a home is optional

            try {
                // If the vehicle is a named class try and load the class
                Vehicle vehicle = (Vehicle) Class.forName(new String(vehicleName)).newInstance();
                System.out.println("Classname:" + vehicle.getClass().getName());
                vehicle.initialize(this.world);
                if (vehicleHome != null) {
                    // Find the home in the homeList
                    for(Home h : homeList){
                        if (vehicleHome.equals(h.name))
                            vehicle.setHome(h.position);
                    }
                }
                insertVehicle(vehicle, item);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                // The name is likely a filename. Create a JSONVehicle and load the json file
                String fileName = "data//" + vehicleName + ".json";
                JSONVehicle vehicle = new JSONVehicle();
                vehicle.initialize(this.world, fileName); // Halts on failure
                insertVehicle(vehicle, item);
                if (vehicleHome != null) {
                    // Find the home in the homeList
                    for(Home h : homeList){
                        if (vehicleHome.equals(h.name)) {
                            vehicle.setHome(h.position);
                            break;
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
        } catch (Exception e) {
        } // Obstacles are optional

        // Add Food (squares)
        try {
            ArrayList<JsonObject> food = (ArrayList<JsonObject>) worldJSON.get("food");
            double width = 0.0;
            double height = 0.0;
            for (JsonObject item : food) {
                try {
                    position = (ArrayList<BigDecimal>) (item.get("position"));
                    x = position.get(0).doubleValue();
                    y = position.get(1).doubleValue();
                } catch (Exception e) {
                    System.out.println("Food must have a position [x, y]!");
                    System.exit(0);
                }

                SimulationBody Food = new SimulationBody();
                Food.setColor(Color.pink);
                Food.addFixture(Geometry.createRectangle(0.5, 0.5));
                Food.translate(new Vector2(x, y));
                Food.setUserData(new String("Food"));
                this.world.addBody(Food);
            }
        } catch (Exception e) {
        } // Food is optional


    }

    /**
     *
     * @param vehicle
     * @param item
     */
    private void insertVehicle(Vehicle vehicle, JsonObject item) {
        try {
            String scanLines = (String)item.get("draw_scan_lines");
            if (scanLines.equals("true"))
                vehicle.setDrawScanLines(true);
            else
                vehicle.setDrawScanLines(false);
        } catch (Exception e) { // drawing scan lines is not set. Default to false
            vehicle.setDrawScanLines(false);
        }

        try {
            ArrayList<BigDecimal> position = (ArrayList<BigDecimal>) (item.get("position"));
            double x = position.get(0).doubleValue();
            double y = position.get(1).doubleValue();
            vehicle.translate(x, y);
        } catch (Exception e) { // Position will be random if not given
            int max = (int)(canvas.getHeight()/ (2* camera.scale) - 2);
            int min = (int)-(canvas.getHeight()/ (2* camera.scale) + 2);
            vehicle.translate(Math.floor(Math.random()*(max-min+1)+min),Math.floor(Math.random()*(max-min+1)+min));
        }

        try {
            ArrayList<BigDecimal> position = (ArrayList<BigDecimal>) (item.get("position"));
            double x = position.get(0).doubleValue();
            double y = position.get(1).doubleValue();
            vehicle.setHome(x, y);
        } catch (Exception e) { // Home is non-existent if not set
        }

        myVehicles.add(vehicle);
        world.addBody(vehicle);
    }


    /* (non-Javadoc)
     * @see org.dyn4j.samples.SimulationFrame#render(java.awt.Graphics2D, double)
     */
    protected void render(Graphics2D g, double elapsedTime) {
        super.render(g, elapsedTime);
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
        String filename = new String("data//world2.json");

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

            // Now move vehicles
            for(SimulationBody v : myVehicles) {
                ((Vehicle)v).sense(); // call to sense the world.
                Action act = ((Vehicle)v).decideAction(); // must cast it so we can call the decideAction function.
                ((Vehicle)v).act(act);
            }
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
}
