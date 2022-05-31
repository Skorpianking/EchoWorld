package Ants;

import framework.SimulationBody;
import framework.SimulationFrame;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

/**
 * Beginning of an echo-based version of ant-like agents.  I'm going to start with a simple shape, e.g. a circle,
 * and build out from there.  Obviously, all blueprinted off of Holland's base echo model.
 *
 * Created:  25 April 2022
 *
 */

public class AntWorld extends SimulationFrame {
    public ArrayList<Ant> antColonies = new ArrayList<>();
    public ArrayList<SimulationBody> toRemove = new ArrayList<>();
    public ArrayList<Resource> resources = new ArrayList<Resource>();

    // Init variables, also used for testing
    int numAnts = 5;
    int numResources = 10;
    int scale = 20;
    int generation = 0; // helps track when ants were added to the world

    // Vars for writing to a file
    String directory = System.getProperty("user.dir");
    String filename = directory + File.separator + "random_echo_ant_test_" + 0 + ".csv";
    PrintWriter outputStream = null; // file writer


    /**
     * Constructor.
     * <p>
     * By default creates a 800x600 canvas.
     *
     */
    public AntWorld() {
        super("Ant World", 20);
        this.scale = 20;
        // Add ants here
        for(int i = 0; i < numAnts; i++) {
            Ant testAnt = new Ant(this.world);
            testAnt.setGeneration(0);
            this.world.addBody(testAnt);
            antColonies.add(testAnt);
        }

        // Let's add a resource or two
        for(int i = 0; i < numResources; i++) {
            Resource res = new Resource();
            resources.add(res);
        }

        try {
            String tacticalAgents = filename;
            outputStream = new PrintWriter( new FileOutputStream(tacticalAgents, true));
            outputStream.write("timestep,id,offense,defense,mating,generation,life,death\n"); // writes header to csv file
        }
        catch (FileNotFoundException e){
            System.out.println("Error opening the file: " + filename);
            System.exit(0);
        }
    }

    @Override
    protected void initializeWorld() {
        this.world.setGravity(World.ZERO_GRAVITY);
        int scale = 20; // this doesn't appear to be something you can pull from the world at this point.
        addWorldObjects(scale);
    }

    private void addWorldObjects(int scale) {
        // add bounding shapes to the world, these are the walls
        SimulationBody right = new SimulationBody();
        SimulationBody left = new SimulationBody();
        SimulationBody bottom = new SimulationBody();
        SimulationBody top = new SimulationBody();

        right.setColor(Color.black);
        right.addFixture(Geometry.createRectangle(0.2, 40 + scale));
        right.setMass(MassType.INFINITE);
        right.translate(16.65 + scale * 1.16, 7);
        right.setUserData("Obstacle");
        this.world.addBody(right);

        left.setColor(Color.black);
        left.addFixture(Geometry.createRectangle(0.2, 40 + scale));
        left.setMass(MassType.INFINITE);
        left.translate(-16.65 - scale * 1.16, 7);
        left.setUserData("Obstacle");
        this.world.addBody(left);

        top.setColor(Color.black);
        top.addFixture(Geometry.createRectangle(40 + scale * 2, 0.2));
        top.setMass(MassType.INFINITE);
        top.translate(0, 8.25 + scale * 0.58);
        top.setUserData("Obstacle");
        this.world.addBody(top);

        bottom.setColor(Color.black);
        bottom.addFixture(Geometry.createRectangle(40 + scale * 2, 0.2));
        bottom.setMass(MassType.INFINITE);
        bottom.translate(0, -8.25 - scale * 0.58);
        bottom.setUserData("Obstacle");
        this.world.addBody(bottom);


        // Light (a polygon)
        SimulationBody newLight = new SimulationBody();
        newLight.setColor(Color.yellow);
        newLight.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        newLight.translate(new Vector2(-8.0 - scale * .5, -5 - scale * .5));
        newLight.setMass(MassType.INFINITE);
        newLight.setUserData("Light");
        this.world.addBody(newLight);

        // Extra light
        SimulationBody extraLight = new SimulationBody();
        extraLight.setColor(Color.yellow);
        extraLight.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        extraLight.translate(new Vector2(8.0 + scale * .5, 5 + scale * 0.5));
        extraLight.setMass(MassType.INFINITE);
        extraLight.setUserData("Light");
        this.world.addBody(extraLight);

        // Obstacle
        SimulationBody polygon = new SimulationBody();
        polygon.setColor(Color.CYAN);
        polygon.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        polygon.translate(new Vector2(-2.0, 0));
        polygon.setMass(MassType.INFINITE);
        polygon.setUserData("Light");
        this.world.addBody(polygon);
    }

    private void start() {
        // initialize the last update time
        this.last = System.nanoTime();
        // don't allow AWT to paint the canvas since we are
        this.canvas.setIgnoreRepaint(true);
        // enable double buffering (the JFrame has to be
        // visible before this can be done)
        this.canvas.createBufferStrategy(2);
        // run a separate thread to do active rendering
        // because we don't want to do it on the EDT
        Thread thread = new Thread() {
            public void run() {
                // perform an infinite loop stopped
                // render as fast as possible
                // todo:  add in the simulation time steps here... it kicks out once exceeded
                while (!isStopped()) {
                    gameLoop();
                    // you could add a Thread.yield(); or
                    // Thread.sleep(long) here to give the
                    // CPU some breathing room
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {}
                }
                // I think I can close file here
                outputStream.flush();
                outputStream.close();
            }
        };
        // set the game loop thread to a daemon thread so that
        // it cannot stop the JVM from exiting
        thread.setDaemon(true);
        // start the game loop
        thread.start();
    }

    public void run() {
        // set the look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // show it
        this.setVisible(true);

        // start it
        this.start();
    }

    private void gameLoop() {
        try {
            writeToFile(); // Before we do anything, we will write out all the ants to a file
        }
        catch (Exception e) {
            System.out.println("Being sloppy with exception handling");
            outputStream.flush();
            outputStream.close();
            System.exit(0); // end the sim
        }
        generation++; // increment our generation counter

        // get the graphics object to render to
        Graphics2D g = (Graphics2D)this.canvas.getBufferStrategy().getDrawGraphics();

        // by default, set (0, 0) to be the center of the screen with the positive x axis
        // pointing right and the positive y axis pointing up
        this.transform(g);
        // reset the view
        this.clear(g);
        // get the current time
        long time = System.nanoTime();
        // get the elapsed time from the last iteration
        long diff = time - this.last;
        // set the last time
        this.last = time;
        // convert from nanoseconds to seconds
        double elapsedTime = (double)diff / NANO_TO_BASE;
//    	System.out.println(elapsedTime);

        // render anything about the simulation (will render the World objects)
        AffineTransform tx = g.getTransform();
        g.translate(this.camera.offsetX, this.camera.offsetY);
        this.render(g, elapsedTime);
        g.setTransform(tx);

        // update the World
        if (!this.paused.isActive()) {

            ArrayList<SimulationBody> newAnts = new ArrayList<SimulationBody>();
            for(SimulationBody alive: antColonies) {
                Ant temp = new Ant((Ant)alive);
                //System.out.println(temp.id + " " + temp.getLinearVelocity());
                if(temp.replicate()) { // If this ant can replicate, it will
                    // Ideally we would like to replicate from a parent ant versus just adding randos to the world
                    // One could, if so inclined, take stock of the entire population, saving those that are
                    // more fit and letting them breed to create the next generation of ants.
                    // Alright, to add more words here, we are going to replicate new ants at the location of
                    // the parent ant, give it the same tag, but not the resources.
                    Ant newA = new Ant(this.world);
                    newA.id = newA.id + "_" + elapsedTime;
                    System.out.println("Hello! My name is: " + newA.id);
                    newA.setGeneration(generation);
                    newA.setHome(temp.getHome()); // same home as its parent
                    newA.setColor(temp.getColor()); // same color as its parent
                    newA.reservoir = new ArrayList<>(); // resets the reservoir
                    newAnts.add(newA); // adding after this loop to avoid concurrency issues
                    this.world.addBody(newA); // add to the world
                }
            }

            // Now that we've moved all of this into AntWorld, let's see if we can
            // remove objects without crashing the whole simulation
            // Remove from the colonies array
            for(SimulationBody dead: toRemove) {
                System.out.println("Bring out your dead!" + ((Ant)dead).id);
                antColonies.remove(dead);
            }
            toRemove = new ArrayList<>();

            for(SimulationBody temp: newAnts) {
                antColonies.add((Ant)temp);
            }
            this.world.removeAllBodies();
            for(SimulationBody ant : antColonies) {
                this.world.addBody(ant);
            }
            this.world.update(elapsedTime);

        } else if (this.step.isActive()) {
            this.world.step(1);
            this.stepNumber++;
            this.step.setActive(false);
        }
        this.handleEvents();

        // dispose of the graphics object
        g.dispose();

        // blit/flip the buffer
        BufferStrategy strategy = this.canvas.getBufferStrategy();
        if (!strategy.contentsLost()) {
            strategy.show();
        }

        // Sync the display on some systems.
        // (on Linux, this fixes event queue problems)
        Toolkit.getDefaultToolkit().sync();

        this.stepNumber++;
        this.step.setActive(false);
        world.step(1);
    }

    private void writeToFile() {
        for(SimulationBody v : antColonies) {
            Ant temp = (Ant)v;
            if(temp.isAlive()) {
                //"id,offense,defense,mating,generation,life,death
                outputStream.write(generation + ","+ temp.id + "," + temp.offenseTag + "," + temp.defenseTag + "," + temp.matingTag + "," +
                        temp.getGeneration() + "," + temp.getLife() + "," + "0" + "\n");
            }
            else {
                outputStream.write(generation + "," + temp.id + "," + temp.offenseTag + "," + temp.defenseTag + "," + temp.matingTag + "," +
                        temp.getGeneration() + "," + temp.getLife() + "," + generation + "\n");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.dyn4j.samples.SimulationFrame#render(java.awt.Graphics2D, double)
     */
    protected void render(Graphics2D g, double elapsedTime) {
        super.render(g, elapsedTime);

        double r = 4.0;
        // Now move ants
        for(SimulationBody v : antColonies) {
            if((((Ant)v).isAlive())) {
                ((Ant) v).decide(antColonies, resources);
                v.render(g, elapsedTime);
                ((Ant) v).decLife();
            }
            else {
                toRemove.add(v);
            }
            // Testing area, paint their home
            Vector2 homePoint = ((Ant)v).getHome();
            g.setColor(Color.red);
            g.fillRect((int)(homePoint.x*scale-r*0.5),(int)(homePoint.y*scale-r*0.5),5,5);
        }
        addResources(); // adds resources to the world with a small probability

        // Paint resources as dots on the screen
        for(Resource res : resources) {
            Vector2 point = res.location;
            g.setColor(Color.GREEN);
            g.fillRect((int)(point.x*scale-r*05),(int)(point.y*scale-r*.05),3,3);
        }
    }

    /**
     * Adds up to 10 random resources to the scenario at each time step.  Resources are added to random locations.
     */
    private void addResources() {
        Random rand = new Random();
        for(int i = 0; i < 10; i++) {
            int prob = rand.nextInt(100);
            if (prob < 2) {
                resources.add(new Resource());
            }
        }
    }

    /**
     * Entry point for the example application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        AntWorld simulation = new AntWorld();
        simulation.run();
    }
}
