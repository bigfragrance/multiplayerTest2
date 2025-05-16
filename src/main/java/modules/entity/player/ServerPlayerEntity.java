package modules.entity.player;

import engine.math.Vec2d;
import modules.ctrl.ServerInputManager;
import modules.network.ServerNetworkHandler;
import modules.network.packet.Packet;
import modules.network.packet.s2c.PlayerDataS2CPacket;
import org.json.JSONObject;

import static java.lang.Math.floor;

public class ServerPlayerEntity extends PlayerEntity{
    public static double[] skillPointMultipliersMax={1,0.75,1,1,0.8};
    public ServerInputManager inputManager=new ServerInputManager();
    public int upgradeTimer=0;
    public int skillPointNow=0;
    public int skillPointUsed=0;
    public ServerNetworkHandler networkHandler=null;
    public ServerPlayerEntity(Vec2d position) {
        super(position);
    }
    public void tick(){
        skillPointNow=(int)floor(score*scoreMultiplier);
        this.targetingPos=inputManager.aimPos;
        updateSkillPoint();
        whenAlive();

        this.velocity.multiply1(0.5);
        whenAlive();
        //updateBullet(1);
        super.tick();
        this.updateCollision();
        sendPacket(new PlayerDataS2CPacket(skillPoints,skillPointNow-skillPointUsed));
        this.noEnemyTimer=Math.max(0,this.noEnemyTimer-1);
    }
    public void whenAlive(){
        if(!this.isAlive) return;
        Vec2d input=new Vec2d(inputManager.side,inputManager.forward);
        input=input.limit(PlayerEntity.speed);
        this.velocity.offset(input);

        if(this.health<=0){
            this.kill();
            this.health=0;
        }

        this.health+=healthRegen;
        this.health=Math.min(this.health,healthMax);

    }
    public JSONObject getUpdate(){
        return super.getUpdate();
    }
    private void sendPacket(Packet<?> packet){
        if(networkHandler==null) return;
        networkHandler.send(packet.toJSON());
    }
    private void updateSkillPoint(){
        skillPointNow=(int)floor(score*scoreMultiplier);
        for(int i=0;i<skillPoints.length;i++){
            if(skillPointUsed>=skillPointNow){
                break;
            }
            if(inputManager.upgradingSkill==i){
                skillPointLevels[i]+=0.1;
                skillPoints[i]=1+getMultiplier(skillPointLevels[i],skillPointMultipliersMax[i]);
                upgradeTimer=2;
                skillPointUsed++;
                break;
            }
        }
    }
    public void updateBullet(double time){
        if(weapon==null||!isAlive) return;
        this.weapon.setMultiplier(skillPoints);
        weapon.update(time);
        if(inputManager.shoot){
            weapon.shoot();
        }
    }
    public static double getMultiplier(double level,double max){
        return -max/(level+1)+max;
    }
}
