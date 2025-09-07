package big.modules.network.packet.c2s;

import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.PacketUtil;
import big.modules.entity.player.PlayerEntity;
import big.modules.entity.player.ServerPlayerEntity;
import big.modules.network.ServerNetworkHandler;
import big.modules.network.packet.Packet;
import big.modules.network.packet.s2c.PlayerSpawnS2CPacket;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class PlayerRespawnC2SPacket implements Packet<ServerNetworkHandler> {
    public PlayerRespawnC2SPacket() {

    }
    @Override
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        PacketUtil.putPacketType(o,getType());
        return o;
    }

    @Override
    public void apply(ServerNetworkHandler serverNetworkHandler) {
        ServerPlayerEntity e=serverNetworkHandler.clientHandler.player;
        if(e!=null){
            if(e.isAlive) return;
            e.respawn();
        }else{
            ServerPlayerEntity player=new ServerPlayerEntity(new Vec2d(0,0));
            player.team=cs.getTeam();
            cs.addEntity(player);
            serverNetworkHandler.clientHandler.player=player;
        }
    }

    @Override
    public String getType() {
        return "player_respawn";
    }
}
