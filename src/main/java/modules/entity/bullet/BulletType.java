package modules.entity.bullet;

import engine.math.util.PacketUtil;
import org.json.JSONObject;

public class BulletType {
    public int type;
    public boolean sharp;
    public double sharpFactor;
    public BulletType(int type,boolean sharp,double sharpFactor){
        this.type=type;
        this.sharp=sharp;
        this.sharpFactor=sharpFactor;
    }
    public BulletType(int type){
        this(type,false,1);
    }
    public BulletType(){
        this(0,false,1);
    }
    public BulletType copy(){
        return new BulletType(type,sharp,sharpFactor);
    }
    public static BulletType fromJSON(JSONObject o){
        return new BulletType(PacketUtil.getInt(o,"type"),PacketUtil.getBoolean(o,"sharp"),PacketUtil.getDouble(o,"sharpFactor"));
    }
    public JSONObject toJSON(){
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"type",type);
        PacketUtil.put(o,"sharp",sharp);
        PacketUtil.put(o,"sharpFactor",sharpFactor);
        return o;
    }
}
