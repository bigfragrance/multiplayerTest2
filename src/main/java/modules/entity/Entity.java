package modules.entity;

import engine.math.BlockPos2d;
import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.PacketUtil;
import engine.math.util.Util;
import engine.modules.EngineMain;
import engine.render.Screen;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static engine.math.util.Util.round;
import static engine.modules.EngineMain.chunkSize;
import static engine.modules.EngineMain.cs;
import static engine.render.Screen.SCREEN_BOX;

public abstract class Entity implements NetworkItem {
    public Box boundingBox;
    public Box prevBoundingBox;
    public Vec2d position;
    public Vec2d prevPosition;
    public Vec2d velocity;
    public long id;
    public boolean isAlive=true;
    Vec2d nextPosition=null;
    Box nextBoundingBox=null;
    public double health;
    public double damage;
    public int team;
    public double rotation;
    public double prevRotation;
    public double score=0;
    public static double collisionVector=0.2;
    public static double collisionMax=10;
    public HashMap<Long,DamageSource> damageTaken=new HashMap<>();
    private double lastHealth=0;
    public boolean isDamageTick=false;
    public double mass=400;
    public double tickDelta=0;
    @Override
    public void update(JSONObject o) {
        lastHealth=this.health;
        if(o.has("basic")){
            //this.prevPosition = this.position.copy();
            JSONObject basic = o.getJSONObject("basic");
            basic.keys().forEachRemaining(key -> {
                if(PacketUtil.getShortString("position").equals(key)){
                    JSONObject position = basic.getJSONObject(PacketUtil.getShortString("position"));
                    this.nextPosition =Vec2d.fromJSON(position);
                } else if (PacketUtil.getShortString("boundingBox").equals(key)) {
                    JSONObject boundingBox = basic.getJSONObject(PacketUtil.getShortString("boundingBox"));
                    this.nextBoundingBox  = Box.fromJSON(boundingBox);
                } else if (PacketUtil.getShortString("health").equals(key)) {
                    this.health  = basic.getDouble(PacketUtil.getShortString("health"));
                } else if (PacketUtil.getShortString("damage").equals(key)) {
                    this.damage  = basic.getDouble(PacketUtil.getShortString("damage"));
                } else if (PacketUtil.getShortString("team").equals(key)) {
                    this.team  = basic.getInt(PacketUtil.getShortString("team"));
                } else if (PacketUtil.getShortString("isAlive").equals(key)) {
                    this.isAlive  = basic.getBoolean(PacketUtil.getShortString("isAlive"));
                } else if (PacketUtil.getShortString("rotation").equals(key)) {
                    this.rotation  = basic.getDouble(PacketUtil.getShortString("rotation"));
                }else if(PacketUtil.getShortString("score").equals(key)){
                    this.score=basic.getDouble(PacketUtil.getShortString("score"));
                }
            });
        }
        /*this.prevPosition.set(this.position);
        this.prevBoundingBox=this.boundingBox.copy();
        this.position.set(this.nextPosition);
        this.boundingBox=this.nextBoundingBox.copy();
        //this.tickDelta=0;
        if(!cs.isServer)this.resetTickDelta();*/
        isDamageTick=health<lastHealth;
    }
    public void tick(){
        this.prevPosition.set(this.position);
        this.prevBoundingBox=this.boundingBox.copy();
        this.prevRotation=this.rotation;
        this.mass=this.boundingBox.xSize()*this.boundingBox.ySize();
        if(cs.isServer||this.id==cs.player.id) {
            this.resetTickDelta();
            if(!this.boundingBox.intersects(cs.borderBox)){
                this.velocity.offset(this.position.subtract(cs.borderBox.getCenter()).multiply(-1).limit(12));
            }
            this.position.offset(this.velocity);
            this.boundingBox.offset1(this.velocity);
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
        basic.put(PacketUtil.getShortString("position"), this.position.toJSON());
        basic.put(PacketUtil.getShortString("boundingBox"), this.boundingBox.toJSON());
        basic.put(PacketUtil.getShortString("health"),this.health);
        basic.put(PacketUtil.getShortString("damage"),this.damage);
        basic.put(PacketUtil.getShortString("team"),this.team);
        basic.put(PacketUtil.getShortString("id"), this.id);
        basic.put(PacketUtil.getShortString("isAlive"),this.isAlive);
        basic.put(PacketUtil.getShortString("rotation"),this.rotation);
        basic.put(PacketUtil.getShortString("score"),this.score);
        o.put("basic", basic);

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
