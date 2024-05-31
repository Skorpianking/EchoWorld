package Vehicles;

import Vehicles.Action;
import Vehicles.State;
import Vehicles.Vehicle;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.dyn4j.world.World;

// ActiveMQ STOMP packaging
import jakarta.jms.*;
import org.fusesource.stomp.jms.*;

public class NetworkVehicle extends Vehicle {
    /**
     * Using our own State object to store our vehicles personal data
     */
    private State state;
    private behaviorFramework.Action action = new behaviorFramework.Action();

    String user = "vehicle";
    String password = "password";
    String host = "localhost";
    int port = 61613;
    String output = "/state/"; // = "/state/{name}";
    String input = "/action/"; // = "/action/{name}"
    Session session;
    MessageProducer producer;
    MessageConsumer consumer;

    public NetworkVehicle() {
        state = new State();
    }

    /**
     * <p>The intialization method receives
     *
     */
    public void initializeNetwork() {

        //TODO: Write network socket initialization
        try {
            StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
            factory.setBrokerURI("tcp://" + host + ":" + port);

            Connection connection = factory.createConnection(user, password);
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // Name of state output channel is "/state/{name}"
            output.concat(name);
            Destination dest = new StompJmsDestination(output);
            producer = session.createProducer(dest);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            // Name of action input channel is "/action/{name}" need new JmsDestination.
            input.concat(name);
            dest = new StompJmsDestination(input);
            consumer = session.createConsumer(dest);
        } catch (Exception e) {
            System.out.println("Unable to connect to ActiveMQ server.");
            System.out.println("Please ensure that it is running and restart.");
            System.out.println(e);
            System.exit(0);
        }
    }

    /**
     * Every vehicle senses the world
     *
     * NetworkVehicle doesn't include an extended state. It is assumed the external
     * agent will maintain what it needs.
     *
     * @return true
     */
    public boolean sense(){
        // Update the base sensors first
        super.sense();

        if (session == null) // because of class loading, initialize isn't being called without a cast
            initializeNetwork();

        try {
            JsonObject object = new JsonObject();
            object = state.toJson();

            String jsonString = Jsoner.serialize(object);
            TextMessage msg = session.createTextMessage(jsonString);

            producer.send(msg);
        } catch (Exception e) {
            System.out.println("Unable to send to ActiveMQ server");
            System.out.println(e);
        }

        return true;
    }

    /**
     * Call the networked agent and get an action to execute
     *
     * @return the action to execute
     */
    public Action decideAction() {
        action.clear();

        // Get an action from the networked agent
        String msg = messagePoll();
        JsonObject obj = null;

        try {
            if (msg != null)
                obj = (JsonObject) Jsoner.deserialize(msg);
            if (obj != null)
                action.fromJson(obj);
        } catch (Exception e) {
            System.out.println("Invalid JSON: " + msg);
            System.out.println(e);
        }

        System.out.println(name + ": " + action.name + " " + action.getLeftWheelVelocity() + " " + action.getRightWheelVelocity());

        return action;
    }

    private String messagePoll () {
        String stringMsg = null;

        try {
            Message msg = consumer.receiveNoWait();
            if( msg instanceof  TextMessage ) {
                stringMsg = ((TextMessage) msg).getText();
            } else if (msg instanceof BytesMessage){ // Had to do this for Python. Receive BytesMessage and convert to String.
                BytesMessage byteMessage = (BytesMessage) msg;
                byte[] byteData = null;
                byteData = new byte[(int) byteMessage.getBodyLength()];
                byteMessage.readBytes(byteData);
                byteMessage.reset();
                stringMsg =  new String(byteData);
                System.out.println(stringMsg);
            } else if (msg != null ){
                System.out.println("Unexpected message type: "+msg.getClass());
            }
        } catch (Exception e) {
            System.out.println("Unable to parse incoming ActiveMQ message");
            System.out.println(e);
        }

        return stringMsg;
    }
}
