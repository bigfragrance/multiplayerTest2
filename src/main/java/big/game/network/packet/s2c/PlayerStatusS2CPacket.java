package big.game.network.packet.s2c;

import big.engine.util.PacketUtil;
import big.game.entity.player.PlayerEntity;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class PlayerStatusS2CPacket implements Packet<ClientNetworkHandler> {
    //dont use this packet for now
    public JSONObject data;

    public PlayerStatusS2CPacket(JSONObject o) {
        this.data=o;
    }
    public PlayerStatusS2CPacket(PlayerEntity playerEntity) {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());
        playerEntity.addJSON(o);
    }

    @Override
    public JSONObject toJSON() {
        return data;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        if(cs.player!=null){
            cs.player.updateStatus(data);
        }
    }

    @Override
    public String getType() {
        return "player_status";
    }
}
