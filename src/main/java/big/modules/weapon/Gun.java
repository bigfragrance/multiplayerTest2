package big.modules.weapon;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.ColorUtils;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.modules.entity.Attackable;
import big.modules.entity.Entity;
import big.modules.entity.bullet.AimBullet;
import big.modules.entity.bullet.BulletEntity;
import big.modules.entity.bullet.BulletType;
import org.json.JSONObject;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;
import static big.modules.entity.bullet.BulletEntity.baseValues;

public class Gun extends CanAttack {
    public static Color color=new Color(150,150,150,255);
    public static double shrinkMultiplier=0.15;

    public double offsetRotation;

    public Vec2d offset;
    public BulletType bulletType;
    public double reloadMultiplier;
    public double layer;
    private double startDelay;
    private double[] size;
    private double reload=1;
    private boolean lastFire=false;
    private int fireTime=0;
    private int prevFireTime=0;
    private int nextFireTime=0;
    private double sizeMultiplier=1;
    private JSONObject extraData;
    public Vec2d startPos;
    public Gun(JSONObject data,long id){
        this.offsetRotation = PacketUtil.getDouble(data,"offsetRotation");
        this.offset=Vec2d.fromJSON(PacketUtil.getJSONObject(data,"offset"));
        this.bulletType=BulletType.fromJSON(PacketUtil.getJSONObject(data,"bulletType"));
        this.reloadMultiplier=PacketUtil.getDouble(data,"reloadMultiplier");
        this.layer=PacketUtil.getDouble(data,"layer");
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
    public Gun(Entity owner, double offsetRotation,Vec2d offset, BulletType bulletType, double reloadMultiplier,double layer,double startDelay,double[] size,long id,JSONObject extraData){
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
        return new Gun(owner,this.offsetRotation +offsetYaw,offset,bulletType,reloadMultiplier,layer,startDelay,size,id,extraData);
    }
    public Gun another(double offsetYaw,Vec2d offset){
        return new Gun(owner,this.offsetRotation +offsetYaw,offset,bulletType,reloadMultiplier,layer,startDelay,size,id,extraData);
    }
    public Gun another(double offsetYaw,Vec2d offset,double startDelay){
        return new Gun(owner,this.offsetRotation +offsetYaw,offset,bulletType,reloadMultiplier,layer,startDelay,size,id,extraData);
    }
    public double getOffsetRotation(){
        return offsetRotation;
    }
    public Vec2d getOffset(){
        return offset;
    }
    public double getReload(){
        return reload;
    }
    public void tick(boolean fire,boolean defend,boolean server){
        if(server) {
            boolean firing=(PacketUtil.contains(extraData,"defend"))?defend:fire;
            this.sizeMultiplier=owner.getSizeMultiplier();
            BulletType bulletType=getBulletType();
            //bulletType.multipliers[3]=sizeMultiplier;
            double reloadMultiplier=owner.addReloadMultiplier(this.reloadMultiplier);
            updateOffsetRotation();
            updateRotation();
            this.reload = Math.max(0, reload - 1);
            if (!lastFire && firing) {
                this.reload = Math.max(reload,startDelay);
            }
            if (firing && reload <= 0&&canFire()) {
                reload = 10d/reloadMultiplier;
                fireTime = 4;
                BulletEntity b=create(bulletType);
                cs.addEntity(b);
                owner.velocity.offset(b.velocity.subtract(owner.getRealVelocity()).multiply(-BulletEntity.getMultipliedValue(7,bulletType)/bulletType.getMultiplier(1)));
            }
            lastFire = firing;
            if (fireTime > 0) {
                fireTime--;
            }
            prevFireTime=fireTime;
            prevRotation=rotation;
            offsetRotationAll.nextPrev();
        }else{
            prevFireTime=fireTime;
            prevRotation=rotation;
            fireTime=nextFireTime;
            rotation=nextRotation;
            offsetRotationAll.next();
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
        PacketUtil.put(obj,"offsetRotationAll",offsetRotationAll.get());
        return obj;
    }
    public void update(JSONObject o){
        this.nextRotation=PacketUtil.getDouble(o,"rotation");
        this.nextFireTime=PacketUtil.getInt(o,"fireTime");
        this.sizeMultiplier=PacketUtil.getDouble(o,"size");
        this.offsetRotationAll.setNow(PacketUtil.getDouble(o,"offsetRotationAll"));
    }
    public void render(Graphics g){
        Vec2d buttonPos=getRenderPosition();
        for(int i=0;i<size.length;i+=3){
            buttonPos=render(g,buttonPos,size[i],size[i+1],size[i+2]);
        }
        /*Vec2d buttonPos=getRenderPosition();
        double rotation=Util.lerp(prevRotation,this.rotation, Screen.tickDelta);
        double shrink=(1-getRenderFireTime()*shrinkMultiplier)*sizeMultiplier;
        Vec2d gunLine=new Vec2d(size[0]*shrink,0).rotate(rotation);
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
        Util.render(g,false,first,second,third,fourth);*/
    }
    private Vec2d render(Graphics g,Vec2d buttonPos,double len,double w1,double w2){
        double rotation=Util.lerpRotation(prevRotation,this.rotation, Screen.tickDelta);
        double shrink=(1-getRenderFireTime()*shrinkMultiplier)*sizeMultiplier;
        Vec2d gunLine=new Vec2d(len*shrink,0).rotate(rotation);
        Vec2d buttonLine=new Vec2d(0,w1*sizeMultiplier).rotate(rotation);
        Vec2d headLine=new Vec2d(0,w2*sizeMultiplier).rotate(rotation);
        g.setColor(color);
        Vec2d first=buttonPos.add(buttonLine);
        Vec2d second=buttonPos.subtract(buttonLine);
        Vec2d headMiddle=buttonPos.add(gunLine);
        Vec2d third=headMiddle.subtract(headLine);
        Vec2d fourth=headMiddle.add(headLine);
        Util.render(g,true,first,second,third,fourth);
        g.setColor(ColorUtils.darker(color,0.6));
        Util.render(g,false,first,second,third,fourth);
        return headMiddle;
    }
    public double getRenderFireTime(){
        return Util.lerp(prevFireTime,fireTime, Screen.tickDelta);
    }
    public void updateRotation(){
        this.rotation=offsetRotationAll.get();
        /*if(customRotation){

            this.rotation=customRotationAngle+ offsetRotation;
        }else {
            this.rotation = owner.rotation + offsetRotation;
        }*/
    }
    public void updateOffsetRotation(){
        if(lastNode!=null){
            offsetRotationAll.setNow(offsetRotation+lastNode.getAimRotation());
        }
        else{
            offsetRotationAll.setNow(offsetRotation+owner.rotation);
        }
    }
    public Vec2d getBulletPosition(){
        return getStartPos().add(offset.multiply(sizeMultiplier).rotate(offsetRotationAll.get()));
    }
    public Vec2d getRenderPosition(){
        return getRenderStartPos().add(offset.multiply(sizeMultiplier).rotate(getRenderOffsetRotation()));
    }
    public double getRenderRotation(){
        return Util.lerpRotation(prevRotation,rotation, Screen.tickDelta);
    }
    public double getRenderOffsetRotation(){
        return Util.lerpRotation(offsetRotationAll.getPrev(),offsetRotationAll.get(), Screen.tickDelta);
    }
    public double getStartDelay(){
        return startDelay;
    }
    public Vec2d getStartPos(){
        return lastNode==null?owner.position:lastNode.getPos();
    }
    public Vec2d getRenderStartPos(){
        return lastNode==null?owner.getRenderPosition():lastNode.getRenderPos();
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
        PacketUtil.put(o,"type","Gun");
        return o;
    }
    public void setLayer(double v) {
        layer = v;
    }
    public static Gun fromJSON(JSONObject obj){
        return new Gun(obj);
    }
}
