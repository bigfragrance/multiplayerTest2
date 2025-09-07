package big.modules.entity.player;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.Util;
import big.modules.ctrl.ServerInputManager;
import big.modules.entity.Attackable;
import big.modules.entity.Controllable;
import big.modules.network.ServerNetworkHandler;
import big.modules.network.packet.Packet;
import big.modules.network.packet.s2c.MessageS2CPacket;
import big.modules.network.packet.s2c.PlayerDataS2CPacket;
import big.modules.network.packet.s2c.PlayerWeaponUpdateS2CPacket;
import big.modules.weapon.GunList;
import org.json.JSONObject;

import static big.engine.modules.EngineMain.cs;
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
        super.tick();
        updateBullet();
        this.updateCollision();
        sendPacket(new PlayerDataS2CPacket(skillPoints,skillPointNow-skillPointUsed));
        this.noEnemyTimer=Math.max(0,this.noEnemyTimer-1);
        if(name.equals(noEnemyID)){
            this.health=healthMax;
            this.speed=SPEED*5;
        }else{
            this.speed=SPEED*skillPoints[5];
            if(inputManager.defend){
                this.speed*=0.2;
            }
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
        if(inputManager.defend){
            regenShieldAndHealth();
            regenShieldAndHealth();
            regenShieldAndHealth();
        }
    }
    public void respawn(){
        this.isAlive=true;
        this.setPosition(EntityUtils.getRandomSpawnPosition(this.team));
        this.health=PlayerEntity.healthMax;
        this.shield=PlayerEntity.shieldMax;
        this.noEnemyTimer=0;
        this.weapon=null;
        this.networkHandler.send(new PlayerWeaponUpdateS2CPacket(this.id,null).toJSON());
        //this.score*=0.5;
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
                    /*if(Util.random(0,10)<2)instantRegen();
                    upgradeTimer=2;
                    skillPointUsed++;*/
                    break;
                }
                if(skillPointLevels[i]>=18){
                    break;
                }
                skillPointLevels[i]+=1;
                skillPoints[i]=skillPointDefault+ getMultiplier(skillPointLevels[i],skillPointMultipliersMax[i]);
                upgradeTimer=2;
                //instantRegen();
                skillPointUsed++;
                break;
            }
        }
    }
    public void instantRegen(){
        if(!havingShield){
            havingShield=true;
            shield=shieldMax;
            return;
        }
        health+=100;
    }
    public void updateBullet(){
        if(weapon==null||!isAlive) return;
        weapon.tick(inputManager.shoot,inputManager.defend,cs.isServer);
    }
    public static double getMultiplier(double level,double max){
        return max*level/8;
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
    public int getTeam() {
        return team;
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
