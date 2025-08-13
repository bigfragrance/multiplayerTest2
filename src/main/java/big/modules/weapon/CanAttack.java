package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.NNPRecorder;
import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import big.modules.entity.Entity;
import org.json.JSONObject;

import java.awt.*;

public class CanAttack extends Entity {
    public boolean customOffsetRotation=false;
    public double customOffsetRotationAngle=0;
    public NNPRecorder<Double> offsetRotationAll=new NNPRecorder<>(0d);
    public Node lastNode=null;
    public Entity owner;
    public CanAttack(){

    }
    public void tick(boolean fire,boolean server){

    }
    public void render(Graphics g){

    }
    public void setSize(double m){

    }
    public double getLayer() {
        return 0;
    }
    public double getOffsetRotation(){
        return 0;
    }
    public Vec2d getOffset(){
        return new Vec2d(0,0);
    }
    public CanAttack another(double angle){
        return null;
    }
    public CanAttack another(double angle, Vec2d offset){
        return null;
    }
    public CanAttack another(double offsetYaw,Vec2d offset,double startDelay){
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
                return new SurroundGun(new Gun(json,0),PacketUtil.getInt(json,"count"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "Mirror":
                return new MirrorGun(new Gun(json,0),PacketUtil.getInt(json,"mode"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "AutoGunList":
                return AutoGunList.fromJSONServer(json);
            case "AutoGunArray":
                return new SurroundGun(AutoGunList.fromJSONServer(json),PacketUtil.getInt(json,"count"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "AutoMirror":
                return new MirrorGun(AutoGunList.fromJSONServer(json),PacketUtil.getInt(json,"mode"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
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

    protected double getStartDelay() {
        return 0;
    }
}
