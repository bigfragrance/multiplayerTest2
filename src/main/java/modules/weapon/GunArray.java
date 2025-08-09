package modules.weapon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;

public class GunArray extends CanAttack{
    //only exists in server side
    public CanAttack[] guns;
    public GunArray(){

    }
   /* public void render(Graphics g){
        for(CanAttack gun:guns){
            gun.render(g);
        }
    }
    public void tick(boolean fire){
        for(Gun gun:guns){
            gun.tick(fire);
        }
    }*/
    public double getLayer() {
        return guns[0].getLayer();
    }
}
