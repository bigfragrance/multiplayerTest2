package big.modules.network.packet.c2s;

import big.engine.math.util.PacketUtil;
import big.modules.network.ServerNetworkHandler;
import big.modules.network.packet.Packet;
import big.modules.network.packet.s2c.MessageS2CPacket;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class MessageC2SPacket implements Packet<ServerNetworkHandler> {
    public String message;
    public MessageC2SPacket(String msg) {
        this.message=msg;
    }
    public MessageC2SPacket(JSONObject o) {
        this.message=PacketUtil.getString(o,"text");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"text",message);
        PacketUtil.putPacketType(o,getType());
        return o;
    }

    @Override
    public void apply(ServerNetworkHandler serverNetworkHandler) {
        String text="<"+serverNetworkHandler.clientHandler.player.name +"> "+message;
        cs.multiClientHandler.clients.forEach(c->c.serverNetworkHandler.send(new MessageS2CPacket(text).toJSON()));
    }

    @Override
    public String getType() {
        return "message";
    }
}
