package big.modules.network.packet.c2s;

import big.engine.math.util.PacketUtil;
import big.modules.entity.Entity;
import big.modules.entity.player.PlayerEntity;
import big.modules.network.ServerNetworkHandler;
import big.modules.network.packet.Packet;
import big.modules.network.packet.s2c.PlayerWeaponUpdateS2CPacket;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

public class WantWeaponC2SPacket implements Packet<ServerNetworkHandler> {
    public long id;
    public WantWeaponC2SPacket(long id){
        this.id=id;
    }
    public WantWeaponC2SPacket(JSONObject o){
        this.id=PacketUtil.getLong(o,"id");
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
            serverNetworkHandler.send(new PlayerWeaponUpdateS2CPacket(e.id,e.weapon.toJSON()).toJSON());
            if(e instanceof PlayerEntity player) {
                serverNetworkHandler.sendPlayerData(player);
            }
        }
    }

    @Override
    public String getType() {
        return "want_weapon";
    }
}
