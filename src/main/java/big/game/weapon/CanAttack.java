package big.game.weapon;

import big.engine.math.Vec2d;
import big.engine.util.NNPRecorder;
import big.engine.util.PacketUtil;
import big.engine.util.Util;
import big.game.entity.Entity;
import org.json.JSONObject;

import java.awt.*;

public abstract class CanAttack extends Entity {
    public boolean customOffsetRotation=false;
    public double customOffsetRotationAngle=0;
    public NNPRecorder<Double> offsetRotationAll=new NNPRecorder<>(0d);
    public Node lastNode=null;
    public Entity owner;
    public CanAttack(){

    }
    public void tick(boolean fire,boolean defend,boolean server){

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
            case "GunLine":
                return new LineGun(new Gun(json,0),PacketUtil.getInt(json,"count"),PacketUtil.getVec2d(json,"oOffset"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "Mirror":
                return new MirrorGun(new Gun(json,0),PacketUtil.getInt(json,"mode"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "AutoGunList":
                return AutoGunList.fromJSONServer(json);
            case "AutoGunArray":
                return new SurroundGun(AutoGunList.fromJSONServer(json),PacketUtil.getInt(json,"count"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "AutoMirror":
                return new MirrorGun(AutoGunList.fromJSONServer(json),PacketUtil.getInt(json,"mode"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "AutoLine":
                return new LineGun(AutoGunList.fromJSONServer(json),PacketUtil.getInt(json,"count"),PacketUtil.getVec2d(json,"oOffset"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "GunArrayList":
                return new SurroundGunList(GunArray.getGuns(json),PacketUtil.getInt(json,"count"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "GunLineList":
                return new LineGunList(GunArray.getGuns(json),PacketUtil.getInt(json,"count"),PacketUtil.getVec2d(json,"oOffset"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);
            case "MirrorList":
                return new MirrorGunList(GunArray.getGuns(json),PacketUtil.getInt(json,"mode"),PacketUtil.contains(json,"startDelays")? Util.getDoubles(PacketUtil.getJSONArray(json, "startDelays")):null);

            default:
                return null;
        }
    }
    public static CanAttack fromJSONClient(JSONObject json){
        switch (PacketUtil.getString(json,"type")){
            case "Gun":
                return new Gun(json);
            case "AutoGunList":
                return AutoGunList.fromJSONClient(json);
            default:
                return null;
        }
    }

    protected double getStartDelay() {
        return 0;
    }

    public void setLayer(double v) {

    }


}
