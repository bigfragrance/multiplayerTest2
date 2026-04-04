package big.game.network.packet.s2c;

import big.engine.math.Box;
import big.engine.util.PacketUtil;
import big.game.entity.Entity;
import big.game.entity.player.DodgeGhost;
import big.game.entity.player.PlayerEntity;
import big.game.network.ClientNetworkHandler;
import big.game.network.PacketType;
import big.game.network.packet.Packet;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class DodgeGhostS2CPacket implements Packet<ClientNetworkHandler> {
    public Box boundingBox;
    public long playerID;
    public DodgeGhostS2CPacket(Box boundingBox, long playerID) {
        this.boundingBox = boundingBox;
        this.playerID = playerID;
    }
    public DodgeGhostS2CPacket(JSONObject obj) {
        this.boundingBox= PacketUtil.getBox(obj,"box");
        this.playerID= PacketUtil.getLong(obj,"id");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,getType());
        PacketUtil.put(o,"box",boundingBox);
        PacketUtil.put(o,"id",playerID);
        return o;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        try {
            Entity entity = cs.world.getEntity(playerID);
            if (entity instanceof PlayerEntity player) {
                player.dodgeGhost = new DodgeGhost(boundingBox,player.rotation);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public PacketType getType() {
        return PacketType.DODGE_GHOST_S2C;
    }
}
