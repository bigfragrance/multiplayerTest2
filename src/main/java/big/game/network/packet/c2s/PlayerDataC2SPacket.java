package big.game.network.packet.c2s;

import big.engine.math.Vec2d;
import big.engine.util.PacketUtil;
import big.game.entity.player.PlayerEntity;
import big.game.entity.player.ServerPlayerEntity;
import big.game.network.PacketType;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import big.server.NettyServerMain;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class PlayerDataC2SPacket implements Packet<ServerNetworkHandler> {
    public String name;
    public PlayerDataC2SPacket(String name) {
        this.name=name;
    }
    public PlayerDataC2SPacket(JSONObject o) {
        this.name=PacketUtil.getString(o,"name");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());
        PacketUtil.put(o,"name",name);
        return o;
    }

    @Override
    public void apply(ServerNetworkHandler serverNetworkHandler) {
        ServerPlayerEntity e=serverNetworkHandler.clientHandler.player;
        e.name= name;
        if(NettyServerMain.connectedPlayersEntity.containsKey(e.name.hashCode())){
            NettyServerMain.connectedPlayersEntity.get(e.name.hashCode()).set(e);
        }
        cs.multiClientHandler.clients.forEach(c -> {
            if(c.player.id!=e.id){
                c.serverNetworkHandler.sendPlayerData(e);
            }
        });
    }
    @Override
    public PacketType getType() {
        return PacketType.PLAYER_DATA_C2S;
    }
}
