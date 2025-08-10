package big.server;

import org.json.JSONObject;

import java.util.ArrayList;

public class MultiClientHandler {
    public ArrayList<ClientHandler> clients = new ArrayList<>();
    public MultiClientHandler() {

    }
    public void addClient(ClientHandler client) {
        clients.add(client);
    }
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
    public void sendToAll(JSONObject o) {
        clients.forEach(client -> client.send(o));
    }
}
