package Braitenburg;

/* Profiler: -XX:+UnlockCommercialFeatures -agentlib:hprof=cpu=samples,interval=10 */

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Braitenberg extends SimulationFrame {
    /** The serial version id */
    private static final long serialVersionUID = -8518496222422955267L;
    public ArrayList<SimulationBody> myVehicles = new ArrayList<>();
    private Map<Integer, SimulationBody> keyBoundItemList;
    private SimulationBody keyBoundItem = null;


    private SimulationBody Light;

    private boolean drawScanLines = true;   // right now going to fall through and
                                            // let each vehicle be set to draw or not.

    /**
     * Constructor.
     */
    public Braitenberg() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super("Vehicles", 20);

        KeyListener listener = new CustomKeyListener();
        this.addKeyListener(listener);
        this.canvas.addKeyListener(listener);
        String vehicleName = "vehicle";

        try {
            // If the vehicle is a named class try and load the class
            Vehicle test = (Vehicle) Class.forName(new String(vehicleName)).newInstance();
            System.out.println("Classname:" + test.getClass().getName());
            test.initialize(this.world);
            test.setDrawScanLines(true);
            myVehicles.add(test);
            this.world.addBody(test);
        } catch (Exception e) {
            // The name is likely a filename. Create a JSONVehicle and load the json file
            vehicleName = vehicleName + ".json";
            JSONVehicle test = new JSONVehicle();
            test.initialize(this.world, vehicleName);
            test.setDrawScanLines(true);
            myVehicles.add(test);
            this.world.addBody(test);
        }

//        for(int i = 0; i < 1; i++) {
            // Add my new vehicle class
//            test = new Vehicle();
//            test.initialize(this.world);
//            myVehicles.add(test);
//            this.world.addBody(test);
//        }
    }

    @Override
    protected void initializeWorld() {
        this.world.setGravity(World.ZERO_GRAVITY);
        int scale = 20; // this doesn't appear to be something you can pull from the world at this point.

        // add bounding shapes to the world, these are the walls
        SimulationBody right = new SimulationBody();
        right.setColor(Color.black);
        right.addFixture(Geometry.createRectangle(0.2, 40+scale));
        right.setMass(MassType.INFINITE);
        right.translate(16.65+scale*1.16, 7);
        right.setUserData(new String("Obstacle"));
        this.world.addBody(right);

        SimulationBody left = new SimulationBody();
        left.setColor(Color.black);
        left.addFixture(Geometry.createRectangle(0.2, 40+scale));
        left.setMass(MassType.INFINITE);
        left.translate(-16.65-scale*1.16, 7);
        left.setUserData(new String("Obstacle"));
        this.world.addBody(left);

        SimulationBody top = new SimulationBody();
        top.setColor(Color.black);
        top.addFixture(Geometry.createRectangle(40+scale*2, 0.2));
        top.setMass(MassType.INFINITE);
        top.translate(0, 8.25+scale*0.58);
        top.setUserData(new String("Obstacle"));
        this.world.addBody(top);

        SimulationBody bottom = new SimulationBody();
        bottom.setColor(Color.black);
        bottom.addFixture(Geometry.createRectangle(40+scale*2, 0.2));
        bottom.setMass(MassType.INFINITE);
        bottom.translate(0, -8.25-scale*0.58);
        bottom.setUserData(new String("Obstacle"));
        this.world.addBody(bottom);


        // Light (a polygon)
        Light = new SimulationBody();
        Light.setColor(Color.yellow);
        Light.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        Light.translate(new Vector2(8.0+scale*0.5, 5+scale*0.5));
        Light.setMass(MassType.NORMAL);
        Light.setUserData(new String("Light"));
        this.world.addBody(Light);
        keyBoundItemList = new HashMap<Integer, SimulationBody>();
        keyBoundItemList.put(1,Light);

/*
        // Obstacle
        SimulationBody polygon = new SimulationBody();
        polygon.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        polygon.translate(new Vector2(-2.0, 0));
        polygon.setMass(MassType.INFINITE);
        this.world.addBody(polygon);
*/

        // Custom listener for the simulation update loop
        CustomStepListener listen = new CustomStepListener();
        listen.setUpdateRate(3);
        this.world.addStepListener(listen);
    }

    /* (non-Javadoc)
     * @see org.dyn4j.samples.SimulationFrame#render(java.awt.Graphics2D, double)
     */
    protected void render(Graphics2D g, double elapsedTime) {
        super.render(g, elapsedTime);
        if (drawScanLines) {
            for (SimulationBody v : myVehicles) {
                ((Vehicle) v).render(g, 20);
            }
        }
    }

    /**
     * Entry point for the example application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            Braitenberg simulation = new Braitenberg();
            simulation.run();
        } catch(Exception e) {
            System.out.println("FAIL:" + e);
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
