package modules.entity;

import engine.math.Vec2d;
import engine.math.util.EntityUtils;
import modules.entity.bullet.BulletEntity;

import static engine.modules.EngineMain.cs;

public class MobEntity extends Entity{
    public MobEntity(){
        super();
        this.team=-1;
    }
    public void tick(){
        if(!cs.isServer) {
            super.tick();
            return;
        }
        if(this.health<=0){
            this.kill();
        }
        super.tick();
        updateCollision();

    }
    private void updateCollision(){
        EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive),e->EntityUtils.intersectsCircle(this,e),e->{
            if(e.team!=this.team) {
                this.health-=e.damage;
                storeDamage(e,e.damage);
                if(e instanceof BulletEntity b) {
                    this.extraVelocity.offset(EntityUtils.getKnockBackVector(this,b,b.knockBackFactor/this.mass));
                }
            }
            if(!(e instanceof BulletEntity)) {
                Vec2d coll = EntityUtils.getPushVector(this, e);
                this.velocity.offset(coll);
            }
        });
    }
}
