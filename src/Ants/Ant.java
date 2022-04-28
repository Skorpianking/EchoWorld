package Ants;

import framework.SimulationBody;
import framework.SimulationFrame;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Interval;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.awt.*;
import java.util.*;

public class Ant extends SimulationBody {
    // Echo parameters -- will be extended over time
    String id;
    ArrayList<Resource> reservoir = new ArrayList<>();

    // Boid-ish vectors to help with new movement
    Vector2 position;      // position of Agent_Ant in 2d space
    Vector2 velocity;      // velocity of Agent_Ant (2D vector)
    Vector2 acceleration;    // acceleration of Agent_Ant (2D vector)
    boolean headedHome = false;

    // Extra variables to track the ants
    private boolean alive = true;
    private int lifespan = 1000000;
    private double x_pos;
    private double y_pos;
    private Vector2 home; // where the colony "resides" in the space.
    float maxspeed = 5;    // default speed for an Ant
    float maxforce = 0.05f;    // steering force for the Ant

    //  Separation and alignment variables
    float sepDist = 10f;
    float alignDist = 10f;

    // Actual ant "body" for the world to paint
    private SimulationBody basicAnt;

    // All taken from the cue basicAnt class template
    // 2.25 in diameter = 0.028575 m radius
    final double ballRadius = 0.028575;
    // 0.126 oz/in^3 = 217.97925 kg/m^3
    final double ballDensity = 217.97925;
    final double ballFriction = 0.00;
    final double ballRestitution = 0.9;

    private double MAX_VELOCITY = 5; // arbitrary right now
    private int SENSOR_RANGE = 5; // how far an ant can see
    private int MAX_TORQUE = 1; // how fast we can turn
    private int TOLERANCE = 2; // how far "off" the target can be, allows us to home in on a target

    // World the ant lives in
    World<SimulationBody> myWorld;

    public Ant(World<SimulationBody> world) {
        myWorld = world;
        basicAnt = new SimulationBody(new Color(255, 255, 255));
        basicAnt.addFixture(Geometry.createCircle(ballRadius), ballDensity, ballFriction, ballRestitution);
        basicAnt.translate(-0.25, 0.0);
        basicAnt.setLinearVelocity(2.0, 0.0);
        basicAnt.setLinearDamping(0.3);
        basicAnt.setAngularDamping(0.8);
        basicAnt.setMass(MassType.NORMAL);
        myWorld.addBody(basicAnt);

        int max = 15;
        int min = -15;
        x_pos = Math.floor(Math.random()*(max-min+1)+min);
        y_pos = Math.floor(Math.random()*(max-min+1)+min);
        basicAnt.translate(x_pos,y_pos);
        id = new String(basicAnt.getWorldCenter().toString()); // hopefully this makes their names random enough for now

        Random rand = new Random();
        home = new Vector2(-4,0); // center of the screen for now.
        position = home;
        velocity = new Vector2(-1*rand.nextInt(1),-1*rand.nextInt(1));
        acceleration = new Vector2(0,0);
    }

    /**
     * Good old copy constructor
     * @param copy
     */
    public Ant(Ant copy) {
        this.myWorld = copy.myWorld;
        this.basicAnt = copy.basicAnt;
        this.x_pos = copy.x_pos;
        this.y_pos = copy.y_pos;
        this.id = copy.id;
        this.home = copy.home;
        this.myWorld.addBody(this.basicAnt);
        this.position = copy.position;
        this.velocity = copy.velocity;
        this.acceleration = copy.acceleration;
    }

    /**
     * Decision-making process for an ant - look around, do I see food? do I see another ant? etc.
     * @param resources
     */
    public void decide(ArrayList<Ant> allAgent_Ants, ArrayList<Resource> resources) {
        if(!headedHome) {
            check(allAgent_Ants);  // sep/target selection function
            update(resources); // update position
            returnHome(); // see if it is necessary to return home
        }
        else {
            headHome(allAgent_Ants); // maintain separation from other Agent_Ants
            update(resources); // update position
            checkHome(); // is the Agent_Ant home?
        }
        borders(); // check the borders
        decLife(); // always burn 1 energy per time step

        //  I think this is where we want to take the vectors and apply them to the linear and angular portions
        //  of the agent sim body.
        basicAnt.setLinearVelocity(velocity.add(basicAnt.getLinearVelocity()));

        double angle;
        Vector2 headed = new Vector2(basicAnt.getWorldCenter(), home);
        angle = headed.getAngleBetween(basicAnt.getLinearVelocity()); // radians
        angle = angle * 180 / Math.PI; // degrees
        if(angle > 0 && angle < 180 && headed.getMagnitude() < 5) {
            // Roughly:  right = motors[right]*headed.magnitude)
            basicAnt.applyTorque(-Math.PI/4);
        }
        else if(angle < 0 && angle > -180 && headed.getMagnitude() < 5) {
            // Roughly:  right = motors[left]*headed.magnitude)
            basicAnt.applyTorque(Math.PI / 4);
        }
    }

    /**
     * Checks if Agent_Ant reached home
     */
    private void checkHome() {
        if( ((position.x > home.x - TOLERANCE) && position.x < home.x + TOLERANCE) &&
                (position.y > home.y - TOLERANCE && position.y < home.y + TOLERANCE) ) {
           // Add code for resource drop off here
        }
    }

    /**
     * Steers ant towards home
     */
    private void headHome(ArrayList<Ant> allAgent_Ants) {
        Vector2 sep = separate(allAgent_Ants);    // creates a 2D vector to keep ants from smashing into one another (we are modeling UAVs)
        sep = sep.multiply(1.5);
        Vector2 go = seek(home);          // head home
        go = go.multiply(1.5);
        applyForceMine(sep);                  // apply separation vector.
        applyForceMine(go);
    } // end headHome()

    /**
     * Checks to see if an Agent_Ant needs to head home to refuel
     */
    private void returnHome() {
        // Distance = speed * time
        double d = velocity.getMagnitude() * this.lifespan; // how far the Agent_Ant can travel
        if(!headedHome && d <= position.distance(home) + 100) { // added in a 100 buffer to make sure they can reach home
            headedHome = true;
        }
        else if(reservoir.size() > 0) {
            headedHome = true; // return home with the resource
        }
    } // end returnHome()

    /**
     * Main function for an Ant.  It maintains sep from other Ants while moving towards its target
     *
     * @param allAgent_Ants
     */
    private void check(ArrayList<Ant> allAgent_Ants) {
        Vector2 sep = separate(allAgent_Ants);    // separation vector
        applyForceMine(sep);          // apply separation vector
    } // end check()

    /**
     * Calculates a new position that heads towards the target passed in.
     *
     * @param target
     * @return
     */
    Vector2 seek(Vector2 target) {
        Vector2 desired = target.difference(position); // A vector pointing from the position to the target
        desired.normalize();
        desired.multiply(maxspeed); // scale to maxspeed
        // Steering = Desired minus Velocity
        Vector2 steer = desired.difference(velocity); //new Vector2(desired,velocity); //Vector2.sub(desired,velocity);
        steer.multiply(1/maxforce);  // Limit to maximum steering force

        /**
         * TODO:  ok, we need to make sure we continuosly update the position vector.
         * We also need to apply these forces to the ants linear velocity,
         * and, in all likelihood, its torque.
         */
        return steer;
    }

    /**
     * Method checks for nearby basicBoids and steers away
     * @param allAgent_Ants
     * @return
     */
    Vector2 separate (ArrayList<Ant> allAgent_Ants) {
        Vector2 steer = new Vector2(0, 0);
        int count = 0;
        // For every boid in the system, check if it's too close
        for (Ant other : allAgent_Ants) {
            double d = position.distance(other.position); //new Vector2(position,other.position).getMagnitude();
            // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
            if ((d > 0) && (d < sepDist)) {
                // Calculate vector pointing away from neighbor
                Vector2 diff = position.difference(other.position); //new Vector2(position,other.position);
                diff.normalize();
                diff.divide(d);        // Weight by distance
                steer.add(diff);
                count++;            // Keep track of how many
            }
        } // end for
        // Average -- divide by how many
        if (count > 0) {
            steer.divide((float)count);
        }
        // As long as the vector is greater than 0
        if (steer.getMagnitude() > 0) {
            steer.setMagnitude(maxspeed);
            steer = steer.difference(velocity); // steer - velocity; //steer.sub(velocity); <-- hopefully this
            steer.multiply(1/maxforce); // this is a hack to limit the vector
        }
        return steer;
    } // end separation()


    /**
     * Add's force vector to current acceleration vector.
     *
     * @param force
     */
    private void applyForceMine(Vector2 force) {
        acceleration.add(force);
    }
    /**
     * Update the Agent_Ant's current velocity and position.
     */
    void update(ArrayList<Resource> resources) {
        // Update velocity
        velocity = velocity.add(acceleration);
        // Limit speed
        velocity.normalize();
        velocity.multiply(maxspeed);
        position.add(velocity);
        // Reset acceleration to 0 each cycle
        acceleration = new Vector2(0,0);

        // Collect resource if within range
        double bestDist = Double.MAX_VALUE;
        Resource toGet = null;
        for(Resource res : resources) {
            double distance = res.location.distance(position);
            if (distance <= SENSOR_RANGE) {
                if (distance <= 0.5 && distance < bestDist) {
                    bestDist = distance;
                    toGet = res;
                }
            }
        }
        if(toGet != null) {
            reservoir.add(toGet); // should be the closest resource to pick up
            velocity = velocity.add(seek(toGet.location));
        }
        for(Resource iOwn : reservoir) {
            resources.remove(iOwn); // remove from global resource list
        }
        if(reservoir.size() > 0) { // agent should live longer because it has a resource
            this.incLife();
        }

        // making sure the ant knows where it is
        x_pos = position.x;
        y_pos = position.y;

    } // end update()


    /**
     * This builds off of the typical boids application where we feed in a vector the ant should head towards.
     * This is also where we can begin having smarter agents who will only travel towards the closest food as
     * long as it is there.
     * @param target
     */
    /**
    public void seek(Vector2 target) {

        // Let's head towards it -- right now this will cause conflicts as it will head towards
        // the last food processed if it sees more than one.  Could add a little overhead and
        // have it head to the nearest if we wanted to (just add that as a todo), may also want
        // to limit how much the ant can intake at a time.  But, for now, winner takes all!
        Vector2 distance = new Vector2(home,basicAnt.getWorldCenter());
        double angle;
        angle = distance.getAngleBetween(basicAnt.getLinearVelocity()); // radians
        angle = angle * 180 / Math.PI; // degrees
        if(angle > TOLERANCE) {
            basicAnt.applyTorque(0.5*distance.getMagnitude());
        }
        else if(angle < -TOLERANCE) {
            basicAnt.applyTorque(-0.5*distance.getMagnitude());
        }
        basicAnt.setLinearVelocity(distance.add(basicAnt.getLinearVelocity()));

        Vector2 normal1 = this.basicAnt.getTransform().getTransformedR(new Vector2(0.0, 1.0));
        normal1.multiply(MAX_VELOCITY); // add to our position
        basicAnt.applyForce(normal1);

        // make sure the linear velocity is already in the direction of the tank front
        Vector2 normal = this.basicAnt.getTransform().getTransformedR(new Vector2(0.0, 1.0));
        double defl = basicAnt.getLinearVelocity().dot(normal);

        // clamp the velocity
        defl = Interval.clamp(defl, 0.5, MAX_VELOCITY);
        basicAnt.setLinearVelocity(normal.multiply(defl));

        // clamp the angular velocity
        double av = basicAnt.getAngularVelocity();
        av = Interval.clamp(av, -MAX_TORQUE, MAX_TORQUE);
        basicAnt.setAngularVelocity(av);

        // Borrowed from my boid code
        Vector2 desired = new Vector2(target,basicAnt.getWorldCenter()); // A vector pointing from the position to the target
        desired.normalize();
        desired.multiply(MAX_VELOCITY);         // Scale to maximum speed

        // Steering = Desired minus Velocity
        Vector2 steer = new Vector2(desired,this.getLinearVelocity()); //   Vector2.sub(desired,velocity);
        Vector2 normal = this.basicAnt.getTransform().getTransformedR(new Vector2(0.0, 1.0));
        double defl = basicAnt.getLinearVelocity().dot(normal);

        // clamp the velocity
        defl = Interval.clamp(defl, 0.5, MAX_VELOCITY);
        basicAnt.setLinearVelocity(normal.multiply(defl));

        // update the internal x and y to make things easier
        x_pos = this.basicAnt.getWorldCenter().x;
        y_pos = this.basicAnt.getWorldCenter().y;

        //steer.limit(maxforce);  // Limit to maximum steering force
        //return steer;
    }*/

    public void render(Graphics2D g, double scale) {
        super.render(g, scale, Color.CYAN);
        //if(reservoir.size() > 0 || lifespan < 10) { // head home
        //    seek(home);
        //}
        //else if(reservoir.size() > 0) {
        //    seek(reservoir.get(0).location); // head to the resource
       // }
       // else {
        //    seek(randomHeading()); // Move in a random direction, much like an ant would
       // }
    }

    private Vector2 randomHeading() {
        return new Vector2(1,1);
    }

    private void borders() {
        int minX = 0;
        int maxX = 10;
        int minY = 0;
        int maxY = 10;

       // System.out.println("before: " + this.id + " x: " + this.x_pos + " y: "+ this.y_pos); //+ " x':" + this.basicAnt.getWorldCenter().x + " y':" + this.basicAnt.getWorldCenter().y);

        if ((this.basicAnt.getWorldCenter().x < minX) || (this.basicAnt.getWorldCenter().x > maxX)) {
            x_pos = -1*x_pos;
            this.basicAnt.getWorldCenter().set(x_pos,y_pos);
        }
        if ((this.basicAnt.getWorldCenter().y < minY) || (this.basicAnt.getWorldCenter().y > maxY)) {
            y_pos = -1*y_pos;
            this.basicAnt.getWorldCenter().set(x_pos,-1*y_pos);
        }
        //System.out.println(this.id + " x: " + this.x_pos + " y: "+ this.y_pos); //+ " x':" + this.basicAnt.getWorldCenter().x + " y':" + this.basicAnt.getWorldCenter().y);
    } // end borders()

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    /**
     * Decrements the agent's lifespan by one.
     */
    public void decLife() {
        this.lifespan--;
        if(this.lifespan <= 0) {
            this.alive = false;
        }
    }

    public void incLife() {
        this.lifespan++;
    }

    /**
     * Equals
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ant ant = (Ant) o;
        return Objects.equals(id, ant.id);
    }

    /**
     * Hash code
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Vector2 getHome() {
        return home;
    }
}

/**
 * // Slow, especially for a lot of ants, but it's what we will run with for now
 *         double bestDist = Double.MAX_VALUE;
 *         Resource toGet = null;
 *         for(Resource res : resources) {
 *             Vector2 distance = new Vector2(res.location, basicAnt.getWorldCenter());
 *             if (distance.getMagnitude() <= SENSOR_RANGE) {
 *                 if (distance.getMagnitude() <= 0.5 && distance.getMagnitude() < bestDist) {
 *                     bestDist = distance.getMagnitude();
 *                     toGet = res;
 *                 }
 *             }
 *         }
 *         if(toGet != null) {
 *             reservoir.add(toGet); // should be the closest resource to pick up
 *         }
 *         for(Resource iOwn : reservoir) {
 *             resources.remove(iOwn); // remove from global resource list
 *         }
 *         if(reservoir.size() > 0) { // agent should live longer because it has a resource
 *             this.incLife();
 *         }
 */
