package big.modules.weapon;


import big.engine.math.Vec2d;
import big.engine.math.util.PacketUtil;
import big.modules.entity.Entity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static big.engine.modules.EngineMain.cs;
import static big.modules.weapon.GunList.fromJSONServer;
import static big.modules.weapon.GunList.presetData;

public class GunArray extends CanAttack{
    //only exists in big.server side
    public CanAttack[] guns;
    public GunArray(){

    }
    public static List<CanAttack> getGuns(JSONObject obj){
        JSONArray array = PacketUtil.getJSONArray(obj,"data");
        Entity owner = cs.entities.get(PacketUtil.getLong(obj, "owner"));
        List<CanAttack> guns=new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject gunObj = array.getJSONObject(i);
            PacketUtil.put(gunObj, "owner", owner.id);
            PacketUtil.put(gunObj, "id", 0);
            if(gunObj.has("preset")){
                JSONArray preset=gunObj.getJSONArray("preset");
                for(int j=0;j<preset.length();j++){
                    String presetName=preset.getString(j);
                    JSONObject pre=presetData.getJSONObject(presetName);
                    GunList list1=fromJSONServer(owner, pre);
                    for(CanAttack ca:list1.list.values()){
                        guns.add(ca);
                    }
                }
                continue;
            }
            CanAttack o = CanAttack.fromJSON(gunObj);
            if (o == null) continue;
            guns.add(o);
        }
        return guns;
    }

    public double getLayer() {
        return guns[0].getLayer();
    }
}
