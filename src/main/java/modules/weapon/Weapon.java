package modules.weapon;

import engine.math.Box;
import engine.math.Vec2d;
import modules.entity.bullet.AimBullet;
import modules.entity.bullet.BulletEntity;
import modules.entity.Entity;
import modules.entity.MobEntity;
import modules.entity.player.PlayerEntity;

import static engine.modules.EngineMain.cs;

public class Weapon{
    public static double damageBase=10;
    public static double speedBase=5;
    public static double healthBase=1;
    public static int reloadBase=10;
    public static double sizeBase=5;
    public static double autoTargetDistanceMax=300;
    public static double extrapolateBase=1.5;
    public static double extrapolateCheckMax=10;
    public static double extrapolateCheckStep=0.5;
    public Entity owner;
    public double damage;
    public double speed;
    public double health;
    public double reload;
    public double size;
    public Weapon(Entity owner){
        this.owner=owner;
        this.damage=10;
        this.speed=5;
        this.health=1;
        this.reload=10;
        this.size=5;
    }
    public void setMultiplier(double[] multiplier){
        this.damage=damageBase*multiplier[0];
        this.speed=speedBase*multiplier[1];
        this.health=healthBase*multiplier[2];
        this.reload= (reloadBase/multiplier[3]);
        this.size=sizeBase*multiplier[4];
    }
    public void update(double time){

    }
    public void shoot(){

    }
    public static Weapon get(Entity owner,int type){
        switch (type){
            case(0)->{
                return new Weapon0(owner);
            }
            case(1)->{
                return new Weapon1(owner);
            }
            case(2)->{
                return new Weapon2(owner);
            }
            case(3)->{
                return new Weapon3(owner);
            }
            case(4)->{
                return new Weapon4(owner);
            }
            case(5)->{
                return new Weapon5(owner);
            }
            case(6)->{
                return new Weapon6(owner);
            }
            case(7)->{
                return new Weapon7(owner);
            }
            case(8)->{
                return new Weapon8(owner);
            }
            case(9)->{
                return new Weapon9(owner);
            }
            case(10)->{
                return new Weapon10(owner);
            }
            case(11)->{
                return new Weapon11(owner);
            }
            case(12)->{
                return new Weapon12(owner);
            }
            case(13)->{
                return new Weapon13(owner);
            }
            case(14)->{
                return new Weapon14(owner);
            }
            case(1258764)->{
                return new Weapon00(owner);
            }
        }
        return null;
    }
    public BulletEntity shootBullet(Vec2d pos,Vec2d velocity,double size,double health,double damage){
        BulletEntity b=new BulletEntity(pos,velocity,new Box(pos,size,size),health,damage,owner.team);
        b.ownerId=owner.id;
        cs.addEntity(b);
        return b;
    }
    public AimBullet shootAimBullet(Vec2d pos,Vec2d velocity,double size,double health,double damage){
        AimBullet b=new AimBullet(pos,velocity,new Box(pos,size,size),health,damage,owner.team);
        b.ownerId=owner.id;
        cs.addEntity(b);
        return b;
    }
    public Entity getTarget(Vec2d targeting){
        Entity bestPlayer=null;
        Entity bestOther=null;
        double best1=10000;
        double best2=10000;
        double rot=targeting.angle();
        for(Entity e:cs.entities.values()){
            if(e.team==this.owner.team) continue;
            if(e instanceof BulletEntity) continue;
            if(e.position.distanceTo(owner.position)>autoTargetDistanceMax) continue;
            if(e instanceof MobEntity m){
                double d=e.position.distanceTo(owner.position);
                if(d<best2){
                    best2=d;
                    bestOther=e;
                }
            }
            if(e instanceof PlayerEntity p){
                double rot2=targeting.angle();
                double abs=Math.abs(rot-rot2);
                if(abs<best1){
                    best1=abs;
                    bestPlayer=e;
                }
            }
        }
        if(bestPlayer!=null) return bestPlayer;
        return bestOther;
    }
    public int getOwnedBullets(){
        int count=0;
        for(Entity e:cs.entities.values()){
            if(e instanceof BulletEntity b){
                if(b.ownerId==owner.id) count++;
            }
        }
        return count;
    }
    public Vec2d extrapolate(Entity e,double tick){
        return e.position.add(e.velocity.multiply(tick));
    }
    public Vec2d extrapolate(Entity e,Vec2d shootPos, double bulletSpeed){
        double distance=e.position.distanceTo(shootPos);
        double speed=e.velocity.length();
        Vec2d first=extrapolate(e,distance/speed);
        Vec2d sub=first.subtract(shootPos);
        Vec2d bVel=sub.limit(bulletSpeed);
        double speedD=bVel.add(e.velocity).length();
        double times=distance/speedD;
        first=extrapolate(e,times+extrapolateBase);

        sub=first.subtract(shootPos);
        bVel=sub.limit(bulletSpeed);
        speedD=bVel.add(e.velocity).length();
        times=distance/speedD;
        first=extrapolate(e,times+extrapolateBase);

        sub=first.subtract(shootPos);
        bVel=sub.limit(bulletSpeed);
        speedD=bVel.add(e.velocity).length();
        times=distance/speedD;
        first=extrapolate(e,times+extrapolateBase);
        return first;
    }
    public Vec2d extrapolate2(Entity e,Vec2d shootPos, double bulletSpeed){
        Vec2d bestPos=null;
        double minDiff=1000;
        for(double i=0;i<extrapolateCheckMax;i+=extrapolateCheckStep){
            Vec2d pos=extrapolate(e,i);
            double dist=pos.subtract(shootPos).length();
            double diff=Math.abs(i-dist/bulletSpeed);
            if(diff<minDiff){
                minDiff=diff;
                bestPos=pos;
            }
        }
        return bestPos==null?e.position:bestPos;
    }
}
