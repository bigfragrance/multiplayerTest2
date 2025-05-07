package modules.entity;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import engine.math.util.Util;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;

public class BulletEntity extends Entity{
    public int lifeTime=0;
    public long ownerId;
    public BulletEntity(Vec2d position, Vec2d velocity, Box boundingBox,double health,double damage,int team){
        super();
        this.position=position;
        this.prevPosition=position.copy();
        this.velocity=velocity;
        this.boundingBox=boundingBox;
        this.prevBoundingBox=boundingBox.copy();
        this.health=health;
        this.damage=damage;
        this.team=team;
    }
    public void tick(){
        if(lifeTime>20||health<=0){
            kill();
        }
        super.tick();
        if(!cs.isServer) return;
        updateCollision();
        lifeTime++;
    }
    private void updateCollision(){
        EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive||e.team==this.team),e->EntityUtils.intersectsCircle(this,e),e->{
            this.health-=e.damage;
        });
    }
    public void update(JSONObject o){
        super.update(o);
    }
    public void render(Graphics g){
        super.render(g);
        EntityUtils.render(g,this);
    }
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        o.put("type","bullet");
        super.addJSON(o);
        return o;
    }
    public String getType(){
        return "bullet";
    }
    public static BulletEntity fromJSON(JSONObject o){
        JSONObject basic=o.getJSONObject("basic");
        BulletEntity e=new BulletEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortString("position"))),new Vec2d(0,0),Box.fromJSON(basic.getJSONObject(PacketUtil.getShortString("boundingBox"))),basic.getDouble(PacketUtil.getShortString("health")),basic.getDouble(PacketUtil.getShortString("damage")),basic.getInt(PacketUtil.getShortString("team")));
        e.id=basic.getLong(PacketUtil.getShortString("id"));
        e.update(o);
        //e.boundingBox=Box.fromJSON(basic.getJSONObject(PacketUtil.getShortString("boundingBox")));
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        o.put("type","entity_update");
        o.put(PacketUtil.getShortString("id"),this.id);
        super.addJSON(o);
        return o;
    }
    public long getDamageSourceID(){
        return this.ownerId;
    }
}
