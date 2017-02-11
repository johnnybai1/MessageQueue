/**
 * When an input is received by the exchange server, return an appropriate
 * response.
 * Responses are usually:
 * (FAILURE) to indicate something went wrong
 * (SUCCESS) to indicate client can continue
 * Name of server, for checks
 */

public class ExchangeProtocol {


    private static String checkPut(ExchangeServer server, String input) {
        String[] split = input.split(" ");
        if (split.length != 4) {
            return "(FAILURE) Usage: put [exchange name] [queue name] [message]";
        }
        if (!split[1].equalsIgnoreCase(server.getName())) {
            return "(FAILURE) Exchange name does not match this server's name";
        }
        if (!split[2].contains("*") && !server.queueIsBound(split[2])) {
            return "(FAILURE) Exchange is not bound to " + split[2];
        }
        return "(SUCCESS) " + input;
    }

    private static String checkPublish(ExchangeServer server, String input) {
        String[] split = input.split(" ");
        if (split.length != 4) {
            return "(FAILURE) Usage: publish [exchange name] [sub name] [message]";
        }
        if (!split[1].equalsIgnoreCase(server.getName())) {
            return "(FAILURE) Exchange name does not match this server's name";
        }
        if (server.getSubs(split[2]) == null) {
            return "(FAILURE) No clients subscribed to " + split[2] + " on this exchange server.";
        }
        return "(SUCCESS) " + input;
    }

    private static String checkSubscribe(ExchangeServer server, String input) {
        String[] split = input.split(" ");
        if (split.length != 4) {
            // Length is 4 because we append client info
            return "(FAILURE) Usage: subscribe [subscription name]";
        }
        return "(SUCCESS) " + input;
    }


    public String processInput(ExchangeServer server, String input) {
        String split[] = input.split(" ");
        String operation = split[0];
        if (input.equalsIgnoreCase("NAME?")) {
            // Someone asked for the server's name
            return server.getName();
        }
        if (input.equalsIgnoreCase("(DONE)")) {
            // Someone told us we're finished
            return "(DONE)";
        }
        switch (operation) {
            case "put": {
                // put e q m
                return checkPut(server, input);
            }
            case "publish": {
                // publish e n m
                return checkPublish(server, input);
            }
            case "subscribe": {
                // subscribe name clientAddress clientPort
                return checkSubscribe(server, input);
            }
            default: {
                return "(FAILURE) The operation [" + operation + "] is not supported.";
            }
        }
    }

}
