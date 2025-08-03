package modules.network.packet.s2c;

import engine.math.util.PacketUtil;
import modules.entity.Entity;
import modules.entity.player.PlayerEntity;
import modules.network.ClientNetworkHandler;
import modules.network.packet.Packet;
import modules.weapon.GunList;
import org.json.JSONObject;

import static engine.modules.EngineMain.cs;

public class PlayerWeaponUpdateS2CPacket implements Packet<ClientNetworkHandler> {
    public long playerID;
    public JSONObject weaponData;
    public PlayerWeaponUpdateS2CPacket(long playerID, JSONObject weaponData){
        this.playerID=playerID;
        this.weaponData=weaponData;
    }
    public PlayerWeaponUpdateS2CPacket(JSONObject o){
        this.playerID=PacketUtil.getLong(o,"playerID");
        this.weaponData= o;
    }
    @Override
    public JSONObject toJSON() {
        JSONObject weaponData=new JSONObject(this.weaponData.toString());
        PacketUtil.putPacketType(weaponData,getType());
        PacketUtil.put(weaponData,"playerID",playerID);
        return weaponData;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        Entity entity=cs.entities.get(playerID);
        if(entity !=null){
            System.out.println("PlayerWeaponUpdateS2CPacket");
            entity.weapon= GunList.fromJSONClient(weaponData);
        }
    }

    @Override
    public String getType() {
        return "weapon_update";
    }
}
