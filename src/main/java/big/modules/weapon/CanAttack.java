package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.NNPRecorder;
import big.engine.math.util.PacketUtil;
import big.modules.entity.Entity;
import org.json.JSONObject;

import java.awt.*;

public class CanAttack extends Entity {
    public boolean customOffsetRotation=false;
    public float customOffsetRotationAngle=0;
    public NNPRecorder<float> offsetRotationAll=new NNPRecorder<>(0d);
    public Node lastNode=null;
    public CanAttack(){

    }
    public void tick(boolean fire,boolean server){

    }
    public void render(Graphics g){

    }
    public void setSize(float m){

    }
    public float getLayer() {
        return 0;
    }
    public float getOffsetRotation(){
        return 0;
    }
    public Vec2d getOffset(){
        return new Vec2d(0,0);
    }
    public CanAttack another(float angle){
        return null;
    }
    public CanAttack another(float angle, Vec2d offset){
        return null;
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
            case "AutoGunList":
                return AutoGunList.fromJSONServer(json);
            case "AutoGunArray":
                return new SurroundGun(AutoGunList.fromJSONServer(json),PacketUtil.getInt(json,"count"));
            case "AutoMirror":
                return new MirrorGun(AutoGunList.fromJSONServer(json),PacketUtil.getInt(json,"mode"));
            default:
                return null;
        }
    }
    public static CanAttack fromJSONClient(JSONObject json){
        switch (PacketUtil.getString(json,"type")){
            case "Gun":
                return new Gun(json);
            /*case "GunArray":
                return new SurroundGun(new Gun(json),PacketUtil.getInt(json,"count"));
            case "Mirror":
                return new MirrorGun(new Gun(json),PacketUtil.getInt(json,"mode"));*/
            case "AutoGunList":
                return AutoGunList.fromJSONClient(json);
            /*case "AutoGunArray":
                return new SurroundGun(AutoGunList.fromJSONClient(json),PacketUtil.getInt(json,"count"));
            case "AutoMirror":
                return new MirrorGun(AutoGunList.fromJSONClient(json),PacketUtil.getInt(json,"mode"));*/
            default:
                return null;
        }
    }
}
