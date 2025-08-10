package big.modules.entity.bullet;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import big.modules.entity.BlockEntity;
import big.modules.entity.Entity;
import org.json.JSONObject;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;

public class BulletEntity extends Entity {
    public static double[] baseValues={10,0.1,4,0.1,100,20,0.003,0.05};//damage speed health size lifeTime kbFactor randomVelocity selfKB
    public long ownerId;
    private boolean invisibleTick=false;
    public BulletType type;
    public int maxLifeTime=100;
    public double knockBackFactor=20;
    public BulletEntity(Vec2d position, Vec2d velocity,int team,BulletType type){
        super();
        this.position=position;
        this.prevPosition=position.copy();
        this.velocity=velocity;
        this.type=type;
        this.health=baseValues[2]*type.getMultiplier(2);
        this.damage=baseValues[0]*type.getMultiplier(0);
        this.maxLifeTime=(int) (baseValues[4]*type.getMultiplier(4));
        this.knockBackFactor=baseValues[5]*type.getMultiplier(5);
        this.boundingBox=new Box(position,baseValues[3]*type.getMultiplier(3));
        this.prevBoundingBox=boundingBox.copy();
        this.team=team;
        invisibleTick=true;
        this.rotation=velocity.angle();
        this.prevRotation=rotation;
        this.checkBorderCollision=false;
    }
    public void tick(){
        super.tick();
        if(!cs.isServer) return;
        //Entity e=cs.entities.get(ownerId);
        if(lifeTime>maxLifeTime||health<=0/*||e==null||!e.isAlive*/){
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
    public void move(Vec2d vec){
        //Box b=this.boundingBox.stretch(this.velocity.x,this.velocity.y);
        /*EntityUtils.updateCollision(this,e->e.id==this.id||!e.isAlive||!(e instanceof BlockEntity),e->e.boundingBox.intersects(b),(e)->{
            BlockEntity block=(BlockEntity)e;
            vec.set(EntityUtils.getMaxMove(this.boundingBox,vec,e.boundingBox,block.leftCheck,block.rightCheck,block.topCheck,block.buttonCheck));
        });*/
        vec.set(insideWall?vec: EntityUtils.getMaxMove(this.boundingBox,vec));
        this.position.offset(vec);
        this.boundingBox=this.boundingBox.offset(vec);
    }
    public void updateCollision(){
        EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive),e->EntityUtils.intersectsCircle(this,e),e->{
            if (e.team != this.team) {
                EntityUtils.takeDamage(this,e);
                /*if(e instanceof BulletEntity b) {
                    this.velocity.offset(EntityUtils.getKnockBackVector(this,b,b.knockBackFactor/this.mass));
                }*/
            }
            /*if(!(e instanceof BulletEntity)) {
                Vec2d coll = EntityUtils.getPushVector(this, e);
                this.velocity.offset(coll);
            }*/
        });
        //this.velocity.set(EntityUtils.getReboundVelocity(velocity,this.boundingBox));

        if(EntityUtils.isInsideWall(this.boundingBox.expand(0.0000001,0.0000001))){
            this.health=-1;
        }
    }
    public Vec2d getTargetingPos(){
        return this.velocity.multiply(10);
    }
    public void update(JSONObject o){
        super.update(o);
        //this.type=BulletType.fromJSON(PacketUtil.getJSONObject(o,"bType"));
        this.invisibleTick=false;
    }
    public void kill(){
        super.kill();
    }
    public void render(Graphics g){
        if(invisibleTick) return;
        super.render(g);
        //g.setColor(EntityUtils.getTeamcolor(this.team));
        //Util.renderCube(g,boundingBox.switchToJFrame());
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
        BulletEntity e=new BulletEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("position"))),new Vec2d(0,0),PacketUtil.getInt(basic,"team"),BulletType.fromJSON(PacketUtil.getJSONObject(o,"bType")));
        e.id=basic.getLong(PacketUtil.getShortVariableName("id"));
        e.update(o);
        //e.boundingBox=Box.fromJSON(basic.getJSONObject(PacketUtil.getShortString("boundingBox")));
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,"entity_update");
        //PacketUtil.put(o,"bType",this.type.toJSON());
        super.addSmallJSON(o);
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
    public static double getMultipliedValue(int index,BulletType type){
        return baseValues[index]*type.getMultiplier(index);
    }
    public long getOwnerID(){
        return this.ownerId;
    }
    public long getDamageSourceID(){
        return this.ownerId;
    }
}
