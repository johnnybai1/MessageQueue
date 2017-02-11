import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * The ExchangeServer performs two functions:
 * 1. Binds to a QueueServer upon request
 * 2. Sends a put request from the Client to the QueueServer(s)
 */

public class ExchangeServer extends MessageServer {

    private volatile HashMap<String, String[]> bindings;
    private volatile HashMap<String, LinkedList<String[]>> subscriptions;

    public ExchangeServer(String name) {
        super(name, ServerType.EXCHANGE);
        bindings = new HashMap<>();
        subscriptions = new HashMap<>();
    }

    public synchronized boolean bind(String queueName, String[] queueInfo) {
        if (bindings.containsKey(queueName)) {
            // Check if connection is still alive
            try (Socket socket = new Socket(getQueueAddress(queueName), getQueuePortNumber(queueName))) {
                // Still alive! Don't do anything
                return false;
            } catch (IOException e) {
                // Not alive, let execution continue
                bindings.put(queueName, queueInfo);
                return true;
            }
        }
        bindings.put(queueName, queueInfo);
        return true;
    }

    public synchronized Set<String> getBindKeys() {
        return bindings.keySet();
    }

    public boolean queueIsBound(String queueName) {
        return bindings.containsKey(queueName);
    }

    public String getQueueAddress(String queueName) {
        if (bindings.containsKey(queueName)) {
            String[] queueInfo = bindings.get(queueName);
            return queueInfo[0];
        }
        return null;
    }

    public int getQueuePortNumber(String queueName) {
        if (bindings.containsKey(queueName)) {
            String[] queueInfo = bindings.get(queueName);
            return Integer.parseInt(queueInfo[1]);
        }
        return -1;
    }

    public synchronized boolean subscribe(String name, String[] clientInfo) {
        LinkedList<String[]> subs = subscriptions.get(name);
        if (subs == null) {
            subs = new LinkedList<>();
        }
        subs.addLast(clientInfo);
        subscriptions.put(name, subs);
        return true;
    }

    public synchronized Set<String> getSubKeys() {
        return subscriptions.keySet();
    }

    public LinkedList<String[]> getSubs(String name) {
        return subscriptions.get(name);
    }

    public void publish(String name, String message) {
        LinkedList<String[]> subs = subscriptions.get(name);
        if (subs != null) {
            for (String[] info : subs) {
                String address = info[0];
                int portNumber = Integer.parseInt(info[1]);
                try (Socket socket = new Socket(address, portNumber);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                ) {
                    out.println("Message from " + name + ": " + message);
                    out.println("Client> ");
                }
                catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

}
