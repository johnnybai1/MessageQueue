/**
 * This is the main program driver for the Exchange and Queue servers
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class MessageServer {

    public enum ServerType {EXCHANGE, QUEUE, CLIENT}

    private String name = null; // the name of this server
    private ServerType serverType;
    private ServerSocket serverSocket; // this server's socket
    private String address = null; // address to connect to this server
    private int port = -1; // the port to connect to this server

    private ExecutorService threadPool = null;

    public MessageServer(String name, ServerType serverType) {
        this.name = name;
        this.serverType = serverType;
        try {
            serverSocket = new ServerSocket(0); // automatically find a port
            port = serverSocket.getLocalPort();
            address = serverSocket.getInetAddress().getHostAddress();
        }
        catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("Server created: " + port);
        threadPool = Executors.newCachedThreadPool();
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPortNumber() {
        return port;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java MessageServer create <exchange | queue> <name>");
            System.exit(1);
        }
        if (args[0].equalsIgnoreCase("create")) {
            String type = args[1];
            String name = args[2];
            if (type.equalsIgnoreCase("exchange")) {
                // Create an EXCHANGE server
                ExchangeServer server = new ExchangeServer(name);
                ExecutorService threadPool = server.getThreadPool();
                threadPool.submit(new ExchangeInputThread(server));
                try (ServerSocket serverSocket = server.getServerSocket()) {
                    while (true) {
                        threadPool.submit(new ExchangeServerThread(server, serverSocket.accept()));
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            if (type.equalsIgnoreCase("queue")) {
                // Create a QUEUE server
                QueueServer server = new QueueServer(name);
                ExecutorService threadPool = server.getThreadPool();
                try (ServerSocket serverSocket = server.getServerSocket()) {
                    while (true) {
                        threadPool.execute(new QueueServerThread(server, serverSocket.accept()));
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
            } else {
                System.err.println("Usage: java MessageServer create <exchange | queue> <name>");
                System.exit(1);
            }
        } else {
            System.err.println("Usage: java MessageServer create <exchange | queue> <name>");
            System.exit(1);
        }
    }
}

