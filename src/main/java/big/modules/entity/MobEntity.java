package big.modules.entity;

import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.timer.IntTimer;
import big.modules.entity.bullet.BulletEntity;
import big.modules.entity.player.PlayerEntity;

import static big.engine.modules.EngineMain.cs;

public class MobEntity extends Entity{
    public static int MOB_TEAM=-1;
    public static int LIFE_TIME=60*10*20;
    public static int ATTACK_RANGE=4;
    public boolean attackPlayer=false;
    public boolean autoTargeting=false;
    public PlayerEntity target=null;
    private IntTimer targetTimer=new IntTimer(5);
    public MobEntity(){
        super();
        this.team=MOB_TEAM;
    }
    public void tick(){
        if(!cs.isServer) {
            super.tick();
            return;
        }
        if(this.health<=0||this.lifeTime>=LIFE_TIME){
            this.kill();
        }
        super.tick();
        updateCollision();
        updateTarget();
    }
    public void updateTarget(){
        if(!this.autoTargeting) return;
        targetTimer.update();
        if(targetTimer.passed()){
            targetTimer.reset();
            EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive),e->this.position.distanceTo(e.position)<=ATTACK_RANGE,e->{
                if(e.team!=this.team) {
                    if(e instanceof PlayerEntity){
                        this.target=(PlayerEntity)e;
                    }
                }
            });
        }
    }
    private void updateCollision(){
        EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive),e->EntityUtils.intersectsCircle(this,e),e->{
            if(e.team!=this.team) {
                EntityUtils.takeDamage(this,e);
                if(e instanceof BulletEntity b) {
                    this.velocity.offset(EntityUtils.getKnockBackVector(this,b,b.knockBackFactor/this.mass));
                }
            }
            if(!(e instanceof BulletEntity)) {
                Vec2d coll = EntityUtils.getPushVectorNew(this, e);
                this.move(coll);
            }
        });
    }
}
