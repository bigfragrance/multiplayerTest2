package big.modules.weapon;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.modules.entity.bullet.AimBullet;
import big.modules.entity.bullet.BulletEntity;
import big.modules.entity.Entity;
import big.modules.entity.MobEntity;
import big.modules.entity.player.PlayerEntity;

import static big.engine.modules.EngineMain.cs;
import static big.modules.entity.Entity.sizeMultiplier;

public class Weapon{
    public static double damageBase=10;
    public static double speedBase=5*sizeMultiplier;
    public static double healthBase=1;
    public static int reloadBase=10;
    public static double sizeBase=5*sizeMultiplier;
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
        this.damage=damageBase;
        this.speed=speedBase;
        this.health=healthBase;
        this.reload=reloadBase;
        this.size=sizeBase;
    }
    public void setMultiplier(double[] multiplier){
        this.damage=damageBase*multiplier[0];
        this.speed=speedBase*multiplier[1];
        this.health=healthBase*multiplier[2];
        this.size=sizeBase*multiplier[3];
        this.reload= (reloadBase/multiplier[4]);
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
        }
        return null;
    }
    public static Weapon getB(Entity owner,int type){
        switch (type){

        }
        return null;
    }
    /*public BulletEntity shootBullet(Vec2d pos,Vec2d velocity,double size,double health,double damage){
        BulletEntity b=new BulletEntity(pos,velocity,new Box(pos,size,size),health,damage,owner.team);
        b.ownerId=owner.getOwnerID();
        cs.addEntity(b);
        return b;
    }
    public BulletEntity shootBullet(Vec2d pos,Vec2d velocity,double size,double health,double damage,int lifeTime){
        BulletEntity b=new BulletEntity(pos,velocity,new Box(pos,size,size),health,damage,owner.team);
        b.ownerId=owner.getOwnerID();
        b.maxLifeTime=lifeTime;
        cs.addEntity(b);
        return b;
    }
    public AimBullet shootAimBullet(Vec2d pos,Vec2d velocity,double size,double health,double damage){
        AimBullet b=new AimBullet(pos,velocity,new Box(pos,size,size),health,damage,owner.team);
        b.ownerId=owner.getOwnerID();
        cs.addEntity(b);
        return b;
    }*/
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
                if(b.ownerId==owner.id&&b instanceof AimBullet) count++;
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
