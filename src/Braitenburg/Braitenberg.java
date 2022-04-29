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
import java.util.ArrayList;
import java.util.Random;

public class Braitenberg extends SimulationFrame {
    /** The serial version id */
    private static final long serialVersionUID = -8518496222422955267L;
    public ArrayList<SimulationBody> myVehicles = new ArrayList<>();

    /**
     * Constructor.
     */
    public Braitenberg() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        super("Vehicles", 20);
        Vehicle test = (Vehicle) Class.forName(new String("Sample.MyVehicle")).newInstance();
        System.out.println("Classname:" + test.getClass().getName());
        test.initialize(this.world);
        myVehicles.add(test);
        this.world.addBody(test);
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
        this.world.addBody(right);

        SimulationBody left = new SimulationBody();
        left.setColor(Color.black);
        left.addFixture(Geometry.createRectangle(0.2, 40+scale));
        left.setMass(MassType.INFINITE);
        left.translate(-16.65-scale*1.16, 7);
        this.world.addBody(left);

        SimulationBody top = new SimulationBody();
        top.setColor(Color.black);
        top.addFixture(Geometry.createRectangle(40+scale*2, 0.2));
        top.setMass(MassType.INFINITE);
        top.translate(0, 8.25+scale*0.58);
        this.world.addBody(top);

        SimulationBody bottom = new SimulationBody();
        bottom.setColor(Color.black);
        bottom.addFixture(Geometry.createRectangle(40+scale*2, 0.2));
        bottom.setMass(MassType.INFINITE);
        bottom.translate(0, -8.25-scale*0.58);
        this.world.addBody(bottom);

        // Light (a polygon)
        Light newLight = new Light();
        newLight.setColor(Color.yellow);
        newLight.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        newLight.translate(new Vector2(-8.0-scale*.5, -5-scale*.5));
        newLight.setMass(MassType.INFINITE);
        this.world.addBody(newLight);

        // Extra light
        Light extraLight = new Light();
        extraLight.setColor(Color.yellow);
        extraLight.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        extraLight.translate(new Vector2(8.0+scale*.5, 5+scale*0.5));
        extraLight.setMass(MassType.INFINITE);
        this.world.addBody(extraLight);

        // Obstacle
        SimulationBody polygon = new SimulationBody();
        polygon.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        polygon.translate(new Vector2(-2.0, 0));
        polygon.setMass(MassType.INFINITE);
        this.world.addBody(polygon);

        CustomStepListener listen = new CustomStepListener();
        listen.setUpdateRate(3);
        this.world.addStepListener(listen);
    }

    /* (non-Javadoc)
     * @see org.dyn4j.samples.SimulationFrame#render(java.awt.Graphics2D, double)
     */
    protected void render(Graphics2D g, double elapsedTime) {
        super.render(g, elapsedTime);
        // Now move vehicles
/*        for(SimulationBody v : myVehicles) {
            ((Vehicle)v).sense(); // call to sense the world.
            ((Vehicle)v).decideAction(); // must cast it so we can call the decideAction function.
        }*/
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
}
