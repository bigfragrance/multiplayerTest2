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
            System.out.println("aaa");
        }
    }

    @Override
    public String getType() {
        return PacketUtil.getShortPacketName("player_data");
    }
}
