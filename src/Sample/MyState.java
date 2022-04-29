package Sample;

import Braitenburg.State;

public class MyState extends State {
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
