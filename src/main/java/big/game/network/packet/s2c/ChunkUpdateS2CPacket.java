package big.game.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import big.game.world.Chunk;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class ChunkUpdateS2CPacket implements Packet<ClientNetworkHandler> {
    public long id;
    public JSONObject json;
    public ChunkUpdateS2CPacket(long id, JSONObject json) {
        this.id = id;
        this.json = json;
    }
    public ChunkUpdateS2CPacket(JSONObject json){
        this(PacketUtil.getLong(json,"id"),PacketUtil.getJSONObject(json,"data"));
    }
    @Override
    public JSONObject toJSON() {
        JSONObject json=new JSONObject();
        PacketUtil.putPacketType(json,getType());
        PacketUtil.put(json,"id",id);
        PacketUtil.put(json,"data",this.json);
        return json;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        Chunk c=Chunk.fromJSON(json);
        if(c!=null) {
            cs.world.worldChunks.put(id, c);
        }
    }

    @Override
    public String getType() {
        return "chunk_update";
    }
}
