import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Each QueueServer is responsible for a single queue, accessible through the
 * ExchangeServer via the name of the QueueServer
 */

public class QueueServer extends MessageServer {

    private volatile ArrayBlockingQueue<String> messageQueue;

    public QueueServer(String name) {
        super(name, ServerType.QUEUE);
        messageQueue = new ArrayBlockingQueue<>(100, true);
    }

    /**
     * Adds a message to the queue
     */
    public synchronized boolean put(String message) {
        try {
            messageQueue.put(message);
            return true;
        }
        catch (InterruptedException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Attempts to return the first message in the queue, tries for 1024 seconds
     */
    public synchronized String get() {
        if (messageQueue.isEmpty()) {
            return "(FAILURE) Queue is empty!";
        }
        String result = null;
        try {
            result = messageQueue.poll(1024, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.out.println(e);
        }
        if (result == null) {
            result = "(FAILURE) Could not get message from the queue!";
        }
        return result + "\n(DONE)";
    }

    public synchronized String list() {
        if (messageQueue.isEmpty()) {
            return "(FAILURE) Queue is empty!";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Items in " + this.getName() + "=======\n");
        Object[] array = new Object[messageQueue.size()];
        messageQueue.toArray(array);
        for (int i = 0; i < array.length; i++) {
            sb.append(i);
            sb.append(" ");
            sb.append(array[i]);
            sb.append("\n");
        }
        sb.append("(DONE)");
        return sb.toString();
    }

}
