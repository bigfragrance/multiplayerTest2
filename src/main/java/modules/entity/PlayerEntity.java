package modules.entity;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import engine.math.util.Util;
import engine.render.Screen;
import modules.ctrl.InputManager;
import modules.weapon.Weapon;
import org.json.JSONObject;

import java.awt.*;

import static engine.math.util.Util.random;
import static engine.modules.EngineMain.cs;
import static engine.render.Screen.*;

public class PlayerEntity extends Entity{
    public static double healthMax=100;
    public static double healthRegen=1;
    public InputManager inputManager=Screen.INSTANCE.inputManager;
    public double speed=12;
    public double size=10;
    private long lastShoot=0;
    private long lastRespawn=0;
    public String name="Player";
    private boolean playerDataSent=false;
    private int playerDataUpdateTimer=0;
    private Weapon weapon=null;
    public int noEnemyTimer=0;
    public static double scoreMultiplier=0.00001;
    public PlayerEntity(Vec2d position) {
        super();
        this.position=position;
        this.velocity=new Vec2d(0,0);
        this.prevPosition=position.copy();
        this.boundingBox=new Box(position,size,size);
        this.prevBoundingBox=boundingBox.copy();
        inputManager=Screen.INSTANCE.inputManager;
        this.health=PlayerEntity.healthMax;;
        this.damage=1;
    }
    public void tick() {
        if(!cs.isServer){
            if(cs.player!=null&& this.id==cs.player.id) {
                if(this.weapon==null){
                    this.weapon=Weapon.get(this,cs.setting.getChosenTank());
                }
                this.weapon.setMultiplier(1+this.score*scoreMultiplier);
                this.name=cs.setting.getName();
                if(playerDataUpdateTimer<0) {
                    cs.networkHandler.sendPlayerData(this);
                    playerDataUpdateTimer=100;
                }
                playerDataUpdateTimer--;

                if(isAlive) {
                    Vec2d input = inputManager.getPlayerInput();
                    this.velocity.multiply1(0.3);
                    this.velocity.offset(input.multiply(speed));
                    this.updateCollision(false);
                    super.tick();
                    cs.networkHandler.sendPlayerMove(this.position);
                    updateBullet();
                }else{
                    this.velocity=new Vec2d(0,0);
                    this.prevPosition.set(this.position);
                    this.prevBoundingBox=this.boundingBox.copy();
                    if(inputManager.isRespawning()){
                        cs.networkHandler.sendPlayerRespawn();
                    }
                }
            }else{
                super.tick();
            }
        }
        else{
            this.noEnemyTimer=Math.max(0,this.noEnemyTimer-1);
            if(this.health<=0&&this.isAlive){
                this.kill();
                this.health=0;
            }
            if(this.isAlive) {
                this.updateCollision(true);
                this.health+=healthRegen;
                this.health=Math.min(this.health,healthMax);
            }
            /*JSONObject o = new JSONObject();
            o.put("type", "entity_update");
            super.addJSON(o);
            cs.multiClientHandler.clients.forEach(c -> c.serverNetworkHandler.send(o));*/
        }
    }
    private void updateCollision(boolean server){
        double dmgMultiplier=1/(1+this.score*scoreMultiplier);
        for(Entity e:cs.entities.values()){
            if(e.id==this.id) continue;
            if(!e.isAlive) continue;
            //if(e.team==this.team) continue;

            boolean intersects=e.boundingBox.intersectsCircle(this.boundingBox)|| EntityUtils.intersectsCircle(this.prevBoundingBox,this.boundingBox,e.prevBoundingBox,e.boundingBox);

            if(intersects){
                if(server) {
                    if (e.team != this.team) {
                        if(this.noEnemyTimer<=0){
                            this.health -= e.damage*dmgMultiplier;
                            storeDamage(e,e.damage);
                        }
                    }
                }else {
                    if(!(e instanceof BulletEntity)) {
                        Vec2d coll = EntityUtils.getPushVector(this, e);
                        this.velocity.offset(coll);
                    }
                }
            }
        }
    }
    public void updateBullet(){
        if(weapon==null) return;
        weapon.update();
        if(inputManager.isShooting()){
            weapon.shoot();
        }
    }
    public void update2(JSONObject o){
        if(o.has("basic")){
            //this.prevPosition = this.position.copy();
            JSONObject basic = o.getJSONObject("basic");
            String healthKey = PacketUtil.getShortString("health");
            String damageKey = PacketUtil.getShortString("damage");
            String teamKey = PacketUtil.getShortString("team");
            String isAliveKey = PacketUtil.getShortString("isAlive");
            String scoreKey = PacketUtil.getShortString("score");
            basic.keys().forEachRemaining(key -> {
                if (healthKey.equals(key))  {
                    this.health  = basic.getDouble(healthKey);
                } else if (damageKey.equals(key))  {
                    this.damage  = basic.getDouble(damageKey);
                } else if (teamKey.equals(key))  {
                    this.team  = basic.getInt(teamKey);
                } else if (isAliveKey.equals(key))  {
                    this.isAlive  = basic.getBoolean(isAliveKey);
                }else if(scoreKey.equals(key)){
                    this.score=basic.getDouble(scoreKey);
                }
            });
        }
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
        o.put("type","player");
        super.addJSON(o);
        return o;
    }
    public String getType(){
        return "player";
    }
    public static PlayerEntity fromJSON(JSONObject o){
        JSONObject basic=o.getJSONObject("basic");
        PlayerEntity e=new PlayerEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortString("position"))));
        e.id=basic.getLong(PacketUtil.getShortString("id"));
        e.boundingBox=Box.fromJSON(basic.getJSONObject(PacketUtil.getShortString("boundingBox")));
        e.update(o);
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        o.put("type","entity_update");
        o.put(PacketUtil.getShortString("id"),this.id);
        super.addJSON(o);
        return o;
    }
    public boolean killed(){
        return false;
    }
}
