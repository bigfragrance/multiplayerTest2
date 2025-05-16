package modules.entity.player;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import modules.entity.bullet.BulletEntity;
import modules.entity.Entity;
import modules.weapon.Weapon;
import org.json.JSONObject;

import java.awt.*;

import static engine.math.util.Util.random;
import static engine.modules.EngineMain.cs;

public class PlayerEntity extends Entity {
    public static double healthMax=100;
    public static double healthRegen=1;
    public static String[] skillNames={"Damage","Speed","Health","Reload","Size"};
    public static double speed=3;
    public static double size=10;
    public String name="Player";
    public double[] skillPoints= {1,1,1,1,1};
    public double[] skillPointLevels={0,0,0,0,0};
    public Weapon weapon=null;
    public int noEnemyTimer=0;
    public static double scoreMultiplier=0.1;

    public PlayerEntity(Vec2d position) {
        super();
        this.weapon=new Weapon(this);
        this.position=position;
        this.velocity=new Vec2d(0,0);
        this.prevPosition=position.copy();
        this.boundingBox=new Box(position,size,size);
        this.prevBoundingBox=boundingBox.copy();
        this.health=PlayerEntity.healthMax;;
        this.damage=1;
    }
    public void tick() {
        super.tick();

        if(cs.isServer){

            /*JSONObject o = new JSONObject();
            o.put(PacketUtil.getShortVariableName("type"), "entity_update");
            super.addJSON(o);
            cs.multiClientHandler.clients.forEach(c -> c.serverNetworkHandler.send(o));*/
        }
    }

    protected void updateCollision(){
        double dmgMultiplier=1;///(1+this.score*scoreMultiplier);

        EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive),e->EntityUtils.intersectsCircle(this,e),e->{
            System.out.println("collision");
            if (e.team != this.team) {
                if(this.noEnemyTimer<=0){
                    this.health -= e.damage*dmgMultiplier;
                    storeDamage(e,e.damage);
                }
                if(e instanceof BulletEntity b) {
                    this.extraVelocity.offset(EntityUtils.getKnockBackVector(this,b,b.knockBackFactor/this.mass));
                }
            }
            if(!(e instanceof BulletEntity)) {
                Vec2d coll = EntityUtils.getPushVector(this, e);
                this.velocity.offset(coll);
            }
        });
    }

    
    public void updateStatus(JSONObject o){
        super.update(o);
        this.position.set(this.nextPosition);
        this.boundingBox=this.nextBoundingBox.copy();
        this.prevPosition.set(this.nextPosition);
        this.prevBoundingBox=this.nextBoundingBox.copy();
    }
    public void render(Graphics g){
        super.render(g);
        EntityUtils.render(g,this);
        EntityUtils.renderPlayerName(g,this);
        EntityUtils.renderScore(g,this);
    }
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),"player");
        super.addJSON(o);
        return o;
    }
    public String getType(){
        return "player";
    }
    public static PlayerEntity fromJSON(JSONObject o){
        JSONObject basic=PacketUtil.getJSONObject(o,"basic");
        PlayerEntity e=new PlayerEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("position"))));
        e.id=basic.getLong(PacketUtil.getShortVariableName("id"));
        e.boundingBox=Box.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("boundingBox")));
        e.update(o);
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,"entity_update");
        o.put(PacketUtil.getShortVariableName("id"),this.id);
        super.addJSON(o);
        return o;
    }
    public boolean killed(){
        return false;
    }
}
