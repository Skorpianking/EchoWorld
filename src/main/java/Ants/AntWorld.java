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
    public ArrayList<Ant[]> matingPairs = new ArrayList<>();

    // Init variables, also used for testing
    int numAnts = 50; // with strict matching, see EchoAntCatFly, this should start a little lower.
    int numResources = 10;
    int scale = 20;
    int generation = 0; // helps track when ants were added to the world
    int timeSteps = 10000;//00; // how long the simulation will last.  S & B did 10^6, we are doing 10^3 right now
    double mutationRate = 0.0001; // how one may change a genome
    // (ie generations) much faster than their sim - we do actual movement through the environment

    // Vars for writing to a file
    String directory = System.getProperty("user.dir");
    //String filename = directory + File.separator + "testing.csv";
    String filename = directory + File.separator + "echo_pop_50_genome_1_mutation_0001_" + 1 + ".csv";
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
        int scale = 20; // this doesn't appear to be something you can pull from the world at this point.
        addWorldObjects(scale);
    }

    private void addWorldObjects(int scale) {
        // add bounding shapes to the world, these are the walls
        SimulationBody right = new SimulationBody();
        right.setColor(Color.black);
        right.addFixture(Geometry.createRectangle(0.2, canvas.getHeight() / camera.scale));
        right.setMass(MassType.INFINITE);
        right.translate((canvas.getWidth()/ (2* camera.scale)) - 0.1, 0);
        right.setUserData(new String("Obstacle"));
        this.world.addBody(right);

        SimulationBody left = new SimulationBody();
        left.setColor(Color.black);
        left.addFixture(Geometry.createRectangle(0.2, canvas.getHeight() / camera.scale));
        left.setMass(MassType.INFINITE);
        left.translate(-(canvas.getWidth()/ (2* camera.scale)) + 0.1, 0);
        left.setUserData(new String("Obstacle"));
        this.world.addBody(left);

        SimulationBody top = new SimulationBody();
        top.setColor(Color.black);
        top.addFixture(Geometry.createRectangle(canvas.getWidth() / camera.scale, 0.2));
        top.setMass(MassType.INFINITE);
        top.translate(0, (canvas.getHeight()/(2*camera.scale)) - 0.1);
        top.setUserData(new String("Obstacle"));
        this.world.addBody(top);

        SimulationBody bottom = new SimulationBody();
        bottom.setColor(Color.black);
        bottom.addFixture(Geometry.createRectangle(canvas.getWidth() / camera.scale, 0.2));
        bottom.setMass(MassType.INFINITE);
        bottom.translate(0, -(canvas.getHeight()/(2*camera.scale)) + 0.1);
        bottom.setUserData(new String("Obstacle"));
        this.world.addBody(bottom);


        // Light (a polygon)
        SimulationBody newLight = new SimulationBody();
        newLight.setColor(Color.yellow);
        newLight.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        newLight.translate(new Vector2(-8.0, -5));
        newLight.setMass(MassType.INFINITE);
        newLight.setUserData("Light");
        this.world.addBody(newLight);

        // Extra light
        SimulationBody extraLight = new SimulationBody();
        extraLight.setColor(Color.yellow);
        extraLight.addFixture(Geometry.createUnitCirclePolygon(5, 0.5));
        extraLight.translate(new Vector2(8.0, 5));
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
                while (!isStopped()) {
                    if(timeSteps > 0) {
                        gameLoop();
                        // you could add a Thread.yield(); or
                        // Thread.sleep(long) here to give the
                        // CPU some breathing room
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                        }
                        timeSteps--;
                    }
                    else {
                        break;
                    }
                }
                // Finish writing to file and close out the program.
                outputStream.flush();
                outputStream.close();
                System.exit(0);
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
            System.out.println("Check file location?");
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
                if(((Ant)alive).isAlive()) {
                    Ant temp = new Ant((Ant) alive);
                    //System.out.println(temp.id + " " + temp.getLinearVelocity());
                    if (temp.replicate()) { // If this ant can replicate, it will
                        ((Ant) alive).numReproductions++;
                        ((Ant) alive).reproduce = true;
                        // Ideally we would like to replicate from a parent ant versus just adding randos to the world
                        // One could, if so inclined, take stock of the entire population, saving those that are
                        // more fit and letting them breed to create the next generation of ants.
                        // Alright, to add more words here, we are going to replicate new ants at the location of
                        // the parent ant, give it the same tag, but not the resources.
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
                        newA.id = newA.tag + generation; //newA.matingTag+newA.offenseTag+newA.defenseTag;
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

            // Now that we've moved all of this into AntWorld, let's see if we can
            // remove objects without crashing the whole simulation
            // Remove from the colonies array
            for(SimulationBody dead: toRemove) {
                //System.out.println("Bring out your dead! " + ((Ant)dead).id);
                dustToDust(dead); // gives resources, if any, back to the environment.
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
            addWorldObjects(scale);
            this.world.update(elapsedTime);

        } else if (this.step.isActive()) {
            world.step(1);
            stepNumber++;
            step.setActive(false);
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
        try {
            world.step(1);
        }
        catch(Exception e) {
            e.printStackTrace(System.out);
            System.out.println("Time step: " + timeSteps);
        }
        stepNumber++;
        step.setActive(false);
    }

    private void dustToDust(SimulationBody dead) {
        for(Resource res : ((Ant)dead).reservoir) {
            res.color = Color.RED;
            resources.add(res);
        }
    }


    /**
     * GA-based breeding of two ants. Crossover and Mutation will occur.  This is very specific right now
     * to the 8 tag+condition model we have.
     *
     * @param ant
     * @param ant1
     * @return
     */
    private Ant[] createChildren(Ant ant, Ant ant1) {
        Ant[] children = new Ant[2];
        // Ok, time to create the two children and split resources -- chromosomes can be of different sizes AND
        // each tag can also be of different sizes, so we will split the children between some point betwee the tags
        // Right now we have 8 tags + conditions, so our crossover will be a random tag between 0 and 8
        //        id = interActionTag+matingTag+offenseTag+defenseTag+tradingTag+combatCondition+tradeCondition+matingCondition;
        Random rand = new Random();
        int crossover = rand.nextInt(8);

        String[] tagsAnt = {ant.interActionTag,ant.matingTag,ant.offenseTag,ant.defenseTag,ant.tradingTag,ant.combatCondition,ant.tradeCondition,ant.matingCondition};
        String[] tagsAnt1 = {ant1.interActionTag,ant1.matingTag,ant1.offenseTag,ant1.defenseTag,ant1.tradingTag,ant1.combatCondition,ant1.tradeCondition,ant1.matingCondition};
        String[] newTag1 = new String[8];
        String[] newTag2 = new String[8];
        // Build the new tags for the two new ants
        int i = 0;
        while(i < crossover) {
            newTag1[i] = tagsAnt[i];
            newTag2[i] = tagsAnt1[i];
            i++;
        }
        while(i < 8) {
            newTag1[i] = tagsAnt1[i];
            newTag2[i] = tagsAnt[i];
            i++;
        }
        children[0] = new Ant(this.world,newTag1);
        //children[0].setColor(ant.getColor()); // new color as this is a new species
        children[1] = new Ant(this.world,newTag2);
        //children[1].setColor(ant1.getColor());

        return children;
    }

    private void writeToFile() {
        for(SimulationBody v : antColonies) {
            Ant temp = (Ant)v;
            if(temp.isAlive()) {
                //"id,offense,defense,mating,generation,life,death
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
            g.setColor(v.getColor());
            g.fillRect((int)(homePoint.x*scale-r*0.5),(int)(homePoint.y*scale-r*0.5),5,5);
        }
        addResources(); // adds resources to the world with a small probability

        // At this point we have possible replication pairs from the previous round.
        // Got through the replication pairs and create new Ants from them
        // Concurrency issues atm.  Need to delve into this more.
        /**
        for(Ant[] pair: matingPairs) {
            // Remove the pairs from the antColonies -- may throw an exception
            // Likely it will if a ant tries to mate more than once
            try {
                Ant[] children = createChildren(pair[0],pair[1]);
                antColonies.add(children[0]);
                antColonies.add(children[1]);
            }
            catch (Exception e) {
                // Keep going -- this means ants will breed on a 1st come, 1st serve basis which is fine
                e.printStackTrace();
            }
        }
        matingPairs = new ArrayList<>(); // make sure we reset this
         */


        // Paint resources as dots on the screen
        for(Resource res : resources) {
            Vector2 point = res.location;
            g.setColor(res.color);
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