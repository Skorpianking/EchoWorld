package Sample.behaviors;

import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.Behavior;

public class Drop extends Behavior {

    public Drop() {
        super();
    }

    public Action genAction(State state) {
        assert (state != null);

        Action action = new Action();
        action.name = "Drop";
        action.setVote(0);

        if (state.isHolding()) {
            action.setDrop(true);
            action.setVote(1);
        }

        return action;
    }
}
