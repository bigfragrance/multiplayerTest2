package big.game.network.packet.s2c;

import big.engine.math.Box;
import big.engine.util.PacketUtil;
import big.engine.render.Screen;
import big.game.network.ClientNetworkHandler;
import big.game.network.packet.Packet;
import big.game.screen.TankChooseScreen;
import big.game.weapon.GunList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static big.engine.modules.EngineMain.cs;

public class ServerDataS2CPacket implements Packet<ClientNetworkHandler> {
    public JSONObject tanksData;
    public JSONObject presetData;
    public Box borderBox;
    public ServerDataS2CPacket(JSONObject tanksData, JSONObject presetData,Box borderBox){
        this.tanksData=tanksData;
        this.presetData=presetData;
        this.borderBox=borderBox;
    }
    public ServerDataS2CPacket(JSONObject data){
        this.tanksData=PacketUtil.getJSONObject(data,"tank");
        this.presetData=PacketUtil.getJSONObject(data,"preset");
        this.borderBox=PacketUtil.getBox(data,"box");
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
        JSONObject data=new JSONObject();
        PacketUtil.put(data,"tank",tanksData);
        PacketUtil.put(data,"preset",presetData);
        PacketUtil.put(data,"box",borderBox.toJSON());
        PacketUtil.putPacketType(data,getType());
        return data;
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
        cs.borderBox=this.borderBox;
    }

    @Override
    public String getType() {
        return "tanks_data";
    }
}
