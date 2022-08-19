package Sample.behaviors;

import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

public class Drop extends Behavior {
    private int counter;

    public Drop() {
        super();
        counter = 0;
    }

    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "Drop";
        action.setVote(0);

        if (state.isHolding())
            counter++;
        if (counter > 25) {
            action.setDrop(true);
            action.setVote(1);
            counter = 0;
        }

        return action;
    }
}
