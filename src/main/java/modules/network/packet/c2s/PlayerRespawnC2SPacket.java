package modules.network.packet.c2s;

import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import modules.entity.player.PlayerEntity;
import modules.entity.player.ServerPlayerEntity;
import modules.network.ServerNetworkHandler;
import modules.network.packet.Packet;
import modules.network.packet.s2c.PlayerSpawnS2CPacket;
import org.json.JSONObject;

import static engine.modules.EngineMain.cs;

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
        PlayerEntity e=serverNetworkHandler.clientHandler.player;
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
