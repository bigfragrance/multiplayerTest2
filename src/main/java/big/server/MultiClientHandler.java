package big.server;

import org.json.JSONObject;

import java.util.ArrayList;

public class MultiClientHandler {
    public ArrayList<NettyClientHandler> clients = new ArrayList<>();
    public MultiClientHandler() {

    }
    public void addClient(NettyClientHandler client) {
        clients.add(client);
    }
    public void removeClient(NettyClientHandler client) {
        clients.remove(client);
    }
    public void sendToAll(JSONObject o) {
        clients.forEach(client -> client.send(o));
    }
}
