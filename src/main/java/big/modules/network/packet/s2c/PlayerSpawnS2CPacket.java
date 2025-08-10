package big.modules.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.modules.entity.player.ClientPlayerEntity;
import big.modules.network.ClientNetworkHandler;
import big.modules.network.packet.Packet;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class PlayerSpawnS2CPacket implements Packet<ClientNetworkHandler> {
    public ClientPlayerEntity player;
    public PlayerSpawnS2CPacket(ClientPlayerEntity player) {
        this.player = player;
    }
    public PlayerSpawnS2CPacket(JSONObject o) {
        JSONObject o2=o.getJSONObject("entity");
        this.player= ClientPlayerEntity.fromJSON(o2.getJSONObject("data"));
    }

    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());

        JSONObject o3=new JSONObject();
        o3.put(PacketUtil.getShortVariableName("type"),player.getType());
        o3.put("data",player.toJSON());
        o.put("entity",o3);
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        cs.player=player;
        cs.addEntity(player);
        cs.generateGroundBlocks(true);
    }

    @Override
    public String getType() {
        return "player_respawn";
    }
}
