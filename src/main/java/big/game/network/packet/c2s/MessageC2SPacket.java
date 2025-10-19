package big.game.network.packet.c2s;

import big.engine.util.PacketUtil;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import big.game.network.packet.s2c.AssetsS2CPacket;
import big.game.network.packet.s2c.MessageS2CPacket;
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
        if(message.equals(AssetsS2CPacket.RECEIVED)){
            serverNetworkHandler.clientHandler.dataSent=true;
            return;
        }
        if(message.equals(AssetsS2CPacket.NEED_UPDATE)){
            serverNetworkHandler.clientHandler.sendAssetsData();
            return;
        }
        String text="<"+serverNetworkHandler.clientHandler.player.name +"> "+message;
        cs.multiClientHandler.clients.forEach(c->c.serverNetworkHandler.send(new MessageS2CPacket(text).addHistory().toJSON()));
    }

    @Override
    public String getType() {
        return "message";
    }
}
