package Sample;

import Braitenburg.State;

/**
 * <p>This is a sample state object to show how to extend the base state and
 * maintain memory particular to your vehicle.</p>
 *
 * <p>Examples, processed sensor data, maps being built, locations resources
 * were last seen in, ...</p>
 *
 */
public class MyState extends State {
    /**
     * Only keeping an int in this sample state
     */
    private int myMemory;

    public MyState() {
        myMemory = 0;
    }

    public void setMyMemory(int m) {
        myMemory = m;
    }

    public int getMyMemory() {
        return myMemory;
    }
}
