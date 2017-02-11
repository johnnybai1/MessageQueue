import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Client process will communicate with exchange server by issuing a
 * put request.
 * The Client process will communicate with queue server by issuing
 * list/get requests.
 * The Client has a server thread that will listen for published messages
 */

public class MessageClient extends MessageServer {

    public MessageClient() {
        super("client", ServerType.CLIENT);
    }

    private static boolean validConnection(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            System.err.println(e);
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java MessageClient [IP address | host name] [port number]");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        if (validConnection(host, port)) {
            MessageClient client = new MessageClient();
            ExecutorService threadPool = client.getThreadPool();
            threadPool.submit(new ClientInputThread(client, host, port));
            try (ServerSocket clientSocket = client.getServerSocket()) {
                while (true) {
                    threadPool.submit(new ClientPublishThread(client, clientSocket.accept()));
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}
