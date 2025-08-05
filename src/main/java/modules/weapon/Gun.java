package modules.weapon;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.ColorUtils;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Attackable;
import modules.entity.Entity;
import modules.entity.bullet.AimBullet;
import modules.entity.bullet.BulletEntity;
import modules.entity.bullet.BulletType;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;
import static engine.render.Screen.sc;
import static modules.entity.bullet.BulletEntity.baseValues;

public class Gun extends CanAttack {
    public static Color color=new Color(150,150,150,255);
    public static double shrinkMultiplier=0.15;
    public Entity owner;
    public double offsetRotation;
    public Vec2d offset;
    public BulletType bulletType;
    public double reloadMultiplier;
    public int layer;
    private double startDelay;
    private double[] size;
    private double reload=0;
    private boolean lastFire=false;
    private int fireTime=0;
    private int prevFireTime=0;
    private int nextFireTime=0;
    private double sizeMultiplier=1;
    private JSONObject extraData;
    public Gun(JSONObject data,long id){
        this.offsetRotation = PacketUtil.getDouble(data,"offsetRotation");
        this.offset=Vec2d.fromJSON(PacketUtil.getJSONObject(data,"offset"));
        this.bulletType=BulletType.fromJSON(PacketUtil.getJSONObject(data,"bulletType"));
        this.reloadMultiplier=PacketUtil.getDouble(data,"reloadMultiplier");
        this.layer=PacketUtil.getInt(data,"layer");
        this.startDelay=PacketUtil.getDouble(data,"startDelay");
        this.owner=cs.entities.get(PacketUtil.getLong(data,"owner"));
        try {
            this.size = Util.getDoubles(PacketUtil.getJSONArray(data, "size"));
        }catch (Exception e){
            e.printStackTrace();
        }
        this.extraData=new JSONObject();
        if(PacketUtil.contains(data,"extraData")){
            try {
                this.extraData = PacketUtil.getJSONObject(data, "extraData");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        this.id=id;
        this.boundingBox=new Box(owner.position,0,0);
        this.prevBoundingBox=boundingBox.copy();
        this.position=owner.position.copy();
        this.prevPosition=position.copy();
        this.rotation=owner.rotation;
        this.prevRotation=rotation;
    }
    public Gun(JSONObject data){
        this(data,PacketUtil.getLong(data,"id"));
    }
    public Gun(Entity owner, double offsetRotation,Vec2d offset, BulletType bulletType, double reloadMultiplier,int layer,double startDelay,double[] size,long id,JSONObject extraData){
        this.offsetRotation =offsetRotation;
        this.offset=offset;
        this.bulletType=bulletType;
        this.reloadMultiplier=reloadMultiplier;
        this.layer=layer;
        this.startDelay=startDelay;
        this.size=size;
        this.id=id;
        this.extraData=extraData;

        this.boundingBox=new Box(owner.position,0,0);
        this.prevBoundingBox=boundingBox.copy();
        this.position=owner.position.copy();
        this.prevPosition=position;
        this.rotation=owner.rotation;
        this.prevRotation=rotation;
        this.owner=owner;
    }
    public Gun another(double offsetYaw){
        return new Gun(owner,this.offsetRotation +offsetYaw,offset,bulletType,reloadMultiplier,layer,startDelay,size,id+1,extraData);
    }
    public Gun another(double offsetYaw,Vec2d offset){
        return new Gun(owner,this.offsetRotation +offsetYaw,offset,bulletType,reloadMultiplier,layer,startDelay,size,id+1,extraData);
    }
    public double getReload(){
        return reload;
    }
    public void tick(boolean fire){
        if(cs.isServer) {
            this.sizeMultiplier=owner.getSizeMultiplier();
            BulletType bulletType=getBulletType();
            //bulletType.multipliers[3]=sizeMultiplier;
            double reloadMultiplier=owner.addReloadMultiplier(this.reloadMultiplier);
            updateRotation();
            this.reload = Math.max(0, reload - 1);
            if (!lastFire && fire) {
                this.reload = Math.max(reload,startDelay);
            }
            if (fire && reload <= 0&&canFire()) {
                reload = 10d/reloadMultiplier;
                fireTime = 4;
                BulletEntity b=create(bulletType);
                cs.addEntity(b);
                owner.velocity.offset(b.velocity.subtract(owner.getRealVelocity()).multiply(-BulletEntity.getMultipliedValue(7,bulletType)/bulletType.getMultiplier(1)));
            }
            lastFire = fire;
            if (fireTime > 0) {
                fireTime--;
            }
            prevFireTime=fireTime;
            prevRotation=rotation;
        }else{
            prevFireTime=fireTime;
            prevRotation=rotation;
            fireTime=nextFireTime;
            rotation=nextRotation;
        }
    }
    public BulletType getBulletType(){
        return owner.addMultipliers(this.bulletType);
    }
    public double getBulletSpeed(BulletType type){
        return baseValues[1]*type.getMultiplier(1);
    }
    public void setSize(double m){
        sizeMultiplier=m;
    }
    private boolean canFire(){
        if(PacketUtil.contains(extraData,"maxCount")){
            int maxCount=PacketUtil.getInt(extraData,"maxCount");
            int count=0;
            for(Entity e:cs.entities.values()){
                if(e instanceof BulletEntity b&&b.ownerId==owner.getOwnerID()&&b.type.isSame(bulletType)){
                    count++;
                }
            }
            if(count>=maxCount)return false;
        }
        return true;
    }
    public BulletEntity create(BulletType bulletType){
        Vec2d pos = getBulletPosition();
        double speed=getBulletSpeed(bulletType);
        Vec2d vel = new Vec2d(this.rotation).limit(speed).add(owner.getRealVelocity()).add(Util.randomVec().multiply(baseValues[6]*bulletType.getMultiplier(6)/bulletType.getMultiplier(1)));
        BulletEntity b=null;
        switch (bulletType.type){
            case 0->{
                b=new BulletEntity(pos, vel, owner.team, bulletType);
            }
            case 1->{
                b=new AimBullet(pos, vel, owner.team, bulletType);
                ((AimBullet)b).speedAdd=bulletType.getMultiplier(1)*baseValues[1]*0.2;
                if(owner instanceof Attackable a){
                    ((AimBullet)b).owner=a;
                }
            }
            case 2->{
                b=new AimBullet(pos, vel, owner.team, bulletType);
                ((AimBullet)b).speedAdd=bulletType.getMultiplier(1)*baseValues[1]*0.5;
                ((AimBullet) b).dragFactor=0.5;
                if(owner instanceof Attackable a){
                    ((AimBullet) b).aimPos=a.getAimPos();
                }
            }
            case 3->{
                b=new AimBullet(pos, vel, owner.team, bulletType);
                ((AimBullet) b).dragFactor=0.86;
            }
        }
        if(b==null) return null;
        b.ownerId=owner.getOwnerID();
        return b;
    }
    public JSONObject getUpdate(){
        JSONObject obj=new JSONObject();
        PacketUtil.put(obj,"id",id);
        PacketUtil.put(obj,"rotation",rotation);
        PacketUtil.put(obj,"fireTime",fireTime);
        PacketUtil.put(obj,"size",sizeMultiplier);
        return obj;
    }
    public void update(JSONObject o){
        this.nextRotation=PacketUtil.getDouble(o,"rotation");
        this.nextFireTime=PacketUtil.getInt(o,"fireTime");
        this.sizeMultiplier=PacketUtil.getDouble(o,"size");
    }
    public void render(Graphics g){
        Vec2d buttonPos=getRenderPosition();
        double rotation=owner.getRenderRotation()+offsetRotation;
        Vec2d gunLine=new Vec2d(size[0]*(1-getRenderFireTime()*shrinkMultiplier)*sizeMultiplier,0).rotate(rotation);
        Vec2d buttonLine=new Vec2d(0,size[1]*sizeMultiplier).rotate(rotation);
        Vec2d headLine=new Vec2d(0,size[2]*sizeMultiplier).rotate(rotation);
        g.setColor(color);
        Vec2d first=buttonPos.add(buttonLine);
        Vec2d second=buttonPos.subtract(buttonLine);
        Vec2d headMiddle=buttonPos.add(gunLine);
        Vec2d third=headMiddle.subtract(headLine);
        Vec2d fourth=headMiddle.add(headLine);
        Util.render(g,true,first,second,third,fourth);
        g.setColor(ColorUtils.darker(color,0.6));
        Util.render(g,false,first,second,third,fourth);
    }
    public double getRenderFireTime(){
        return Util.lerp(prevFireTime,fireTime, Screen.tickDelta);
    }
    public void updateRotation(){
        this.rotation=owner.rotation+ offsetRotation;
    }
    public Vec2d getBulletPosition(){
        return owner.position.add(offset.multiply(sizeMultiplier).rotate(owner.rotation+ offsetRotation));
    }
    public Vec2d getRenderPosition(){
        return owner.getRenderPosition().add(offset.multiply(sizeMultiplier).rotate(owner.getRenderRotation()+ offsetRotation));
    }
    public double getLayer(){
        return layer;
    }
    public JSONObject toJSON(){
        JSONObject o=new JSONObject();
        PacketUtil.put(o,"offsetRotation",offsetRotation);
        PacketUtil.put(o,"offset",offset.toJSON());
        PacketUtil.put(o,"bulletType",bulletType.toJSON());
        PacketUtil.put(o,"reloadMultiplier",reloadMultiplier);
        PacketUtil.put(o,"layer",layer);
        PacketUtil.put(o,"owner",owner.id);
        PacketUtil.put(o,"startDelay",startDelay);
        PacketUtil.put(o,"size",size);
        PacketUtil.put(o,"id",id);
        PacketUtil.put(o,"extraData",extraData);
        return o;
    }
    public static Gun fromJSON(JSONObject obj){
        return new Gun(obj);
    }
}
