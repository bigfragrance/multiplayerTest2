package big.modules.entity.bullet;

import big.engine.math.util.ColorUtils;
import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import org.json.JSONObject;

import java.awt.*;
import java.util.Arrays;

import static big.engine.math.util.EntityUtils.smallerBullet;

public class BulletType {
    public static double[] baseMultipliers={1,1,1,1,1,1,1,1};
    public int type;
    public int renderType;
    public boolean sharp;
    public double sharpFactor;
    public double[] multipliers;
    public JSONObject weapon;
    public JSONObject tags;
    public BulletType(int type,int renderType,boolean sharp,double sharpFactor,double[] multipliers,JSONObject weapon,JSONObject tags){
        this.type=type;
        this.renderType=renderType;
        this.sharp=sharp;
        this.sharpFactor=sharpFactor;
        this.multipliers=multipliers;
        this.weapon=weapon;
        this.tags=tags;
    }
    public BulletType(JSONObject object){
        this.type=PacketUtil.getInt(object,"type");
        this.renderType=PacketUtil.contains(object,"renderType")?PacketUtil.getInt(object,"renderType"):type;
        this.sharp=PacketUtil.getBoolean(object,"sharp");
        this.sharpFactor=PacketUtil.getDouble(object,"sharpFactor");
        try {
            this.multipliers = Util.getDoubles(PacketUtil.getJSONArray(object, "multipliers"));
        }catch (Exception e){
            e.printStackTrace();
        }
        this.weapon=PacketUtil.contains(object,"weapon")?PacketUtil.getJSONObject(object,"weapon"):null;
        this.tags=PacketUtil.contains(object,"tags")?PacketUtil.getJSONObject(object,"tags"):new JSONObject();
    }
    public boolean isSame(BulletType other){
        return this.type==other.type;
    }
    public boolean multipliersEquals(BulletType other){
        if(this.multipliers.length!=other.multipliers.length) return false;
        for(int i=0;i<this.multipliers.length;i++){
            if(this.multipliers[i]!=other.multipliers[i]) return false;
        }
        return true;
    }
    public double getMultiplier(int index){
        if(index>=multipliers.length)return baseMultipliers[index];
        return multipliers[index];
    }
    public BulletType copy(){
        return new BulletType(type,renderType,sharp,sharpFactor,multipliers.clone(),weapon,tags);
    }
    public static BulletType fromJSON(JSONObject o){
        return new BulletType(o);
    }
    public JSONObject toJSON(){
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"type",type);
        PacketUtil.put(o,"renderType",renderType);
        PacketUtil.put(o,"sharp",sharp);
        PacketUtil.put(o,"sharpFactor",sharpFactor);
        PacketUtil.put(o,"multipliers",multipliers);
        if(tags!=null) PacketUtil.put(o,"tags",tags);
        if(weapon!=null) PacketUtil.put(o,"weapon",weapon);
        return o;
    }
    public JSONObject toJSON2(JSONObject weapon){
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"type",type);
        PacketUtil.put(o,"renderType",renderType);
        PacketUtil.put(o,"sharp",sharp);
        PacketUtil.put(o,"sharpFactor",sharpFactor);
        PacketUtil.put(o,"multipliers",multipliers);
        if(tags!=null) PacketUtil.put(o,"tags",tags);
        if(weapon!=null) PacketUtil.put(o,"weapon",weapon);
        return o;
    }
    public void render(Graphics g,BulletEntity e,Color team){
        if(renderType==0) {
            g.setColor(ColorUtils.darker(team, 0.6));
            Util.render(g, Util.lerp(e.prevBoundingBox, e.boundingBox, e.getTickDelta()).switchToJFrame());
            g.setColor(team);
            Util.render(g,smallerBullet(Util.lerp(e.prevBoundingBox, e.boundingBox, e.getTickDelta())).switchToJFrame());
        }else{
            double[] sharpFactors=getSharpFactors();
            g.setColor(ColorUtils.darker(team, 0.6));
            if(sharpFactors==null) {
                Util.renderPolygon(g, e.getRenderPosition(), renderType + 2, e.boundingBox.avgSize() / 2, e.getRenderRotation(), true, true, sharp, sharpFactor);
                g.setColor(team);
                Util.renderPolygon(g, e.getRenderPosition(), renderType + 2, e.boundingBox.avgSize() / 2 * 0.9, e.getRenderRotation(), true, true, sharp, sharpFactor);
            }else{
                Util.renderPolygon(g, e.getRenderPosition(), renderType + 2, e.boundingBox.avgSize() / 2, e.getRenderRotation(), true, true, sharp, sharpFactors);
                g.setColor(team);
                Util.renderPolygon(g, e.getRenderPosition(), renderType + 2, e.boundingBox.avgSize() / 2 * 0.9, e.getRenderRotation(), true, true, sharp, sharpFactors);
            }
        }
    }
    private double[] getSharpFactors(){
        return PacketUtil.contains(tags,"sharpFactors")?Util.getDoubles(PacketUtil.getJSONArray(tags,"sharpFactors")):null;
    }
    public boolean shouldCustomRotation(){
        return PacketUtil.contains(tags,"rotation");
    }
    public double getCustomRotation(){
        return PacketUtil.getDouble(tags,"rotation");
    }
    public boolean shouldAutoAim(double random){
        return PacketUtil.contains(tags,"autoAim")&&PacketUtil.getDouble(tags,"autoAim")>=random;
    }

    public static BulletType KILLER=new BulletType(0,0,false,1,Util.multiply(baseMultipliers,new double[]{2,1,100,1,0.2,1,1,1}),null,new JSONObject());
}
