package modules.entity;

import engine.math.Vec2d;
import engine.math.util.EntityUtils;

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
        for(Entity e:cs.entities.values()){
            if(e.id==this.id) continue;
            if(!e.isAlive) continue;
            boolean intersects=e.boundingBox.intersectsCircle(this.boundingBox)|| EntityUtils.intersectsCircle(this.prevBoundingBox,this.boundingBox,e.prevBoundingBox,e.boundingBox);

            if(intersects){
                if(e.team!=this.team) {
                    this.health-=e.damage;
                    storeDamage(e,e.damage);
                }
                if(!(e instanceof BulletEntity)) {
                    Vec2d coll = EntityUtils.getPushVector(this, e);
                    this.velocity.offset(coll);
                }
            }
        }
    }
}
