/**
 * When an input is received by the queue server, return an appropriate
 * response.
 */
public class QueueProtocol {


    public String processInput(QueueServer server, String input) {
        if (input.equalsIgnoreCase("NAME?")) {
            // Someone asked for the server's name
            return server.getName();
        }
        if (input.equalsIgnoreCase("(DONE)")) {
            // Someone told us we're finished
            return "(DONE)";
        }
        String split[] = input.split(" ");
        if (split[0].equalsIgnoreCase("put")) {
            // This means the it came from the queue, which will not be permitted
            return "(FAILURE) Connect to an exchange server to put!";
        }
        if (split.length == 5 && split[1].equalsIgnoreCase("put")) {
            // We do the put if we receive "(SUCCESS) put e q m" from Exchange
            server.put(split[4]);
            return "(DONE)";
        }
        if (split.length == 2) {
            if (!server.getName().equalsIgnoreCase(split[1])) {
                return "(FAILURE) You are connected to the wrong queue server.";
            }
            if (split[0].equalsIgnoreCase("list")) {
                return server.list();
            }
            if (split[0].equalsIgnoreCase("get")) {
                return server.get();
            }
        }
        return "(FAILURE) Unknown Input";
    }

}
