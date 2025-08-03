package modules.network.packet.c2s;

import engine.math.util.PacketUtil;
import modules.entity.Entity;
import modules.entity.player.PlayerEntity;
import modules.network.ServerNetworkHandler;
import modules.network.packet.Packet;
import modules.network.packet.s2c.PlayerWeaponUpdateS2CPacket;
import org.json.JSONObject;

import static engine.modules.EngineMain.cs;

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
