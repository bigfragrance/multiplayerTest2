package modules.entity.bullet;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import engine.modules.EngineMain;
import modules.entity.Entity;
import modules.entity.player.PlayerEntity;
import modules.entity.player.ServerPlayerEntity;

public class AimBullet extends BulletEntity{
    public static double dragFactorDef=0.85;
    public ServerPlayerEntity owner=null;
    public Vec2d aimPos=null;
    public double speedAdd=2;
    public boolean infinityLifeTime=false;
    public double dragFactor=dragFactorDef;
    public AimBullet(Vec2d position, Vec2d velocity, Box boundingBox, double health, double damage, int team) {
        super(position, velocity, boundingBox, health, damage, team);
    }
    public void tick(){
        if(infinityLifeTime)this.lifeTime=0;
        super.tick();
        if(EngineMain.cs.isServer){
            this.velocity.multiply1(dragFactor);
            updateAim();
        }
    }
    public void updateAim(){
        Vec2d target=null;
        if(owner!=null){
            if(owner.inputManager.shoot) {
                Vec2d input = owner.inputManager.aimPos;
                target= owner.position.add(input);
            }
        }
        if(aimPos!=null) target=aimPos;
        if(target!=null) {
            Vec2d offVel = target.subtract(this.position).limitOnlyOver(speedAdd * 10).multiply(0.1);
            this.velocity.offset(offVel);
            this.rotation = offVel.angle();
        }
    }
    public void updateCollision(){
        EntityUtils.updateCollision(this, e->(e.id==this.id||!e.isAlive), e->EntityUtils.intersectsCircle(this,e), e->{
            if(e.team==this.team){
                if(e instanceof AimBullet){
                    this.velocity.offset(EntityUtils.getPushVector(this,e));
                }
            }else {
                this.health -= e.damage;
            }
        });
    }
}
