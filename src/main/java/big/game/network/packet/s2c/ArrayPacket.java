package big.game.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import org.json.JSONArray;
import org.json.JSONObject;

public class ArrayPacket implements Packet<ClientNetworkHandler> {
    private JSONArray array;
    public ArrayPacket(JSONArray array){
        this.array=array;
    }
    public ArrayPacket(JSONObject o){
        this.array=PacketUtil.getJSONArray(o,"array");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject json=new JSONObject();
        PacketUtil.put(json,"array",array.toString());
        PacketUtil.putPacketType(json,getType());
        return json;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        for(int i=0;i<array.length();i++){
            JSONObject o=array.getJSONObject(i);
            clientNetworkHandler.apply(o);
        }
    }

    @Override
    public String getType() {
        return "array_packet";
    }
}
