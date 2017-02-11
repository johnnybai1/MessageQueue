import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientInputThread extends Thread {

    private MessageClient client;
    private String host; // Address we're connected to
    private int port; // Port we're connected to

    public ClientInputThread(MessageClient client, String host, int port) {
        super("Client Input Thread");
        this.client = client;
        this.host = host;
        this.port = port;
    }

    private static boolean validOperation(String operation) {
        return (operation.equalsIgnoreCase("put") || operation.equalsIgnoreCase("publish") ||
                operation.equalsIgnoreCase("subscribe") || operation.equalsIgnoreCase("list") ||
                operation.equalsIgnoreCase("get"));
    }

    public void run() {
        BufferedReader keyboard = null;
        try {
            System.out.print("Client> ");
            keyboard = new BufferedReader(new InputStreamReader(System.in));
            String command;
            while ((command = keyboard.readLine()) != null) {
                String[] split = command.split(" ");
                String operation = split[0];
                if (validOperation(operation)) {
                    if (operation.equalsIgnoreCase("subscribe")) {
                        command = command + " " + client.getAddress() + " " + client.getPortNumber();
                    }
                    // Connect to the server: Exchange or Queue
                    int retry = 1;
                    boolean done = false;
                    while (!done) {
                        try (Socket socket = new Socket(host, port);
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                        ) {
                            out.println(command); // Send the command to the server
                            String input;
                            while ((input = in.readLine()) != null) {
                                if (input.startsWith("(FAILURE)")) {
                                    System.out.println(input);
                                    done = true;
                                    break;
                                } else if (input.startsWith("(SUCCESS)") || input.startsWith("(DONE)")) {
                                    done = true;
                                    break;
                                } else System.out.println(input);
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
                                System.out.println(e);
                                done = true;
                            }
                        }
                    }
                }
                else {
                    System.err.println("The operation [" + split[0] + "] is not supported.");
                }
                System.out.print("Client> ");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
