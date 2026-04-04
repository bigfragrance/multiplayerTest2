package big.game.entity;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.render.Screen;
import big.engine.util.EntityUtils;
import big.engine.util.PacketUtil;
import big.engine.util.Util;
import big.game.entity.bullet.BulletEntity;
import big.game.entity.player.PlayerEntity;
import big.game.entity.player.ServerPlayerEntity;
import big.game.weapon.CanAttack;
import big.game.weapon.GunList;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static big.engine.modules.EngineMain.cs;

public class DominatorEntity extends ServerPlayerEntity {
    //                                       {          "Damage[z]","Speed[x]","Health[c]","Size[v]","Reload[b]","MoveSpeed[n]","DamageAbsorb[m]","ShieldRegen[,]","HealthRegen[.]","Fov[/]"};
    public static double[] defSkillPoints= new double[]{1.2         ,1.2       ,1.2          ,3        ,1.5          ,0           ,30               ,0.1             ,0.1           ,0.8};
    public static List<String> dominatorTypes;
    public static AtomicInteger dominatorIDCounter=new AtomicInteger(0);
    public int initialTeam;
    private String initialWeaponID;
    private boolean canRespawn=true;
    public DominatorEntity(Vec2d position,int team,String name,String weaponID) {
        super(position);
        this.team=team;
        this.initialTeam=team;
        this.name=name;
        this.initialWeaponID=weaponID;
        this.weaponID=weaponID;
        this.skillPoints=defSkillPoints.clone();
    }
    public void tick(){
        this.mass=1e20d;
        this.velocity.set(0,0);
        this.inputManager.shoot=true;
        this.inputManager.defend=true;
        this.rotation+=1;
        /*if(this.weapon!=null) {
            for (CanAttack a : weapon.list.values()) {
                a.owner = this;
                a.team=this.team;
            }
        }*/
        super.tick();
        if(!isAlive&&canRespawn){
            if(Screen.sc.inputManager.isSpawningBullet()){
                return;
            }
            this.isAlive=true;
            this.health=healthMax;
            if(this.team==initialTeam){
                this.team=(lastHurtBy==null||lastHurtBy.team==initialTeam) ?-1:lastHurtBy.team;
                this.weaponID="dai";
                this.weapon=null;
            }else{
                this.team=initialTeam;
                this.weaponID=initialWeaponID;
                this.weapon= GunList.fromID(this,weaponID);
            }
        }
    }
    protected void updateCollision(){
        EntityUtils.updateCollision(this, e->(e.id==this.id||!e.isAlive), e->EntityUtils.intersectsCircle(this,e), e->{
            if (e.team != this.team) {
                if(this.noEnemyTimer<=0){
                    EntityUtils.takeDamage(this,e);
                }
            }
        });
    }
    public static void init(){
        dominatorIDCounter.set(0);
        dominatorTypes= new ArrayList<>();
        for(String name:GunList.data.keySet()){
            if(name.contains("Dominator")){
                dominatorTypes.add(name);
            }
        }
    }
    public JSONObject toJSONServer(){
        JSONObject json=new JSONObject();
        json.put("team",initialTeam);
        json.put("weaponID",initialWeaponID);
        json.put("name",name);
        json.put("position",position.toJSON());
        return json;
    }
    public static DominatorEntity fromJSONServer(JSONObject o){
        DominatorEntity e=new DominatorEntity(o.getVec2d("position"),PacketUtil.getInt(o,"team"),PacketUtil.getString(o,"name"),PacketUtil.getString(o,"weaponID"));
        //e.boundingBox= Box.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("boundingBox")));
        //e.update(o);
        return e;
    }
    public boolean killed(){
        return !this.isAlive;
    }
    public void kill(int reason){
        super.kill(reason);
        if(reason==KillReason.CLEAR){
            canRespawn=false;
        }
    }
}
