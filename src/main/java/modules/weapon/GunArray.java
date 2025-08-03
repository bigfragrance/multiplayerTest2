package modules.weapon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;

public class GunArray extends CanAttack{
    //only exists in server side
    public Gun[] guns;
    public GunArray(){

    }
    public void render(Graphics g){
        for(Gun gun:guns){
            gun.render(g);
        }
    }
    public void tick(boolean fire){
        for(Gun gun:guns){
            gun.tick(fire);
        }
    }
    public int getLayer() {
        return guns[0].layer;
    }
}
