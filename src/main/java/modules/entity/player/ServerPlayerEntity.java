package modules.entity.player;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.Util;
import modules.ctrl.ServerInputManager;
import modules.entity.Attackable;
import modules.entity.Controllable;
import modules.network.ServerNetworkHandler;
import modules.network.packet.Packet;
import modules.network.packet.s2c.PlayerDataS2CPacket;
import modules.weapon.GunList;
import org.json.JSONObject;

import static java.lang.Math.floor;

public class ServerPlayerEntity extends PlayerEntity implements Attackable, Controllable {

    public static double drag=0.67;
    public ServerInputManager inputManager=null;
    public int upgradeTimer=0;
    public int skillPointNow=0;
    public int skillPointUsed=0;
    public String weaponID="dai";
    public ServerNetworkHandler networkHandler=null;
    public ServerPlayerEntity(Vec2d position) {
        super(position);
        inputManager=new ServerInputManager();
        this.score=100/scoreMultiplier+10;
    }
    public void tick(){
        skillPointNow=(int)floor(score*scoreMultiplier);
        this.targetingPos=inputManager.aimPos;
        updateSkillPoint();
        whenAlive();

        this.velocity.multiply1(drag);
        whenAlive();
        if(this.weapon==null){
            try {
                this.weapon = GunList.fromID(this, weaponID);
            }catch (Exception e){
                this.weapon=null;
            }
        }
        this.size=SIZE*getSizeMultiplier();
        this.boundingBox=new Box(position,size,size);
        //updateBullet(1);
        super.tick();
        updateBullet();
        this.updateCollision();
        sendPacket(new PlayerDataS2CPacket(skillPoints,skillPointNow-skillPointUsed));
        this.noEnemyTimer=Math.max(0,this.noEnemyTimer-1);
        if(name.equals(noEnemyID)){
            this.health=healthMax;
            this.speed=SPEED*3;
        }else{
            this.speed=SPEED*skillPoints[5];
        }
    }
    public void whenAlive(){
        if(!this.isAlive) return;
        Vec2d input=new Vec2d(inputManager.side,inputManager.forward);
        input=input.limit(speed);
        this.velocity.offset(input);

        if(this.health<=0){
            this.kill();
            this.health=0;
        }
        regenShieldAndHealth();
    }
    public JSONObject getUpdate(){
        return super.getUpdate();
    }
    private void sendPacket(Packet<?> packet){
        if(networkHandler==null) return;
        networkHandler.send(packet.toJSON());
    }
    private void updateSkillPoint(){
        skillPointNow= (int)floor(score*scoreMultiplier);
        for(int i=0;i<skillPoints.length;i++){
            if(skillPointUsed>=skillPointNow){
                break;
            }
            if(inputManager.upgradingSkill==i){
                if(skillPointUsed>=100){
                    instantRegen();
                    upgradeTimer=2;
                    skillPointUsed++;
                    break;
                }
                if(skillPointLevels[i]>=18){
                    break;
                }
                skillPointLevels[i]+=1;
                skillPoints[i]=1+getMultiplier(skillPointLevels[i],skillPointMultipliersMax[i]);
                upgradeTimer=2;
                //instantRegen();
                skillPointUsed++;
                break;
            }
        }
    }
    private void instantRegen(){
        if(!havingShield){
            havingShield=true;
            shield=shieldMax;
            return;
        }
        health+=100;
    }
    public void updateBullet(){
        if(weapon==null||!isAlive) return;
        weapon.tick(inputManager.shoot);
    }
    public static double getMultiplier(double level,double max){
        return max*level/8-0.8;
    }

    @Override
    public Vec2d getAimPos() {
        return inputManager.aimPos.add(this.position);
    }

    @Override
    public boolean isFiring() {
        return inputManager.shoot;
    }

    @Override
    public void setRotation(double rotation) {
        this.rotation=rotation;
    }

    @Override
    public ServerInputManager getInputManager() {
        return inputManager;
    }

    @Override
    public Vec2d getPosition() {
        return position;
    }

    @Override
    public GunList getWeapon() {
        return weapon;
    }

    @Override
    public double getSpeed() {
        return speed;
    }
}
