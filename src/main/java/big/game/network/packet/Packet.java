package big.game.network.packet;

import org.json.JSONObject;

public interface Packet<T> {
    JSONObject toJSON();
    void apply(T t);
    String getType();
}
