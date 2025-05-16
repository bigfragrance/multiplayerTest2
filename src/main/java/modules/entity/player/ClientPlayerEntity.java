package modules.entity.player;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import engine.render.Screen;
import modules.ctrl.InputManager;
import modules.ctrl.ServerInputManager;
import modules.network.packet.c2s.UpdateWeaponC2SPacket;
import modules.weapon.Weapon;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;
import static java.lang.Math.floor;

public class ClientPlayerEntity extends PlayerEntity {
    public ServerInputManager serverInputManager=new ServerInputManager();
    public InputManager inputManager=Screen.INSTANCE.inputManager;
    protected int playerDataUpdateTimer=0;
    public int skillPointCanUse=0;
    public int currentWeapon=-1;
    public int lastAutoFire=0;
    public boolean autoFire=false;
    public ClientPlayerEntity(Vec2d position) {
        super(position);
        inputManager=Screen.INSTANCE.inputManager;
    }
    public void tick(){
        /*if(this.weapon==null){
            this.weapon= Weapon.get(this,cs.setting.getChosenTank());
        }*/
        this.name=cs.setting.getName();
        if(playerDataUpdateTimer<0) {
            cs.networkHandler.sendPlayerData(this);
            cs.networkHandler.send(new UpdateWeaponC2SPacket(cs.setting.getChosenTank()));
            playerDataUpdateTimer=50;
        }
        if(currentWeapon!=cs.setting.getChosenTank()){
            cs.networkHandler.send(new UpdateWeaponC2SPacket(cs.setting.getChosenTank()));
            currentWeapon=cs.setting.getChosenTank();
        }
        playerDataUpdateTimer--;
        super.tick();
        updateInput();
        if(isAlive) {
            /*this.velocity.multiply1(0.3);
            this.velocity.offset(input.multiply(speed));
            this.updateCollision(false);*/
            //cs.networkHandler.sendPlayerMove(this.position);
            //updateBullet();
        }else{
            /*this.velocity=new Vec2d(0,0);
            this.prevPosition.set(this.position);
            this.prevBoundingBox=this.boundingBox.copy();*/
            if(inputManager.isRespawning()){
                cs.networkHandler.sendPlayerRespawn();
            }
        }
    }
    private void updateInput(){
        updateSkillPoint();
        int[] input = inputManager.getPlayerInput();
        if(inputManager.enableAutoFire()&&lastAutoFire<=0){
            autoFire=!autoFire;
            lastAutoFire=10;
        }
        serverInputManager.forward=input[1];
        serverInputManager.side=input[0];
        serverInputManager.aimPos=inputManager.getMouseVec();
        serverInputManager.shoot=inputManager.isShooting()||autoFire;
        serverInputManager.sendUpdate();
    }
    private void updateSkillPoint(){
        //if(skillPointCanUse<=0) return;
        serverInputManager.upgradingSkill=-1;
        for(int i=0;i<skillPoints.length;i++){
            if(inputManager.isUpgrading(i)){
                serverInputManager.upgradingSkill=i;
                break;
            }
        }
    }

    public void render(Graphics g){
        super.render(g);
        EntityUtils.renderSkillPoints(getSkillPointRenderPosition(),skillPoints,skillPointCanUse);
    }
    private Vec2d getSkillPointRenderPosition(){
        return Screen.SCREEN_BOX.getMaxPos().add(-100,-50);
    }
    public static ClientPlayerEntity fromJSON(JSONObject o){
        JSONObject basic=o.getJSONObject(PacketUtil.getShortVariableName("basic"));
        ClientPlayerEntity e=new ClientPlayerEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("position"))));
        e.id=basic.getLong(PacketUtil.getShortVariableName("id"));
        e.boundingBox= Box.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("boundingBox")));
        e.update(o);
        return e;
    }

}
