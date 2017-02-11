import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class ExchangeServerThread extends Thread {

    private ExchangeServer server = null;
    private Socket socket = null;

    public ExchangeServerThread(ExchangeServer server, Socket socket) {
        super(server.getName() + " (Exchange Server Thread)");
        this.server = server;
        this.socket = socket;
    }

    private static String buildRegex(String string) {
        // We have 3 different types
        // queue*, qu*eue, *queue
        String[] split = string.split("\\*");
        if (split.length == 1) {
            // queue*
            return split[0] + ".*";
        }
        if (split.length == 2) {
            if (string.startsWith("*")) {
                // *queue
                return ".*" + split[1];
            } else {
                return split[0] + ".*" + split[1];
            }
        }
        return null;
    }

    private ArrayList<String> putCommands(String command) {
        // (SUCCESS) put e q m
        ArrayList<String> result = new ArrayList<>();
        String[] split = command.split(" ");
        if (split[3].contains("*")) {
            String regex = buildRegex(split[3]);
            for (String queueName : server.getBindKeys()) {
                if (queueName.matches(regex)) {
                    result.add(split[0] + " " + split[1] + " " + split[2] + " " + queueName + " " + split[4]);
                }
            }
        } else result.add(command);
        return result;
    }

    private synchronized String doPut(String command) {
        // (SUCCESS) put e q m
        String result = null;
        String[] split = command.split(" ");
        String queue = split[3];
        int retry = 1;
        while (true) {
            String host = server.getQueueAddress(queue);
            int port = server.getQueuePortNumber(queue);
            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                out.println(command); // Tell queue server "(SUCCESS) put e q m"
                result = in.readLine();
                return result;
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
                    System.out.println(e);
                    return "(TIMEOUT)";
                }
            }
        }
    }

    public void run() {
        // This thread runs when a socket connection is made.
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            ExchangeProtocol ep = new ExchangeProtocol();
            String input, output;
            while ((input = in.readLine()) != null) {
                output = ep.processInput(server, input); // Process the command received
                if (output.contains("(DONE)")) {
                    break;
                }
                if (output.contains("(FAILURE)")) {
                    out.println(output);
                    System.out.println(output);
                    break;
                }
                if (input.startsWith("put") && output.startsWith("(SUCCESS)")) {
                    // (SUCCESS) put e q m, means valid input
                    ArrayList<String> commands = putCommands(output);
                    for (String command : commands) {
                        String result = doPut(command);
                        out.println(result);
                    }
                    break;
                }
                if (input.startsWith("publish") && output.startsWith("(SUCCESS)")) {
                    // (SUCCESS) publish e s m
                    String[] split = output.split(" ");
                    String subName = split[3];
                    String message = split[4];
                    server.publish(subName, message);
                    out.println("(DONE)");
                    break;
                }
                if (input.startsWith("subscribe") && output.startsWith("(SUCCESS)")) {
                    // (SUCCESS) subscribe s address port
                    String[] split = output.split(" ");
                    String subName = split[2];
                    server.subscribe(subName, new String[]{split[3], split[4]});
                    out.println("(DONE)");
                    break;
                }
                out.println(output); // Send the results
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
