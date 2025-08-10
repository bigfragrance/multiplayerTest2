package big.modules.network.packet.c2s;

import big.engine.math.Vec2d;
import big.engine.math.util.PacketUtil;
import big.modules.entity.player.ServerPlayerEntity;
import big.modules.network.ServerNetworkHandler;
import big.modules.network.packet.Packet;
import big.modules.network.packet.s2c.PlayerWeaponUpdateS2CPacket;
import big.modules.weapon.GunList;
import big.modules.weapon.Weapon;
import org.json.JSONObject;

public class UpdateWeaponC2SPacket implements Packet<ServerNetworkHandler> {
    public String id;
    public UpdateWeaponC2SPacket(String id) {
        this.id = id;
    }
    public UpdateWeaponC2SPacket(JSONObject o) {
        this.id = PacketUtil.getString(o,"id");
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
        ServerPlayerEntity player=serverNetworkHandler.clientHandler.player;
        if(player!=null&&!player.weaponID.equals(id)){
            player.weapon= GunList.fromID(player,id);
            serverNetworkHandler.send(new PlayerWeaponUpdateS2CPacket(player.id,player.weapon.toJSON()).toJSON());
            //System.out.println("input:"+forward+","+side+","+aimPos+","+shoot+","+upgradingSkill);
        }
    }

    @Override
    public String getType() {
        return "update_weapon";
    }
}
