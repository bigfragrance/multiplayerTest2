package modules.network.packet.c2s;

import engine.math.util.PacketUtil;
import modules.entity.player.ServerPlayerEntity;
import modules.network.ServerNetworkHandler;
import modules.network.packet.Packet;
import modules.network.packet.s2c.BlockStateUpdateS2CPacket;
import modules.network.packet.s2c.ChunkUpdateS2CPacket;
import modules.weapon.Weapon;
import modules.world.BlockState;
import modules.world.Blocks;
import modules.world.Chunk;
import modules.world.ChunkPos;
import org.json.JSONObject;

import static engine.modules.EngineMain.cs;

public class WantChunkC2SPacket implements Packet<ServerNetworkHandler> {
    public int x;
    public int y;
    public WantChunkC2SPacket(int x,int y) {
        this.x=x;
        this.y=y;
    }
    public WantChunkC2SPacket(JSONObject o) {
        this.x = PacketUtil.getInt(o,"x");
        this.y = PacketUtil.getInt(o,"y");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());
        PacketUtil.put(o,"x",x);
        PacketUtil.put(o,"y",y);
        return o;
    }

    @Override
    public void apply(ServerNetworkHandler serverNetworkHandler) {
        Chunk c=cs.world.getChunk(x,y);
        if(c!=null){
            long id= ChunkPos.toLong(x,y);
            serverNetworkHandler.clientHandler.send(new ChunkUpdateS2CPacket(id,c.toClientJSON()).toJSON());
            /*int baseX=x*16;
            int baseY=y*16;
            for(int i=0;i<=15;i++){
                for(int j=0;j<=15;j++){
                    BlockState state=c.getBlockState(i,j);
                    if(state.getBlock()!= Blocks.AIR){
                        serverNetworkHandler.clientHandler.send(new BlockStateUpdateS2CPacket(baseX+i,baseY+j,state).toJSON());
                    }
                }
            }*/
        }
    }

    @Override
    public String getType() {
        return "want_chunk";
    }
}
