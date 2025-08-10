package big.modules.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.events.MessageReceiveEvent;
import big.modules.network.ClientNetworkHandler;
import big.modules.network.packet.Packet;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class MessageS2CPacket implements Packet<ClientNetworkHandler> {
    public String text;
    public MessageS2CPacket(String text){
        this.text=text;
    }
    public MessageS2CPacket(JSONObject o){
        this.text=PacketUtil.getString(o,"text");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"text",text);
        PacketUtil.putPacketType(o,getType());
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        cs.EVENT_BUS.post(MessageReceiveEvent.get(text));
    }

    @Override
    public String getType() {
        return "message";
    }
}
