import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ExchangeInputThread extends Thread {

    private ExchangeServer server;

    public ExchangeInputThread(ExchangeServer server) {
        super(server.getName() + " Input Thread");
        this.server = server;
    }

    public void run() {
        BufferedReader keyboard;
        try {
            keyboard = new BufferedReader(new InputStreamReader(System.in));
            String command;
            System.out.print(server.getName() + " (exchange)> ");
            while ((command = keyboard.readLine()) != null) {
                String split[] = command.split(" ");
                if (split.length == 4 && split[0].equalsIgnoreCase("bind")) {
                    bindToQueue(split);
                }
                else {
                    System.err.println("To bind to a queue: bind [queue name] " +
                            "[queue address | queue host name] [queue port]");
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
                System.out.print(server.getName() + " (exchange)> ");
            }
        }
        catch (IOException e) {
            System.out.println("ExchangeInputThread: " + e);
        }
    }

    /**
     * Open a socket connection to QueueServer and binds to the queue
     */
    private synchronized void bindToQueue(String args[]) {
        String queueName = args[1]; // Name of the Queue
        String queueHost = args[2]; // Address of the Queue
        int qPort = Integer.parseInt(args[3]); // Port of the Queue
        boolean done = false;
        int retry = 1;
        while (!done) {
            try (Socket socket = new Socket(queueHost, qPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(socket.getInputStream()))
            ) {
                // We can only reach here if the socket connection was opened
                out.println("NAME?"); // Ask the queue for its name
                String input = in.readLine(); // QueueServer's response to "NAME?"
                if (!input.equalsIgnoreCase(queueName)) {
                    // We are trying to bind to the wrong queue server
                    System.out.println("(FAILURE) Check bind request: queue name does not match!");
                    // Exit
                } else {
                    // Try to bind
                    if (server.bind(queueName, new String[]{queueHost, args[3]})) {
                        System.out.println("(SUCCESS) Bound to " + queueName);
                    } else System.out.println("(FAILURE) Cannot bind to " + queueName);
                }
                done = true;
            } catch (IOException e) {
                if (retry <= 1024) {
                    try {
                        System.out.println("RETRYING IN " + retry + " SECONDS...");
                        Thread.sleep(1000 * retry);
                        retry = retry * 2;
                    } catch (InterruptedException f) {
                        System.out.println(f);
                    }
                } else {
                    System.out.println("ExchangeInputThread: " + e);
                    done = true;
                }
            }
        }
    }

}
