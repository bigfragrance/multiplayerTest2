package big.game.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class PlayerDataS2CPacket implements Packet<ClientNetworkHandler> {
    public double[] skillPoints;
    public int skillPointCount;
    public String skillPointNext;
    public PlayerDataS2CPacket(double[] skillPoints, int skillPointCount,String skillPointNext) {
        this.skillPoints = skillPoints;
        this.skillPointCount = skillPointCount;
        this.skillPointNext=skillPointNext;
    }
    public PlayerDataS2CPacket(JSONObject o) {
        this(Util.getDoubles(PacketUtil.getJSONArray(o, "skillPoints")),PacketUtil.getInt(o,"skillPointCount"),PacketUtil.getString(o,"skillPointNext"));
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        PacketUtil.putPacketType(o,getType());
        PacketUtil.put(o,"skillPoints",skillPoints);
        PacketUtil.put(o,"skillPointCount",skillPointCount);
        PacketUtil.put(o,"skillPointNext",skillPointNext);
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        if(cs.player!=null){
            cs.player.skillPoints=skillPoints;
            cs.player.skillPointCanUse=skillPointCount;
            cs.player.skillPointNext=skillPointNext;
        }
    }

    @Override
    public String getType() {
        return PacketUtil.getShortPacketName("player_data");
    }
}
