package Braitenburg;

import framework.SimulationBody;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

public class Light extends SimulationBody {
    String type = "L"; // L for Light because I am clever

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
