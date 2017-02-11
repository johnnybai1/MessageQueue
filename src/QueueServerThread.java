import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class QueueServerThread extends Thread {

    private QueueServer server = null;
    private Socket socket = null;

    public QueueServerThread(QueueServer server, Socket socket) {
        super("Queue Server Thread");
        this.server = server;
        this.socket = socket;
    }

    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            QueueProtocol qp = new QueueProtocol();
            String input, output;
            while ((input = in.readLine()) != null) {
                output = qp.processInput(server, input);
                out.println(output);
                if (output.equals("(DONE)")) {
                    break;
                }
                if (output.startsWith("(FAILURE)")) {
                    System.out.println(output);
                    break;
                }
            }
        }
        catch (IOException e) {
            System.out.println("QueueServerThread: " + e);
        }
    }
}
