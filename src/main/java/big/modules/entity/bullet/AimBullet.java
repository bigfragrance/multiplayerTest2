package big.modules.entity.bullet;

import big.engine.math.BlockPos;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.pathing.Calculator;
import big.engine.math.util.pathing.Path;
import big.engine.modules.EngineMain;
import big.modules.entity.Attackable;
import big.modules.entity.Entity;
import big.modules.entity.player.ServerPlayerEntity;

import static big.engine.modules.EngineMain.cs;

public class AimBullet extends BulletEntity{
    public static float dragFactorDef=0.85;
    public static float flyRange=1;
    public Attackable owner=null;
    public Vec2d aimPos=null;
    public float speedAdd=2;
    public boolean infinityLifeTime=false;
    public boolean pathing=false;
    public float dragFactor=dragFactorDef;
    private Calculator calculator=null;
    public float attackDistance=0;
    public AimBullet(Vec2d position, Vec2d velocity,int team,BulletType type) {
        super(position, velocity,team,type);
    }
    public void tick(){
        if(infinityLifeTime)this.lifeTime=0;
        if(!cs.entities.containsKey(this.ownerId)){
            kill();
        }
        super.tick();
        if(cs.isServer){
            this.velocity.multiply1(dragFactor);
            updateAim();
        }
    }
    public Vec2d getTargetingPos(){
        Vec2d target=null;
        if(owner!=null){
            if(owner.isFiring()) {
                target= owner.getAimPos();
            }else{
                target=super.getTargetingPos();
            }
        }
        if(aimPos!=null) {
            target=aimPos.subtract(this.position);
        }
        return target;
    }
    public void updateAim(){
        Vec2d target=null;
        if(owner!=null){
            if(owner.isFiring()) {
                target= owner.getAimPos();
            }else{
                Vec2d sub=this.position.subtract(owner.getPosition());
                float r=sub.angle();
                target=owner.getPosition().add(new Vec2d(r+30).limit(flyRange));
            }
        }
        if(aimPos!=null) {
            target=aimPos;
        }
        if(target!=null){
            Vec2d sub=target.subtract(this.position);
            sub=sub.limit(sub.length()-attackDistance);
            target=this.position.add(sub);
            if(pathing){
                Vec2d pathPos=getPathPos(target);
                if(pathPos!=null) {
                    target = pathPos;
                }
            }
        }
        if(target!=null) {
            Vec2d offVel = target.subtract(this.position).limitOnlyOver(speedAdd * 5).multiply(0.2);
            this.velocity.offset(offVel);
            this.rotation = offVel.angle();
        }
    }
    private Vec2d getPathPos(Vec2d target){
        if(calculator==null) calculator=new Calculator(this);
        Path p=calculator.getPath(BlockPos.ofFloor(this.position).toCenterPos(),target.copy());
        if(p==null) return null;
        p.update();
        return p.getMoveToNow();
    }
    public void updateCollision(){
        EntityUtils.updateCollision(this,e->(e.id==this.id||!e.isAlive),e->EntityUtils.intersectsCircle(this,e),e->{
            if (e.team != this.team) {
                EntityUtils.takeDamage(this,e);
            }
            if(!(e instanceof BulletEntity)||e instanceof AimBullet) {
                Vec2d coll = EntityUtils.getPushVector(this, e);
                this.velocity.offset(coll);
            }
        });
        //this.velocity.set(EntityUtils.getReboundVelocity(velocity,this.boundingBox));

        if(EntityUtils.isInsideWall(this.boundingBox.expand(0.0000001,0.0000001))){
            this.health=-1;
        }
        /*if(EntityUtils.isInsideWall(this.boundingBox)){
            this.health=-1;
        }*/
    }
}
