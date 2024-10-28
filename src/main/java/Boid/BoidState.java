package Boid;

import Vehicles.BlackBoard;
import Vehicles.SensedObject;
import Vehicles.State;
import org.dyn4j.geometry.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This is a sample state object to show how to extend the base state and
 * maintain memory particular to your vehicle.</p>
 *
 * <p>Examples, processed sensor data, maps being built, locations resources
 * were last seen in, ...</p>
 *
 */
public class BoidState extends State {

    public Map<Integer, Neighbor> neighborList;
    BlackBoard board;

    public BoidState() {
        neighborList = new HashMap<Integer, Neighbor>();
    }

    /**
     *
     */
    public void tick() {
        neighborList.clear();

        for (SensedObject obj : sensedObjects) {
            if (obj.getType().contains("Boid")) {
                // Last three elements of the name are the ID#
                String ID = obj.getType().substring(obj.getType().length() - 3, obj.getType().length());
                int boidID = Integer.parseInt(ID);
                // Only want one return per boid, and retain smallest distance.
                if (neighborList.containsKey(boidID)) {
                    neighborList.get(boidID).distance = Math.min(neighborList.get(boidID).distance, obj.getDistance());
                } else {
                  // Need to get the Neighbors heading & velocity
                  Vector2 vec = new Vector2();
                  if (board.getMessage(boidID) != null) //initialization edge case
                    vec = board.getMessage(boidID);
                  neighborList.put(boidID, new Neighbor(obj.getDistance(), obj.getAngle(), vec));
                }
            }
        }
    }

    public void setBlackBoard(BlackBoard bb) {
      board = bb;
    }

     public class Neighbor {
        public double distance;
        public double angle;
        public Vector2 velocity;

        public Neighbor( double dist, double ang, Vector2 vec) {
          distance = dist;
          angle = ang;
          velocity = vec.copy();
        }
     }
}
