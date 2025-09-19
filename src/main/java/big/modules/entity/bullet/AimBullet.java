package big.modules.entity.bullet;

import big.engine.math.BlockPos;
import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.Util;
import big.engine.math.util.pathing.Calculator;
import big.engine.math.util.pathing.Path;
import big.modules.entity.Attackable;
import big.modules.entity.Entity;
import big.modules.entity.MobEntity;
import big.modules.entity.player.PlayerEntity;

import java.util.concurrent.ConcurrentHashMap;

import static big.engine.modules.EngineMain.cs;

public class AimBullet extends BulletEntity{
    public static double dragFactorDef=0.85;
    public static double flyRange=1;
    public Attackable owner=null;
    public Vec2d aimPos=null;
    public double speedAdd=2;
    public boolean infinityLifeTime=false;
    public boolean pathing=false;
    public boolean isDefend=false;
    public double dragFactor=dragFactorDef;
    private Calculator calculator=null;
    public double attackDistance=0;
    private boolean shouldAutoAim;
    public AimBullet(Vec2d position, Vec2d velocity,int team,BulletType type) {
        super(position, velocity,team,type);
        this.shouldAutoAim =type.shouldAutoAim(Math.random());
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
            if(this.isDefend){
                target=getDefendPosition();
            }
            else if(owner.isFiring()) {
                target= owner.getAimPos();
            }else{
                Vec2d sub=this.position.subtract(owner.getPosition());
                double r=sub.angle();
                target=owner.getPosition().add(new Vec2d(r+30).limit(flyRange));
                target= Util.secondIfNull(getAutoAimPos(3*getFov()),target);
            }
        }
        if(aimPos!=null) {
            target=aimPos;
        }
        if(shouldAutoAim){
            target= Util.secondIfNull(getAutoAimPos(5*getFov()),target);
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
            if(!type.shouldCustomRotation())this.rotation = offVel.angle();
        }
    }

    private double getFov(){
        if(owner==null) return 1;
        return owner.getFov();
    }
    private Vec2d getAutoAimPos(double r){
        if(owner==null) return null;
        Vec2d target=null;
        double minDistance= r +1;
        double minDistanceMob= r;
        PlayerEntity player=null;
        Entity mob=null;
        for(Entity e:cs.entities.values()){
            if(e.team==owner.getTeam()) continue;
            if(!e.isAlive) continue;
            if(e instanceof PlayerEntity||e instanceof MobEntity){
                boolean b=e instanceof PlayerEntity;
                if(b){
                    if(((PlayerEntity) e).name.equals("God")) continue;
                }
                double distance=getPos().distanceTo(e.getPos());
                if(b?distance<minDistance:distance<minDistanceMob){
                    if(b){
                        minDistance=distance;
                        player=(PlayerEntity) e;
                    }
                    else{
                        if(e.score>500) {
                            minDistanceMob = distance;
                            mob = e;
                        }
                    }
                }
            }
        }
        if(player!=null){
            target=player.getPos();
        }
        else if(mob!=null){
            target=mob.getPos();
        }
        return target;
    }
    private Vec2d getDefendPosition(){
        if(owner==null) return null;
        ConcurrentHashMap<Entity,Vec2d> controllingShieldBullets=owner.getControllingShieldBullets();
        if(controllingShieldBullets.containsKey(this)){
            return controllingShieldBullets.get(this).add(owner.getPosition());
        }
        return null;
    }
    private double getDefendDistance(){
        if(owner==null) return 0;
        double m=1;
        if(this.type.tags.has("defendDistanceMultiplier")){
            m= this.type.tags.getDouble("defendDistanceMultiplier");
        }
        double d=owner.getAimPos().distanceTo(owner.getPosition())/2;
        return m*Math.min(d,1.5);
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
            if(e instanceof AimBullet) {
                Vec2d coll = EntityUtils.getPushVectorNew(this, e);
                this.move(coll);
            }
        });
        //this.velocity.set(EntityUtils.getReboundVelocity(velocity,this.boundingBox));
        if(EntityUtils.isInsideWall(this.boundingBox.expand(0.01,0.01))){
            this.health=-1;
        }

    }
}
