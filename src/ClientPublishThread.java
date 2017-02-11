import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * The only "clients" this thread responds to will be Exchange Servers that connect to it to broadcast a
 * published message this client subscribed to.
 */

public class ClientPublishThread extends Thread {

    private MessageClient client;
    private Socket socket;

    public ClientPublishThread(MessageClient client, Socket socket) {
        super("Client Publish Thread");
        this.client = client;
        this.socket = socket;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String input = null;
            while ((input = in.readLine()) != null && !input.equals("(DONE)")) {
                if (input.contains("Client>")) {
                    System.out.print(input);
                } else System.out.println(input);
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
