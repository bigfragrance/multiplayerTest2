package big.game.network.packet.c2s;

import big.engine.util.PacketUtil;
import big.game.entity.Entity;
import big.game.network.PacketType;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import big.game.network.packet.s2c.ChunkUpdateS2CPacket;
import big.game.network.packet.s2c.EntitySpawnS2CPacket;
import big.game.world.Chunk;
import big.game.world.ChunkPos;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class WantEntityC2SPacket implements Packet<ServerNetworkHandler> {
    public long id;
    public WantEntityC2SPacket(long id) {
        this.id=id;
    }
    public WantEntityC2SPacket(JSONObject o) {
        this.id = PacketUtil.getLong(o,"id");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());
        PacketUtil.put(o,"id",id);
        return o;
    }

    @Override
    public void apply(ServerNetworkHandler serverNetworkHandler) {
        Entity e=cs.entities.get(id);
        if(e!=null){
            serverNetworkHandler.sendEntitySpawn(e);
        }
    }
    @Override
    public PacketType getType() {
        return PacketType.WANT_ENTITY_C2S;
    }
}
