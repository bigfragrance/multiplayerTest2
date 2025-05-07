package modules.entity;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import engine.render.Screen;
import modules.ctrl.InputManager;
import modules.weapon.Weapon;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;
import static java.lang.Math.floor;

public class ClientPlayerEntity extends PlayerEntity{
    public InputManager inputManager=Screen.INSTANCE.inputManager;
    protected int playerDataUpdateTimer=0;
    public double[] skillPoints= {1,1,1,1,1};
    protected int skillPointNow=0;
    protected int skillPointUsed=0;
    protected int upgradeTimer=0;
    public ClientPlayerEntity(Vec2d position) {
        super(position);
        inputManager=Screen.INSTANCE.inputManager;
    }
    public void tick(){
        if(this.weapon==null){
            this.weapon= Weapon.get(this,cs.setting.getChosenTank());
        }
        updateSkillPoint();
        this.weapon.setMultiplier(skillPoints);
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
    }
    private void updateSkillPoint(){
        skillPointNow=(int)floor(score*scoreMultiplier);
        for(int i=0;i<skillPoints.length;i++){
            if(skillPointUsed>=skillPointNow){
                break;
            }
            if(inputManager.isUpgrading(i)){
                skillPoints[i]+=0.1;
                upgradeTimer=3;
                skillPointUsed++;
                break;
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
    public void render(Graphics g){
        super.render(g);
        EntityUtils.renderSkillPoints(g,getSkillPointRenderPosition(),skillPoints,skillPointNow-skillPointUsed);
    }
    private Vec2d getSkillPointRenderPosition(){
        return Screen.SCREEN_BOX.getMaxPos().add(-100,-50);
    }
    public static ClientPlayerEntity fromJSON(JSONObject o){
        JSONObject basic=o.getJSONObject("basic");
        ClientPlayerEntity e=new ClientPlayerEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortString("position"))));
        e.id=basic.getLong(PacketUtil.getShortString("id"));
        e.boundingBox= Box.fromJSON(basic.getJSONObject(PacketUtil.getShortString("boundingBox")));
        e.update(o);
        return e;
    }
}
