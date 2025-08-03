package modules.weapon;

import engine.math.util.PacketUtil;
import modules.entity.Entity;
import org.json.JSONObject;

import java.awt.*;

public class CanAttack extends Entity {
    public CanAttack(){

    }
    public void tick(boolean fire){

    }
    public void render(Graphics g){

    }
    public int getLayer() {
        return 0;
    }
    public JSONObject toJSON(){
        return null;
    }
    public static CanAttack fromJSON(JSONObject json){
        switch (PacketUtil.getString(json,"type")){
            case "Gun":
                return new Gun(json,0);
            case "GunArray":
                return new SurroundGun(new Gun(json,0),PacketUtil.getInt(json,"count"));
            case "Mirror":
                return new MirrorGun(new Gun(json,0),PacketUtil.getInt(json,"mode"));
            default:
                return null;
        }
    }
}
