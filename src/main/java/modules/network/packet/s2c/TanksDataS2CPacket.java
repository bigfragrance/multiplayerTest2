package modules.network.packet.s2c;

import engine.math.util.PacketUtil;
import engine.render.Screen;
import modules.entity.Entity;
import modules.network.ClientNetworkHandler;
import modules.network.packet.Packet;
import modules.screen.TankChooseScreen;
import modules.screen.WordHelperScreen;
import modules.weapon.GunList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static engine.modules.EngineMain.cs;

public class TanksDataS2CPacket implements Packet<ClientNetworkHandler> {
    public JSONObject tanksData;
    public TanksDataS2CPacket(JSONObject tanksData){
        this.tanksData=tanksData;
    }
    @Override
    public JSONObject toJSON() {
        JSONObject tankData=new JSONObject(this.tanksData.toString());
        List<String> keys=new ArrayList<>();
        for(String s:tankData.keySet()){
            if(s.contains("visitor")){
                keys.add(s);
            }
        }
        for(String s:keys){
            tankData.remove(s);
        }
        JSONObject weaponData=new JSONObject(tankData.toString());
        PacketUtil.putPacketType(weaponData,getType());
        return weaponData;
    }

    @Override
    public void apply(ClientNetworkHandler clientNetworkHandler) {
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
