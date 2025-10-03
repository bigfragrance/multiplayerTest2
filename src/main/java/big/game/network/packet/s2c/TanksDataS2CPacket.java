package big.game.network.packet.s2c;

import big.engine.math.util.PacketUtil;
import big.engine.render.Screen;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import big.game.screen.TankChooseScreen;
import big.game.weapon.GunList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TanksDataS2CPacket implements Packet<ClientNetworkHandler> {
    public JSONObject tanksData;
    public JSONObject presetData;
    public TanksDataS2CPacket(JSONObject tanksData,JSONObject presetData){
        this.tanksData=tanksData;
        this.presetData=presetData;
    }
    public TanksDataS2CPacket(JSONObject tanksData){
        this.tanksData=tanksData;
        this.presetData=tanksData.getJSONObject("preset");
    }
    @Override
    public JSONObject toJSON() {
        JSONObject tankData=new JSONObject(this.tanksData.toString());
        List<String> keys=new ArrayList<>();
        for(String s:tankData.keySet()){
            if(s.contains("visitor")){
                keys.add(s);
            }
            if(s.contains("test")){
                keys.add(s);
            }
        }
        for(String s:keys){
            tankData.remove(s);
        }
        JSONObject weaponData=new JSONObject(tankData.toString());
        weaponData.put("preset",presetData);
        PacketUtil.putPacketType(weaponData,getType());
        return weaponData;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
        GunList.data=tanksData;
        GunList.presetData=presetData;
        TankChooseScreen.tanksList=tanksData;
        TankChooseScreen.INSTANCE.init();
        //WordHelperScreen.INSTANCE.init();
        Screen.sc.setScreen(TankChooseScreen.INSTANCE);
        System.out.println("TanksDataS2CPacket applied");
    }

    @Override
    public String getType() {
        return "tanks_data";
    }
}
