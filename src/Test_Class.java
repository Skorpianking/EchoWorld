import framework.SimulationFrame;
import org.dyn4j.world.World;

public class Test_Class extends SimulationFrame {
    // See if we can find the dyn objects
    World myWorld = new World();

    /**
     * Constructor.
     * <p>
     * By default creates a 800x600 canvas.
     *
     * @param name  the frame name
     * @param scale the pixels per meter scale factor
     */
    public Test_Class(String name, double scale) {
        super(name, scale);
    }

    @Override
    protected void initializeWorld() {

    }
}
