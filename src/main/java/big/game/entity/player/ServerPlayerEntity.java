package big.game.entity.player;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.Util;
import big.game.ctrl.ServerInputManager;
import big.game.entity.Attackable;
import big.game.entity.Controllable;
import big.game.entity.Entity;
import big.game.entity.bullet.AimBullet;
import big.game.entity.bullet.BulletEntity;
import big.game.network.ServerNetworkHandler;
import big.game.network.packet.Packet;
import big.game.network.packet.s2c.MessageS2CPacket;
import big.game.network.packet.s2c.PlayerDataS2CPacket;
import big.game.network.packet.s2c.PlayerWeaponUpdateS2CPacket;
import big.game.weapon.GunList;
import big.game.world.World;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static big.engine.modules.EngineMain.cs;
import static java.lang.Math.floor;

public class ServerPlayerEntity extends PlayerEntity implements Attackable, Controllable {

    public static double drag=0.67;
    public static double scorePow=1.03;
    public static double initScore=Math.pow(scorePow,30)/scoreMultiplier+1;
    public ServerInputManager inputManager=null;
    public int upgradeTimer=0;
    public int skillPointNow=0;
    public int skillPointUsed=0;
    public String weaponID="dai";
    public ServerNetworkHandler networkHandler=null;
    public ConcurrentHashMap<Entity,Vec2d> controllingShieldBullets=new ConcurrentHashMap<>();

    public ServerPlayerEntity(Vec2d position) {
        super(position);
        inputManager=new ServerInputManager();
        this.score=initScore;
    }
    public void tick(){

        this.targetingPos=inputManager.aimPos;
        this.updateSkillPoint();
        if(World.gravityEnabled){
            this.velocity.multiply1(0.98);
        }else{
            this.velocity.multiply1(drag);
        }
        if(this.weapon==null){
            try {
                this.weapon = GunList.fromID(this, weaponID);
            }catch (Exception e){
                this.weapon=null;
            }
        }
        this.whenAlive();
        this.size=SIZE*getSizeMultiplier();
        this.boundingBox=new Box(position,size,size);
        if(World.gravityEnabled){
            this.velocity.offset(0,World.gravity);
        }
        super.tick();
        this.velocity=getRealVelocity();
        this.updateBullet();
        this.updateCollision();
        this.sendData();
        this.noEnemyTimer=Math.max(0,this.noEnemyTimer-1);
        if(name.equals(noEnemyID)){
            this.health=healthMax;
            this.speed=SPEED*5;
        }else{
            this.speed=SPEED*skillPoints[5];
            if(isDefending()&&this.weaponID.contains("Def")){
                this.speed*=0.2;
            }
        }
    }
    private int getSkillPointNow(){
        double pow=score*scoreMultiplier;
        double now=Util.log(pow,scorePow);
        return Util.floor(now);
    }
    private void sendData(){
        double[] skillPoints=this.skillPoints.clone();
        skillPoints[9]=getFov();
        int nextNeed= (int) ((Math.pow(scorePow,skillPointNow+1)-Math.pow(scorePow,skillPointNow))/scoreMultiplier);
        int nowHave= (int) ((score*scoreMultiplier-Math.pow(scorePow,skillPointNow))/scoreMultiplier);
        sendPacket(new PlayerDataS2CPacket(skillPoints,skillPointNow-skillPointUsed,String.format("%d/%d",nowHave,nextNeed)));
    }
    public void whenAlive(){
        if(!this.isAlive) return;
        Vec2d input=new Vec2d(inputManager.side,inputManager.forward);
        input=input.limit(speed);
        if(World.gravityEnabled){
            if(this.isOnGround()){
                if(inputManager.forward>0){
                    this.velocity.set((input.x+velocity.x*0.2)*2,15*speed);
                }
                this.velocity.multiply1(drag,1);
            }else{
                input=input.multiply(0.3);
            }
        }
        this.velocity.offset(input);

        if(this.health<=0){
            this.kill();
            this.health=0;
        }
        regenShieldAndHealth();
        regenShieldAndHealth();
        if(isDefending()&&this.weaponID.contains("Def")){
            regenShieldAndHealth();
            regenShieldAndHealth();
            regenShieldAndHealth();
        }
    }
    public void addScore(){
        super.addScore();
        sendDeathMsg();
    }
    private void sendDeathMsg(){
        String msg;
        if(lastHurtBy instanceof PlayerEntity player){
            msg=player.name+" killed"+this.name;
        }
        else{
            msg=this.name+" have a stupid death";
        }
        cs.multiClientHandler.sendToAll(new MessageS2CPacket(msg).addHistory().toJSON());
    }
    public void respawn(){
        this.isAlive=true;
        this.setPosition(EntityUtils.getRandomSpawnPosition(this.team));
        this.health=PlayerEntity.healthMax;
        this.shield=PlayerEntity.shieldMax;
        this.noEnemyTimer=0;
        this.weapon=null;
        this.networkHandler.send(new PlayerWeaponUpdateS2CPacket(this.id,null).toJSON());
        this.score*=0.5;
        if(this.score<initScore) this.score=initScore;
        int skillPointNow=Util.floor(score*scoreMultiplier);
        if(skillPointNow<this.skillPointUsed){
            int m=skillPointUsed-skillPointNow;
            int[] arr=Util.createInts(10,i->i);
            for(int i=0;i<m;i++){
                int[] random=arr.clone();
                shuffle(random);
                for(int j=0;j<random.length;j++){
                    int k=random[j];
                    if(skillPointLevels[k]>0){
                        skillPointLevels[k]--;
                        skillPoints[k]=skillPointDefaults[k]+ getMultiplier(skillPointLevels[k],skillPointMultipliersMax[k]);
                        break;
                    }
                }
            }
            skillPointUsed=skillPointNow;
        }
    }
    public JSONObject getUpdate(){
        return super.getUpdate();
    }
    private void sendPacket(Packet<?> packet){
        if(networkHandler==null) return;
        networkHandler.send(packet.toJSON());
    }
    private void updateSkillPoint(){
        skillPointNow=getSkillPointNow();
        for(int i=0;i<skillPoints.length;i++){
            if(skillPointUsed>=skillPointNow){
                break;
            }
            if(inputManager.upgradingSkill==i){
                if(skillPointUsed>=80*80){
                    /*if(Util.random(0,10)<2)instantRegen();
                    upgradeTimer=2;
                    skillPointUsed++;*/
                    break;
                }
                if(skillPointLevels[i]>=10*80){
                    break;
                }
                skillPointLevels[i]+=1;
                skillPoints[i]=skillPointDefaults[i]+getMultiplier(skillPointLevels[i],skillPointMultipliersMax[i]);
                upgradeTimer=3;
                skillPointUsed++;
                killAllBullet();
                break;
            }
        }
    }
    private void killAllBullet(){
        for(Entity e:cs.world.getEntities()){
            if(e instanceof BulletEntity bullet){
                if(bullet.getOwnerID()==this.id){
                    bullet.kill();
                }
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
        updateShieldControl();
    }
    public void updateShieldControl(){
        ConcurrentHashMap<Entity,Vec2d> controllingShieldBullets=new ConcurrentHashMap<>();
        for(Entity b:cs.world.getEntities()){
            if(b instanceof AimBullet bullet){
                if(bullet.getOwnerID()==this.id&&bullet.isDefend){
                    controllingShieldBullets.put(bullet,bullet.getPos());
                }
            }
        }
        if(controllingShieldBullets.isEmpty()) return;
        List<Vec2d> positions=createBulletMatrix(controllingShieldBullets.size());
        for(Entity e:controllingShieldBullets.keySet()){
            Vec2d minDistance=null;
            double minDistanceNow=Double.MAX_VALUE;
            for(Vec2d p:positions){
                double distance=e.getPos().distanceTo(p.add(this.position));
                if(distance<minDistanceNow){
                    minDistanceNow=distance;
                    minDistance=p;
                }
            }
            if(minDistance!=null){
                controllingShieldBullets.put(e,minDistance);
                positions.remove(minDistance);
            }
        }
        this.controllingShieldBullets=controllingShieldBullets;
    }
    public List<Vec2d> createBulletMatrix(int count){
        List<Vec2d> list=new ArrayList<>();
        double rangeBase=1.5*skillPoints[3];
        double layerDistance=rangeBase*0.3;
        if(!inputManager.defend && !inputManager.shoot){
            List<Vec2d> formation = new ArrayList<>();
            double spacing = skillPoints[3];
            int countPlaced = 0;
            int row = 1;

            List<Vec2d> tempPositions = new ArrayList<>();


            while(countPlaced < count){
                int bulletsThisRow = Math.min(row, count - countPlaced);
                double rowOffset = (bulletsThisRow - 1) / 2.0;
                for(int col = 0; col < bulletsThisRow; col++){
                    double x = (col - rowOffset) * spacing;
                    double y = -row * spacing * Math.sqrt(3) / 2.0;
                    tempPositions.add(new Vec2d(x, y));
                    countPlaced++;
                }
                row++;
            }


            double sumX = 0, sumY = 0;
            for(Vec2d v : tempPositions){
                sumX += v.x;
                sumY += v.y;
            }
            double centerX = sumX / tempPositions.size();
            double centerY = sumY / tempPositions.size();

            double angleRad = Math.toRadians(this.rotation+30);
            for(Vec2d v : tempPositions){
                double adjustedX = v.x - centerX;
                double adjustedY = v.y - centerY;

                double rotatedX = adjustedX * Math.cos(angleRad) - adjustedY * Math.sin(angleRad);
                double rotatedY = adjustedX * Math.sin(angleRad) + adjustedY * Math.cos(angleRad);

                formation.add(new Vec2d(rotatedX, rotatedY));
            }

            list.addAll(formation);
        }
        else if((inputManager.defend&&!inputManager.shoot)){
            int bulletPerLayerMax=12;
            double off=180d/bulletPerLayerMax;
            int currentBulletPerLayer=Math.min(bulletPerLayerMax,count);
            int currentLayer=0;
            int added=0;
            int bulletLeft=count;
            for(int i=0;i<count;i++){
                if(added>=bulletPerLayerMax){
                    currentLayer++;
                    added=0;
                    currentBulletPerLayer=Math.min(bulletPerLayerMax,bulletLeft);
                }
                list.add(new Vec2d(360d* added/currentBulletPerLayer+this.rotation+(currentLayer&1)*off).multiply(rangeBase+currentLayer*layerDistance));
                added++;
                bulletLeft--;
            }
        }
        else if(inputManager.defend){
            int bulletPerLayerMax = 9;
            double span = 180d;
            double angleStep = span / bulletPerLayerMax;
            int currentBulletPerLayer = Math.min(bulletPerLayerMax, count);
            int currentLayer = 0;
            int added = 0;
            int bulletLeft = count;
            for (int i = 0; i < count; i++) {
                if (added >= bulletPerLayerMax) {
                    currentLayer++;
                    added = 0;
                    currentBulletPerLayer = Math.min(bulletPerLayerMax, bulletLeft);
                }
                double centerAngle = this.rotation;
                int mid = (currentBulletPerLayer - 1) / 2;
                int indexFromCenter = added - mid;
                double angle = centerAngle + indexFromCenter * angleStep;
                list.add(new Vec2d(angle).multiply(rangeBase + currentLayer * layerDistance));
                added++;
                bulletLeft--;
            }
        }
        else {
            int bulletPerLayerMax = 11;
            double baseSpan = 120d;
            int currentBulletPerLayer = Math.min(bulletPerLayerMax, count);
            int currentLayer = 0;
            int added = 0;
            int bulletLeft = count;
            for (int i = 0; i < count; i++) {
                if (added >= bulletPerLayerMax) {
                    currentLayer++;
                    added = 0;
                    currentBulletPerLayer = Math.min(bulletPerLayerMax, bulletLeft);
                }
                double span = Math.max(30d, baseSpan - currentLayer * 15d);
                double angleStep = span / bulletPerLayerMax;
                double centerAngle = this.rotation;
                int mid = (currentBulletPerLayer - 1) / 2;
                int indexFromCenter = added - mid;
                double angle = centerAngle + indexFromCenter * angleStep;
                list.add(new Vec2d(angle).multiply(rangeBase + currentLayer * layerDistance));
                added++;
                bulletLeft--;
            }
        }


        return list;
    }
    public static void shuffle(int[] array) {
        Random rand = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
    @Override
    public ConcurrentHashMap<Entity, Vec2d> getControllingShieldBullets() {
        return controllingShieldBullets;
    }
    public static double getMultiplier(double level,double max){
        return max*level/10;
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
    public boolean isDefending() {
        return inputManager.defend;
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
