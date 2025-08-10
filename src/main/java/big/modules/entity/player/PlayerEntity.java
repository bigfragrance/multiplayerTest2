package big.modules.entity.player;

import big.engine.math.BlockPos;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import big.modules.entity.Attackable;
import big.modules.entity.bullet.BulletEntity;
import big.modules.entity.Entity;
import big.modules.entity.bullet.BulletType;
import big.modules.network.packet.c2s.WantWeaponC2SPacket;
import big.modules.weapon.Weapon;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;

import static big.engine.math.util.Util.random;
import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class PlayerEntity extends Entity {
    public static String defName="Player"+(char)65501;
    public static double healthMax=400;
    public static double healthRegen=0.15;
    public static double shieldRegen=0.7;
    public static int shieldRespawn=400;
    public static double shieldMax=80;
    public static double scoreMultiplier=0.001;
    public static int maxSkillPoints=50;
    //                                     0           1          2           3          4            5               6                7                 8
    public static String[] skillNames=              {"Damage[z]","Speed[x]","Health[c]","Size[v]","Reload[b]","MoveSpeed[n]","DamageAbsorb[m]","ShieldRegen[,]","HealthRegen[.]","Fov[/]"};
    public static double[] skillPointMultipliersMax={0.8        ,1         ,0.8        ,1      ,1          ,1             ,1                ,1               ,0.8               ,0.7};
    public static double SPEED=1*sizeMultiplier;
    public static double SIZE=10*sizeMultiplier;
    public static String noEnemyID="God";
    public String name=defName;
    public double[] skillPoints= Util.createDoubles(0.2,10);
    public double[] skillPointLevels=Util.createDoubles(0,10);
    public int noEnemyTimer=0;
    public double speed=SPEED;
    public boolean havingShield=false;
    protected double size;
    public PlayerEntity(Vec2d position) {
        super();
        this.size=SIZE;
        this.weapon=null;
        this.position=position;
        this.velocity=new Vec2d(0,0);
        this.prevPosition=position.copy();
        this.boundingBox=new Box(position,size,size);
        this.prevBoundingBox=boundingBox.copy();
        this.health=PlayerEntity.healthMax;
        this.damage=2;
        this.shield=shieldMax;
    }
    public void tick() {
        super.tick();
        if(!cs.isServer){
            if(weapon!=null) weapon.tick(false,cs.isServer);
        }
    }

    protected void updateCollision(){
        ///(1+this.score*scoreMultiplier);
        EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive),e->EntityUtils.intersectsCircle(this,e),e->{
            if (e.team != this.team) {
                if(this.noEnemyTimer<=0){
                    EntityUtils.takeDamage(this,e);
                }
                if(e instanceof BulletEntity b) {
                    this.velocity.offset(EntityUtils.getKnockBackVector(this,b,b.knockBackFactor/this.mass));
                }
            }
            if(!(e instanceof BulletEntity)) {
                Vec2d coll = EntityUtils.getPushVector(this, e);
                this.velocity.offset(coll);
            }
        });
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
    public BulletType addMultipliers(BulletType b){
        BulletType bb=b.copy();
        for(int i=0;i<4;i++){
            bb.multipliers[i]*=skillPoints[i];
        }
        return bb;
    }
    public double addReloadMultiplier(double d){
        return d*skillPoints[4];
    }
    public double getSizeMultiplier(){
        return this instanceof ServerPlayerEntity? skillPoints[3]:this.boundingBox.avgSize()*0.5/SIZE;
    }
    public void addDamage(double d){
        d=d/skillPoints[6];
        if(havingShield){
            if(shield>=d){
                this.shield-=d;
                this.health-=d*0.5;
                return;
            }
            this.shield-=d;
            if(this.shield<0){
                havingShield=false;
                this.health-=d;
                this.shield = -shieldRespawn;
            }
        }else{
            this.health-=d;
            this.shield = -shieldRespawn;
        }
        //debug
        /*if(health<=0){
            health=healthMax;
            shield=shieldMax;
        }*/
    }
    public void regenShieldAndHealth(){
        double healthRegen=PlayerEntity.healthRegen*skillPoints[8];
        double shieldRegen=PlayerEntity.shieldRegen*skillPoints[7];
        this.health=Math.min(healthMax,this.health+healthRegen);
        if(havingShield){
            this.shield=Math.min(shieldMax+1,this.shield+shieldRegen);
            if(this.shield>=shieldMax){
                this.health=Math.min(healthMax,this.health+shieldRegen*2);
            }
        }else{
            this.shield=Math.min(shieldMax,this.shield+shieldRegen);
            if(shield>=0){
                havingShield=true;
            }
        }
    }
    public double getFov(){
        return skillPoints[9];
    }
    public void updateStatus(JSONObject o){
        super.update(o);
        this.position.set(this.nextPosition);
        this.boundingBox=this.nextBoundingBox.copy();
        this.prevPosition.set(this.nextPosition);
        this.prevBoundingBox=this.nextBoundingBox.copy();
    }
    public void respawn(){
        this.isAlive=true;
        this.setPosition(EntityUtils.getRandomSpawnPosition(this.team));
        this.health=PlayerEntity.healthMax;
        this.shield=PlayerEntity.shieldMax;
        this.noEnemyTimer=0;
        //this.score*=0.5;
    }
    public void render(Graphics g){
        super.render(g);
        EntityUtils.render(g,this);
        if(!cs.isServer)sc.renderAtLast((gr)->{
            sc.storeAndSetDef();
            EntityUtils.render(gr,Util.toMiniMap(this.boundingBox.expand(0.3,0.3)),EntityUtils.getTeamcolor(this.team));
            sc.restoreZoom();
        });
        //EntityUtils.renderHealthBar(g,this,);
        //System.out.println(this.name);
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
        if(weapon!=null)PacketUtil.put(o,"wUpdate",weapon.getUpdate());
        super.addJSON(o);
        return o;
    }
    public void update(JSONObject o){
        super.update(o);
        if(PacketUtil.contains(o,"wUpdate")) {
            if (weapon == null) {
                cs.networkHandler.sendPacket(new WantWeaponC2SPacket(this.id));
                return;
            }
            this.weapon.update(PacketUtil.getJSONArray(o, "wUpdate"));
        }
    }
    public boolean killed(){
        return false;
    }

    public BlockPos getBlockPos() {
        return BlockPos.ofFloor(this.position);
    }
    public double getFovMultiplier(){
        return getFov();
    }

}
