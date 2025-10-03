package big.game.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import big.game.world.BlockState;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class BlockStateUpdateS2CPacket implements Packet<ClientNetworkHandler> {
    public int x;
    public int y;
    public BlockState blockState;
    public BlockStateUpdateS2CPacket(int x,int y,BlockState blockState){
        this.x=x;
        this.y=y;
        this.blockState=blockState;
    }
    public BlockStateUpdateS2CPacket(JSONObject json){
        this(PacketUtil.getInt(json,"x"),PacketUtil.getInt(json,"y"),BlockState.fromJSON(PacketUtil.getJSONObject(json,"blockState")));
    }
    public JSONObject toJSON(){
        JSONObject json=new JSONObject();
        PacketUtil.putPacketType(json,getType());
        PacketUtil.put(json,"x",x);
        PacketUtil.put(json,"y",y);
        PacketUtil.put(json,"blockState",blockState.toClientJSON());
        return json;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        cs.world.setBlockState(x,y,blockState);
    }

    @Override
    public String getType() {
        return "block_state_update";
    }
}
