package modules.entity;

import engine.math.BlockPos2d;
import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.PacketUtil;
import engine.math.util.PacketVariable;
import engine.math.util.Util;
import engine.render.Screen;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static engine.math.util.PacketVariable.basic;
import static engine.math.util.Util.round;
import static engine.modules.EngineMain.chunkSize;
import static engine.modules.EngineMain.cs;

public abstract class Entity implements NetworkItem {
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
    protected Vec2d nextPosition=null;
    protected Box nextBoundingBox=null;
    public double health;
    public double prevHealth;
    public double damage;
    public int team;
    public double rotation;
    public double prevRotation;
    public double score=0;
    public static double collisionVector=0.1;
    public static double collisionMax=6;
    public HashMap<Long,DamageSource> damageTaken=new HashMap<>();
    public boolean isDamageTick=false;
    public double mass=400;
    private double tickDelta=0;
    public Vec2d targetingPos=null;
    public boolean isParticle=false;
    public int lifeTime=0;
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
                } else if (PacketUtil.getShortVariableName("boundingBox").equals(key)) {
                    JSONObject boundingBox = basic.getJSONObject(PacketUtil.getShortVariableName("boundingBox"));
                    Box last=this.boundingBox.copy();
                    this.nextBoundingBox  = Box.fromJSON(boundingBox);
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
                    this.rotation  = basic.getDouble(PacketUtil.getShortVariableName("rotation"));
                }else if(PacketUtil.getShortVariableName("score").equals(key)){
                    this.score=basic.getDouble(PacketUtil.getShortVariableName("score"));
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
        this.prevPosition.set(this.position);
        this.prevBoundingBox=this.boundingBox.copy();
        this.prevRotation=this.rotation;
        this.mass=this.boundingBox.xSize()*this.boundingBox.ySize();
        lifeTime++;
        if(isParticle){
            this.resetTickDelta();
            Box b=this.boundingBox;
            this.boundingBox=b.expand(particleBoundingBoxExpand/(b.xSize()*b.xSize()),particleBoundingBoxExpand/(b.ySize()*b.ySize()));
            if(lifeTime>=particleLifeTimeMax){
                this.kill();
            }
            this.position.offset(this.velocity.add(extraVelocity));
            this.boundingBox.offset1(this.velocity.add(extraVelocity));
            this.extraVelocity.multiply1(extraVelocityD);
        }
        if(cs.isServer) {
            this.resetTickDelta();
            if(!this.boundingBox.intersects(cs.borderBox)){
                this.velocity.offset(this.position.subtract(cs.borderBox.getCenter()).multiply(-1).limit(12));
            }
            this.position.offset(this.velocity.add(extraVelocity));
            this.boundingBox.offset1(this.velocity.add(extraVelocity));
            this.extraVelocity.multiply1(extraVelocityD);
        }else{
            if(this.nextPosition!=null){
                this.position.set(this.nextPosition);
                this.nextPosition=null;
            }
            if(this.nextBoundingBox!=null){
                this.boundingBox=this.nextBoundingBox.copy();
                this.nextBoundingBox=null;
            }
        }
    }
    public void updateChunk(){
        BlockPos2d pos=getChunkPos();
        cs.chunkMap.addEntity(this,cs.chunkMap.blockPos(pos));
    }
    public BlockPos2d getChunkPos(){
        return new BlockPos2d(round(this.position.x/chunkSize),round(this.position.y/chunkSize));
    }
    public void resetTickDelta(){
        this.tickDelta=0;
    }
    public void render(Graphics g){
        this.tickDelta= Screen.tickDelta;//Math.min(1,this.tickDelta+ Screen.tickDeltaAdd);
    }
    public void setPosition(Vec2d position){
        this.prevPosition.set(this.position);
        this.prevBoundingBox=this.boundingBox.copy();
        this.boundingBox.offset1(position.subtract(this.position));
        this.position.set(position.copy());
        this.velocity.set(0,0);
    }
    public JSONObject addJSON(JSONObject o) {
        JSONObject basic = new JSONObject();
        basic.put(PacketUtil.getShortVariableName("position"), this.position.toJSON());
        basic.put(PacketUtil.getShortVariableName("boundingBox"), this.boundingBox.toJSON());
        basic.put(PacketUtil.getShortVariableName("health"),this.health);
        basic.put(PacketUtil.getShortVariableName("damage"),this.damage);
        basic.put(PacketUtil.getShortVariableName("team"),this.team);
        basic.put(PacketUtil.getShortVariableName("id"), this.id);
        basic.put(PacketUtil.getShortVariableName("isAlive"),this.isAlive);
        basic.put(PacketUtil.getShortVariableName("rotation"),this.rotation);
        basic.put(PacketUtil.getShortVariableName("score"),this.score);
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
    public double getRenderRotation(){
        return Util.lerp(this.prevRotation,this.rotation,tickDelta);
    }
    public void storeDamage(Entity e,double damage){
        List<Long> toRemove=new ArrayList<>();
        for(Long id:damageTaken.keySet()){
            DamageSource ds=damageTaken.get(id);
            if(ds.isExpired()){
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
        for(DamageSource ds:damageTaken.values()){
            Entity e=cs.entities.get(ds.id);
            if(e!=null){
                e.score+=this.score*0.5*ds.damage/total;
            }
        }
    }
    public long getDamageSourceID(){
        return this.id;
    }
}
