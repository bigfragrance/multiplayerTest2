package big.modules.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.engine.render.Screen;
import big.modules.entity.Entity;
import big.modules.entity.player.PlayerEntity;
import big.modules.network.ClientNetworkHandler;
import big.modules.network.packet.Packet;
import big.modules.screen.TankChooseScreen;
import big.modules.weapon.GunList;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;

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
        JSONObject weaponData=this.weaponData==null?new JSONObject():new JSONObject(this.weaponData.toString());
        if(this.weaponData==null){
            PacketUtil.put(weaponData,"weapon","null");
        }
        PacketUtil.putPacketType(weaponData,getType());
        PacketUtil.put(weaponData,"playerID",playerID);
        return weaponData;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        Entity entity=cs.entities.get(playerID);
        if(entity !=null){
            System.out.println("PlayerWeaponUpdateS2CPacket");
            if(PacketUtil.contains(weaponData,"weapon")&&PacketUtil.getString(weaponData,"weapon").equals("null")){
                entity.weapon=null;
                if(entity==cs.player){
                    cs.player.currentWeapon="none";
                    TankChooseScreen.INSTANCE.init();
                    TankChooseScreen.INSTANCE.open();
                }
            }
            entity.weapon=GunList.fromJSONClient(weaponData);
            if(entity==cs.player){
                TankChooseScreen.INSTANCE.init();
                TankChooseScreen.INSTANCE.open();
            }
        }
    }

    @Override
    public String getType() {
        return "weapon_update";
    }
}
