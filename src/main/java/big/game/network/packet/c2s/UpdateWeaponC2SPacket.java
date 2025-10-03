package big.game.network.packet.c2s;

import big.engine.math.util.PacketUtil;
import big.game.entity.player.ServerPlayerEntity;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import big.game.network.packet.s2c.PlayerWeaponUpdateS2CPacket;
import big.game.weapon.GunList;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

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
            cs.multiClientHandler.sendToAll(new PlayerWeaponUpdateS2CPacket(player.id,player.weapon.toJSON()).toJSON());
            //System.out.println("input:"+forward+","+side+","+aimPos+","+shoot+","+upgradingSkill);
        }
    }

    @Override
    public String getType() {
        return "update_weapon";
    }
}
