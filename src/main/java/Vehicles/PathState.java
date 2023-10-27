package Vehicles;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;

import java.util.Arrays;

/**
 * <p> PathState includes a sensor that records a path taken.
 * Path is encoded in a String with [0..7] starting at North and
 * rotating clockwise (N, NE, E, SE, S, SW, W, NW)</p>
 *
 */
public class PathState extends State {
    /**
     * Path is retained as a String
     */
    private int[] pathRecord = null;
    private int pathRIndex = 0;
    private int[] pathFollow;
    private int pathFIndex = 0;
    private boolean followPathValid = false;
    private boolean pathing = false;
    private int timestep;
    private double[] rawPath;

    private int PATH_STEP_SIZE = 30;

    public PathState() {
        rawPath = new double[PATH_STEP_SIZE];
        pathFollow = new int[50];
        Arrays.fill(pathFollow,9);
    }

    /**
     * Overloading don't need the LightStrength.
     * Each clock tick, clear the ray cast detected objects and update
     * path.
     */
    public void tick() {
        sensedObjects.clear();
        if (isHolding() && !isPathing())
            startPathing();
        if (!isHolding() && isPathing()) {
            endPathing();
            System.out.println("PATH: " + recordedPathToString());
        }
        if (pathing)
            updatePath();
    }

    public void startPathing() {
        pathing = true;
        timestep = 0;
        pathRIndex = 0;
        pathRecord = new int[50];
        followPathValid = false;
    }

    public void endPathing() {
        pathing = false;
    }

    /**
     * Reminder: heading is
     * 0 = x, counter-clockwise +0 to PI, clockwise -0 to PI
     */
    public void updatePath() {
        timestep++;
        if (timestep < PATH_STEP_SIZE) {
            rawPath[timestep] = heading;
        } else {
            timestep = 0;
            double compassDir = 0.0;
            for (int i = 0; i < PATH_STEP_SIZE; i++) {
                compassDir += rawPath[i];
            }
            compassDir = compassDir / PATH_STEP_SIZE;
            double slice = Math.PI/8;
            if (compassDir >= 5*slice && compassDir < 7*slice)
                pathRecord[pathRIndex] = 7;
                //path.concat("7"); // North-West
            if (compassDir >= (3*slice) && compassDir < (5*slice))
                pathRecord[pathRIndex] = 0; // path.concat("0"); // North
            if (compassDir >= slice && compassDir < 3*slice)
                pathRecord[pathRIndex] = 1; // path.concat("1"); // North-East
            if (compassDir >= 0.0 && compassDir < slice)
                pathRecord[pathRIndex] = 2; // path.concat("2"); // upper half of East
            if (compassDir >= -slice && compassDir < 0.0)
                pathRecord[pathRIndex] = 2; // path.concat("2"); // lower half of East
            if (compassDir >= -(7*slice) && compassDir < -(5*slice))
                pathRecord[pathRIndex] = 5; // path.concat("5"); // South-West
            if (compassDir >= -(5*slice) && compassDir < -(3*slice))
                pathRecord[pathRIndex] = 4; // path.concat("4"); // South
            if (compassDir >= -(3*slice) && compassDir < -slice)
                pathRecord[pathRIndex] = 3;// path.concat("3"); // South-East
            if (compassDir >= 7*slice)
                pathRecord[pathRIndex] = 6; // path.concat("6"); // upper half of West
            if (compassDir < -(7*slice))
                pathRecord[pathRIndex] = 6; // path.concat("6"); // lower half of West
            pathRIndex++;
            if (pathRIndex >= 50) // If for whatever reason it takes this long to get back, just loop in the array.
                pathRIndex = 0;
        }

    }

    // Compass to Heading and Heading to Compass methods?
    public void dropAtHome( Home h ) {
        super.dropAtHome(h);
        int dropPath[] = new int[50];

        endPathing();
        for (int i = 0; i < pathRIndex; i++)
            dropPath[pathRIndex-i] = (pathRecord[i]+4)%8; // Flip the compass directions and inverse path

        for (int i = pathRIndex; i < 50; i++)
          dropPath[i] = 9; // Insert a 9 at the end indicating end of path

        pathFollow = h.receivePath(dropPath);
        pathFIndex = 0;
        if (pathFollow[0] != 9)
            followPathValid = true;
        else
            followPathValid = false;
    }

    public String recordedPathToString() {
        IntegerArray rtn = new IntegerArray(pathRecord);
        return rtn.toString();
    }

    public boolean isPathing() {return pathing;}

    public void receivePath(int[] path) {
        pathFollow = Arrays.copyOf(path,50);
        pathFIndex = 0;
    }

    /**
     * Get the next step in the recorded path. A 9 is that there are no
     * more steps
     *
     * @return [0...8,9] compass dir or null
     */
    public int getNextStep() {
        int rtn = pathFollow[pathFIndex++];

        if (pathFIndex >=50 || rtn == 9)
            pathFIndex--;

        return rtn;
    }

    public int getStep(int index) {
        int rtn = 9;

        if (index >= 0 || index < 50)
            rtn = pathFollow[index];

        return rtn;
    }

    public boolean isFollowPathValid() {
        return followPathValid;
    }
}
