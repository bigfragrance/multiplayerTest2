package modules.entity.bullet;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import modules.entity.Entity;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;

public class BulletEntity extends Entity {
    public long ownerId;
    private boolean invisibleTick=false;
    public BulletType type;
    public int maxLifeTime=30;
    public double knockBackFactor=20;
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
        this.type=new BulletType();
        invisibleTick=true;
        this.rotation=velocity.angle();
        this.prevRotation=rotation;
    }
    public void tick(){
        super.tick();
        if(!this.boundingBox.equals(this.prevBoundingBox)){
            invisibleTick=false;
        }
        if(!cs.isServer) return;
        if(lifeTime>maxLifeTime||health<=0||cs.entities.get(this.ownerId)==null){
            kill();
        }
        updateRotation();
        invisibleTick=false;
        updateCollision();
        lifeTime++;
    }
    private void updateRotation(){
        if(this.velocity.length()>0.0000001){
            this.rotation=this.velocity.angle();
        }
        //this.rotation+=10;
    }
    public void updateCollision(){
        EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive||e.team==this.team),e->EntityUtils.intersectsCircle(this,e),e->{
            this.health-=e.damage;
        });
    }
    public void update(JSONObject o){
        super.update(o);
        this.type=BulletType.fromJSON(PacketUtil.getJSONObject(o,"bType"));
    }
    public void kill(){
        super.kill();
    }
    public void render(Graphics g){
        if(invisibleTick) return;
        super.render(g);
        EntityUtils.renderBullet(g,this);
    }
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),"bullet");
        PacketUtil.put(o,"bType",this.type.toJSON());
        super.addJSON(o);
        return o;
    }
    public String getType(){
        return "bullet";
    }
    public static BulletEntity fromJSON(JSONObject o){
        JSONObject basic=PacketUtil.getJSONObject(o,"basic");
        BulletEntity e=new BulletEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("position"))),new Vec2d(0,0),Box.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("boundingBox"))),basic.getDouble(PacketUtil.getShortVariableName("health")),basic.getDouble(PacketUtil.getShortVariableName("damage")),basic.getInt(PacketUtil.getShortVariableName("team")));
        e.id=basic.getLong(PacketUtil.getShortVariableName("id"));
        e.type=BulletType.fromJSON(PacketUtil.getJSONObject(o,"bType"));
        e.update(o);
        //e.boundingBox=Box.fromJSON(basic.getJSONObject(PacketUtil.getShortString("boundingBox")));
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,"entity_update");
        PacketUtil.put(o,"id",this.id);
        PacketUtil.put(o,"bType",this.type.toJSON());
        super.addJSON(o);
        return o;
    }
    /*public EntityParticle toParticle(){
        EntityParticle p=new EntityParticle(this.position.copy(),this.velocity.copy(),this.boundingBox.copy(),this.health,this.damage,this.team);
        p.id=this.id;
        p.ownerId=this.ownerId;
        p.type=this.type;
        p.velocity=this.position.subtract(this.prevPosition);
        p.rotation=this.rotation;
        return p;
    }*/
    public long getDamageSourceID(){
        return this.ownerId;
    }
}
