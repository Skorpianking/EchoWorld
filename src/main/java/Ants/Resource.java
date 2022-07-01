package Ants;

import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

/**
 * Making resources a class so it can have coordinates for ants to compare to.  Resources will
 * also have a string value which indicates its 'type.'
 *
 * Created:  25 Apr 2022
 * Last Update:  25 Apr 2022
 */
public class Resource {
    public String type; // A,B,C, or D -- whatever we may want
    public Vector2 location;
    public Color color = Color.GREEN;

    /**
     * Basic constructor, however, it assigns a random resource and a random location
     */
    public Resource() {
        this.type = randomType();
        location = randomLocation();
    }

    /**
     * User sets type, random location assigned
     * @param type
     */
    public Resource(String type) {
        this.type = type;
        location = randomLocation();
    }

    /**
     * User sets everything
     *
     * @param type
     * @param location
     */
    public Resource(String type, Vector2 location) {
        this.type = type;
        this.location = location;
    }

    public Resource(Resource res) {
        this.type = res.type;
        this.location = res.location;
    }

    /**
     * Returns a random 2d vector
     * @return
     */
    private Vector2 randomLocation() {
        int max = 20;
        int min = -20;
        Math.floor(Math.random()*(max-min+1)+min);
        return(new Vector2(Math.floor(Math.random()*(max-min+1)+min),Math.floor(Math.random()*(max-min+1)+min)));
    }


    /**
     * Returns a String that represents some random resource type
     * @return
     */
    private String randomType(){
        Random rand = new Random();
        int next = rand.nextInt(4);
        String ret = "";

        switch (next) {
            case 0:
                ret = "A";
                break;
            case 1:
                ret = "B";
                break;
            case 2:
                ret = "C";
                break;
            case 3:
                ret = "D";
                break;
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(type, resource.type) && Objects.equals(location, resource.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, location);
    }
}
