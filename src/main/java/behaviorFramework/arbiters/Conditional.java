package behaviorFramework.arbiters;

import Sample.MyState;
import Vehicles.State;
import behaviorFramework.Action;
import behaviorFramework.ArbitrationUnit;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * This arbiter examines a boolean return from the State
 * and executes a TRUE or a FALSE branch.
 *
 * The new action has the same vote as the selected sub-structure.
 *
 * Action components: any in sub-structure
 */
public class Conditional extends ArbitrationUnit {
    String condition;
    State state;
    String stateClassName; // = new String ("Vehicles.State");
    Method methodCall;

    /**
     * The Conditional arbiter needs the method name as a
     * String and the State object that holds that method.
     *
     * @param condition
     * @param s State object to query for the conditional
     */
    public Conditional(String condition, State s) {
        super();
        this.condition = condition;
        state = s;
        stateClassName = state.getClass().getName();
        locateMethod(); // Get a link to the State method named in condition
    }

    public Action evaluate(Collection<Action> actionSet) {
        Action action = new Action();
        try {
            /*
             * Method must return a boolean for this arbiter
             * to function properly.
             */
            boolean response = (boolean)methodCall.invoke(state, null);

            // IF condition TRUE execute first branch
            if (response) {
                action = (Action)actionSet.toArray()[0];
            } else { // condition is FALSE execute second branch
                action = (Action) actionSet.toArray()[1];
            }
        } catch (Exception e) {
            System.out.println("ERROR!: Failed to call boolean conditional: "+ condition + e.toString());
        }

        return action;
    }

    /**
     * Checks the current state for the Conditional method being called.
     * If not present, checks the parent class, etc.
     * @return boolean
     */
    private boolean locateMethod() {
        try {
            Class c = Class.forName(stateClassName);

            methodCall = c.getDeclaredMethod(condition, null);
            methodCall.setAccessible(true);
            boolean response = (boolean) methodCall.invoke(state, null);
        } catch (NoSuchMethodException e) {
            try {
                stateClassName = Class.forName(stateClassName).getSuperclass().getName();
            } catch (Exception e2) {
                System.out.println("No Method Named: " + condition + " in " + state.getClass().getName() + " or parents.");
                System.exit(0);
            }
            //stateClassName = state.getClass().getSuperclass().getName();
            locateMethod();
        } catch (Exception e) {
            System.out.println(this.getClass().getName() + " No ClassName Found ");
            System.exit(0);
        }
        return true;
    }
}
