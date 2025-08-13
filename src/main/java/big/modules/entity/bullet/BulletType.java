package big.modules.entity.bullet;

import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import org.json.JSONObject;

import java.util.Arrays;

public class BulletType {
    public static double[] baseMultipliers={1,1,1,1,1,1,1,1};
    public int type;
    public boolean sharp;
    public double sharpFactor;
    public double[] multipliers;
    public JSONObject weapon;
    public BulletType(int type,boolean sharp,double sharpFactor,double[] multipliers,JSONObject weapon){
        this.type=type;
        this.sharp=sharp;
        this.sharpFactor=sharpFactor;
        this.multipliers=multipliers;
        this.weapon=weapon;
    }
    public BulletType(JSONObject object){
        this.type=PacketUtil.getInt(object,"type");
        this.sharp=PacketUtil.getBoolean(object,"sharp");
        this.sharpFactor=PacketUtil.getDouble(object,"sharpFactor");
        try {
            this.multipliers = Util.getDoubles(PacketUtil.getJSONArray(object, "multipliers"));
        }catch (Exception e){
            e.printStackTrace();
        }
        this.weapon=PacketUtil.contains(object,"weapon")?PacketUtil.getJSONObject(object,"weapon"):null;
    }
    public BulletType(int type,double[] multipliers){
        this(type,false,1,multipliers,null);
    }
    public BulletType(double[] multipliers){
        this(0,false,1,multipliers,null);
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
        return new BulletType(type,sharp,sharpFactor,multipliers.clone(),weapon);
    }
    public static BulletType fromJSON(JSONObject o){
        return new BulletType(o);
    }
    public JSONObject toJSON(){
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"type",type);
        PacketUtil.put(o,"sharp",sharp);
        PacketUtil.put(o,"sharpFactor",sharpFactor);
        PacketUtil.put(o,"multipliers",multipliers);
        if(weapon!=null) PacketUtil.put(o,"weapon",weapon);
        return o;
    }
    public JSONObject toJSON2(JSONObject weapon){
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"type",type);
        PacketUtil.put(o,"sharp",sharp);
        PacketUtil.put(o,"sharpFactor",sharpFactor);
        PacketUtil.put(o,"multipliers",multipliers);
        if(weapon!=null) PacketUtil.put(o,"weapon",weapon);
        return o;
    }
    public static BulletType KILLER=new BulletType(0,false,1,Util.multiply(baseMultipliers,new double[]{2,1,100,1,0.2,1,1,1}),null);
}
