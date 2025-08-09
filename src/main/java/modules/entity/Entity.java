package modules.entity;

import engine.math.BlockPos;
import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import engine.math.util.PacketVariable;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.boss.VisitorEntity;
import modules.entity.bullet.BulletType;
import modules.entity.player.PlayerEntity;
import modules.weapon.GunList;
import modules.weapon.Weapon;
import modules.world.ChunkPos;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static engine.math.util.PacketVariable.basic;
import static engine.math.util.Util.round;
import static engine.modules.EngineMain.chunkSize;
import static engine.modules.EngineMain.cs;

public abstract class Entity implements NetworkItem {
    public static double sizeMultiplier=0.02;
    public static double extraVelocityD=0.1;
    public static int particleLifeTimeMax=4;
    public static double particleBoundingBoxExpand=60;
    public Box boundingBox;
    public Box prevBoundingBox;
    public Vec2d position;
    public Vec2d prevPosition;
    public Vec2d velocity;
    public Vec2d extraVelocity=new Vec2d(0,0);
    public long id;
    public boolean isAlive=true;
    public Vec2d nextPosition=null;
    public Box nextBoundingBox=null;
    public double health;
    public double prevHealth;
    public double shield=0;
    public double prevShield=0;
    public double damage;
    public int team;
    public double rotation;
    public double prevRotation;
    public double nextRotation=0;
    public double score=0;
    private ConcurrentHashMap<Long,Double> scoreAdd=new ConcurrentHashMap<>();
    public static double collisionVector=0.1;
    public static double collisionMax=1;
    public Map<Long,DamageSource> damageTaken=new ConcurrentHashMap<>();
    public boolean isDamageTick=false;
    public double mass=400;
    protected double tickDelta=0;
    public Vec2d targetingPos=null;
    public boolean isParticle=false;
    public boolean checkBorderCollision=true;
    public int lifeTime=0;
    public GunList weapon=null;
    public boolean insideWall=false;
    @Override
    public void update(JSONObject o) {
        this.prevHealth=this.health;
        if(o.has(basic)){
            //this.prevPosition = this.position.copy();
            JSONObject basic = o.getJSONObject(PacketVariable.basic);
            basic.keys().forEachRemaining(key -> {
                if(PacketUtil.getShortVariableName("position").equals(key)){
                    JSONObject position = basic.getJSONObject(PacketUtil.getShortVariableName("position"));
                    Vec2d last=this.position.copy();
                    this.nextPosition =Vec2d.fromJSON(position);
                    this.velocity=this.nextPosition.subtract(last);
                    this.nextBoundingBox=new Box(nextPosition,this.boundingBox.xSize()/2,this.boundingBox.ySize()/2);
                } else if (PacketUtil.getShortVariableName("boundingBox").equals(key)) {
                    JSONObject boundingBox = basic.getJSONObject(PacketUtil.getShortVariableName("boundingBox"));
                    Box last=this.boundingBox.copy();
                    this.nextBoundingBox  = Box.fromJSON(boundingBox);
                    this.nextPosition =nextBoundingBox.getCenter();
                    this.velocity=this.nextBoundingBox.getCenter().subtract(last.getCenter());
                } else if (PacketUtil.getShortVariableName("health").equals(key)) {
                    this.health  = basic.getDouble(PacketUtil.getShortVariableName("health"));
                } else if (PacketUtil.getShortVariableName("damage").equals(key)) {
                    this.damage  = basic.getDouble(PacketUtil.getShortVariableName("damage"));
                } else if (PacketUtil.getShortVariableName("team").equals(key)) {
                    this.team  = basic.getInt(PacketUtil.getShortVariableName("team"));
                } else if (PacketUtil.getShortVariableName("isAlive").equals(key)) {
                    this.isAlive  = basic.getBoolean(PacketUtil.getShortVariableName("isAlive"));
                } else if (PacketUtil.getShortVariableName("rotation").equals(key)) {
                    this.nextRotation  = basic.getDouble(PacketUtil.getShortVariableName("rotation"));
                }else if(PacketUtil.getShortVariableName("score").equals(key)){
                    this.score=basic.getDouble(PacketUtil.getShortVariableName("score"));
                }else if(PacketUtil.getShortVariableName("shield").equals(key)){
                    this.shield=basic.getDouble(PacketUtil.getShortVariableName("shield"));
                }
            });
        }
        /*this.prevPosition.set(this.position);
        this.prevBoundingBox=this.boundingBox.copy();
        this.position.set(this.nextPosition);
        this.boundingBox=this.nextBoundingBox.copy();
        //this.tickDelta=0;
        if(!cs.isServer)this.resetTickDelta();*/
        this.isDamageTick=health<prevHealth;
    }
    public void tick(){
        if(Double.isNaN(this.score)) {
            System.out.println("nan");
        }
        if(Double.isNaN(this.health)){
            this.health=this.prevHealth;
        }
        this.prevPosition.set(this.position);
        this.prevBoundingBox=this.boundingBox.copy();
        this.prevRotation=this.rotation;
        this.prevShield=this.shield;
        this.mass=this.boundingBox.xSize()*this.boundingBox.ySize()*1/(sizeMultiplier*sizeMultiplier);
        lifeTime++;
        if(isParticle){
            this.resetTickDelta();
            Box b=this.boundingBox;
            //this.boundingBox=b.expand(particleBoundingBoxExpand/(b.xSize()*b.xSize()),particleBoundingBoxExpand/(b.ySize()*b.ySize()));
            if(lifeTime>=particleLifeTimeMax){
                this.kill();
            }
            this.move(this.velocity.add(extraVelocity));
            this.extraVelocity.multiply1(extraVelocityD);
        }
        if(cs.isServer) {
            this.resetTickDelta();
            if(checkBorderCollision&&!this.boundingBox.intersects(cs.borderBox)){
                this.velocity.offset(this.position.subtract(cs.borderBox.getCenter()).multiply(-1).limit(0.5));
            }
            this.move(this.velocity.add(extraVelocity));
            this.extraVelocity.multiply1(extraVelocityD);
            if(this.weapon!=null&&!(this instanceof PlayerEntity)&&!(this instanceof VisitorEntity)){
                this.weapon.tick(true,cs.isServer);
            }
            if(EntityUtils.isInsideWall(boundingBox.expand(-0.01,-0.01))){
                this.insideWall=true;
                this.health-=this.health*0.02+5;
            }else{
                this.insideWall=false;
            }
            this.prevHealth=health;

        }else{
            if(this.nextPosition!=null){
                this.position.set(this.nextPosition);
                this.nextPosition=null;
            }
            if(this.nextBoundingBox!=null){
                this.boundingBox=this.nextBoundingBox.copy();
                this.nextBoundingBox=null;
            }
            this.rotation=nextRotation;
            if(Math.abs(rotation-prevRotation)>180){
                if(rotation>prevRotation){
                    rotation-=360;
                }else{
                    rotation+=360;
                }
            }
        }
    }
    public void move(Vec2d v){
        this.position.offset(v);
        this.boundingBox.offset1(v);
    }
    public BulletType addMultipliers(BulletType b){
        return b.copy();
    }
    public double addReloadMultiplier(double b){
        return b;
    }
    public double getSizeMultiplier(){
        return 1;
    }
    public void updateChunk(){
        ChunkPos pos=getChunkPos();
        cs.chunkMap.addEntity(this,pos);
    }
    public ChunkPos getChunkPos(){
        return new ChunkPos(round(this.position.x/chunkSize),round(this.position.y/chunkSize));
    }
    public void resetTickDelta(){
        this.tickDelta=0;
    }
    public void render(Graphics g){
        this.tickDelta= Screen.tickDelta;//Math.min(1,this.tickDelta+ Screen.tickDeltaAdd);
        if(weapon!=null){
            weapon.render(g);
        }
    }
    public void setPosition(Vec2d position){
        this.prevPosition.set(this.position);
        this.prevBoundingBox=this.boundingBox.copy();
        this.boundingBox.offset1(position.subtract(this.position));
        this.position.set(position.copy());
        this.velocity.set(0,0);
    }
    public void setPosition2(Vec2d position){
        this.boundingBox.offset1(position.subtract(this.position));
        this.position.set(position.copy());
    }
    public Vec2d getTargetingPos(){
        return this.targetingPos;
    }
    public Vec2d getRealVelocity(){
        return this.position.subtract(this.prevPosition);
    }
    public JSONObject addJSON(JSONObject o) {
        JSONObject basic = new JSONObject();
        basic.put(PacketUtil.getShortVariableName("position"), this.position.toJSON());
        basic.put(PacketUtil.getShortVariableName("boundingBox"), this.boundingBox.toJSON());
        basic.put(PacketUtil.getShortVariableName("health"),(int)this.health);
        basic.put(PacketUtil.getShortVariableName("damage"),(int)this.damage);
        basic.put(PacketUtil.getShortVariableName("team"),this.team);
        basic.put(PacketUtil.getShortVariableName("id"), this.id);
        basic.put(PacketUtil.getShortVariableName("isAlive"),this.isAlive);
        basic.put(PacketUtil.getShortVariableName("rotation"),(int)this.rotation);
        basic.put(PacketUtil.getShortVariableName("score"),(int)this.score);
        basic.put(PacketUtil.getShortVariableName("shield"),(int)this.shield);
        PacketUtil.put(o,"basic", basic);
        return o;
    }
    public JSONObject addSmallJSON(JSONObject o){
        JSONObject basic = new JSONObject();
        basic.put(PacketUtil.getShortVariableName("boundingBox"), this.boundingBox.toJSON());
        basic.put(PacketUtil.getShortVariableName("id"), this.id);
        basic.put(PacketUtil.getShortVariableName("isAlive"),this.isAlive);
        basic.put(PacketUtil.getShortVariableName("rotation"),(int)this.rotation);
        PacketUtil.put(o,"basic", basic);
        return o;
    }
    public JSONObject addMediumJSON(JSONObject o){
        JSONObject basic = new JSONObject();
        basic.put(PacketUtil.getShortVariableName("boundingBox"), this.boundingBox.toJSON());
        basic.put(PacketUtil.getShortVariableName("id"), this.id);
        basic.put(PacketUtil.getShortVariableName("isAlive"),this.isAlive);
        basic.put(PacketUtil.getShortVariableName("rotation"),(int)this.rotation);
        basic.put(PacketUtil.getShortVariableName("health"),(int)this.health);
        PacketUtil.put(o,"basic", basic);
        return o;
    }
    public JSONObject toJSON() {
        return null;
    }
    public String getType(){
        return "entity";
    }
    public JSONObject getUpdate(){
        return null;
    }
    public void kill(){
        if(!this.isAlive) return;
        this.isAlive=false;
        addScore();
    }
    public boolean killed(){
        return !this.isAlive;
    }
    public Box getRenderBoundingBox(){
        return Util.lerp(this.prevBoundingBox,this.boundingBox,tickDelta);
    }
    public Vec2d getRenderPosition(){
        return Util.lerp(this.prevPosition,this.position,tickDelta);
    }
    public Vec2d getBulletPosition(){
        return Util.lerp(this.prevPosition,this.position,tickDelta+1);
    }
    public double getRenderHealth(){
        return Util.lerp(this.prevHealth,this.health,tickDelta);
    }
    public double getRenderShield(){
        return Util.lerp(this.prevShield,this.shield,tickDelta);
    }
    public double getRenderRotation(){
        return Util.lerp(this.prevRotation,this.rotation,tickDelta);
    }
    public void storeDamage(Entity e,double damage){
        List<Long> toRemove=new ArrayList<>();
        for(Long id:damageTaken.keySet()){
            DamageSource ds=damageTaken.get(id);
            if(ds!=null&&ds.isExpired()){
                toRemove.add(id);
            }
        }
        for(Long id:toRemove){
            damageTaken.remove(id);
        }

        if(!damageTaken.containsKey(e.getDamageSourceID())){
            damageTaken.put(e.getDamageSourceID(),new DamageSource(e.getDamageSourceID(),damage));
        }
        damageTaken.get(e.getDamageSourceID()).increase(damage);
    }
    public double getRenderAlpha(){
        return isParticle? Math.clamp(0.5* (particleLifeTimeMax - lifeTime+tickDelta) /particleLifeTimeMax,0.01,0.99):1;
    }
    public double getTickDelta(){
        return isParticle?Screen.tickDelta:tickDelta;
    }
    public void addScore(){
        double total=0;
        for(DamageSource ds:damageTaken.values()){
            total+=ds.damage;
        }
        if(total<0.001) return;
        for(DamageSource ds:damageTaken.values()){
            Entity e=cs.entities.get(ds.id);
            if(e!=null){
                e.addScore(this.id,this.score*0.5*ds.damage/total);
            }
        }
    }
    public void addScoreAdd(){
        this.scoreAdd.forEach((k,v)->{
            if(!Double.isNaN(v)) {
                this.score += v;
            }
        });
        this.scoreAdd.clear();
    }
    public void addScore(long id,double d){
        this.scoreAdd.put(id,d);
    }
    public void addDamage(double dmg){
        this.health-=dmg;
    }
    public long getDamageSourceID(){
        return this.id;
    }
    public long getOwnerID(){
        return this.id;
    }
    public Vec2d getPos() {
        return this.position;
    }
    public double getFovMultiplier(){
        return 1;
    }
}
