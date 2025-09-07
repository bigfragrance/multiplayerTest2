package big.modules.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import big.modules.entity.player.PlayerEntity;
import big.modules.network.ClientNetworkHandler;
import big.modules.network.packet.Packet;
import big.modules.screen.TankChooseScreen;
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
