package modules.network.packet.s2c;

import engine.math.util.PacketUtil;
import engine.math.util.Util;
import modules.network.ClientNetworkHandler;
import modules.network.packet.Packet;
import org.json.JSONArray;
import org.json.JSONObject;

import static engine.modules.EngineMain.cs;

public class PlayerDataS2CPacket implements Packet<ClientNetworkHandler> {
    public double[] skillPoints;
    public int skillPointCount;
    public PlayerDataS2CPacket(double[] skillPoints, int skillPointCount) {
        this.skillPoints = skillPoints;
        this.skillPointCount = skillPointCount;
    }
    public PlayerDataS2CPacket(JSONObject o) {
        this(Util.getDoubles(PacketUtil.getJSONArray(o, "skillPoints")),PacketUtil.getInt(o,"skillPointCount"));
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        PacketUtil.putPacketType(o,getType());
        PacketUtil.put(o,"skillPoints",skillPoints);
        PacketUtil.put(o,"skillPointCount",skillPointCount);
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        if(cs.player!=null){
            cs.player.skillPoints=skillPoints;
            cs.player.skillPointCanUse=skillPointCount;
        }
    }

    @Override
    public String getType() {
        return PacketUtil.getShortPacketName("player_data");
    }
}
