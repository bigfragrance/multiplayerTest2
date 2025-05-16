package modules.network.packet.c2s;

import engine.math.Vec2d;
import engine.math.util.PacketUtil;
import modules.entity.player.ServerPlayerEntity;
import modules.network.ServerNetworkHandler;
import modules.network.packet.Packet;
import modules.weapon.Weapon;
import org.json.JSONObject;

public class UpdateWeaponC2SPacket implements Packet<ServerNetworkHandler> {
    public int id;
    public UpdateWeaponC2SPacket(int id) {
        this.id = id;
    }
    public UpdateWeaponC2SPacket(JSONObject o) {
        this.id = PacketUtil.getInt(o,"id");
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
        if(player!=null){
            player.weapon= Weapon.get(player,id);
            //System.out.println("input:"+forward+","+side+","+aimPos+","+shoot+","+upgradingSkill);
        }
    }

    @Override
    public String getType() {
        return "update_weapon";
    }
}
