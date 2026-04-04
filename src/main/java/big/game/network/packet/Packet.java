package big.game.network.packet;

import big.game.network.ClientNetworkHandler;
import big.game.network.PacketType;
import org.json.JSONObject;

public interface Packet<T> {
    JSONObject toJSON();
    void apply(T t);
    PacketType getType();
}
