package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.modules.entity.Entity;
import big.modules.entity.MobEntity;
import big.modules.entity.player.AutoController;
import big.modules.entity.player.PlayerEntity;

import static big.engine.modules.EngineMain.cs;

public class AutoAim<T extends AbleToAim> {
    public static double changeDiff=2;
    public static double defRange=8;
    private T owner;
    private double fov;
    private Entity target;
    public double seeRangeMultiplier=1;
    public AutoAim(T owner,double fov){
        this.owner=owner;
        this.fov=fov;
    }
    public void tick(){
        updateTarget();
        updateAim();
    }
    public void updateAim(){
        owner.setFire(false);
        owner.setTarget(new Vec2d(owner.getRotation()).multiply(2).add(owner.getPos()));
        if(target==null) return;
        Vec2d realAim = EntityUtils.extrapolate2(target.position,target.getRealVelocity(), owner.getPos(),owner.getBulletSpeed(), owner.getRealVelocity());
        owner.setTarget(realAim);
        owner.setFire(true);
    }
    public void updateTarget(){
        double minDistance=target==null?defRange*seeRangeMultiplier:target.getPos().distanceTo(owner.getPos())-changeDiff;
        double minDistanceMob=minDistance;
        PlayerEntity player=null;
        Entity mob=null;
        for(Entity e:cs.entities.values()){
            if(e.team==owner.getTeam()) continue;
            if(e instanceof PlayerEntity||e instanceof MobEntity){
                boolean b=e instanceof PlayerEntity;
                if(b){
                    if(((PlayerEntity) e).name.equals("God")) continue;
                }
                if(!inFov(e)){
                    continue;
                }
                double distance=owner.getPos().distanceTo(e.getPos());
                if(b?distance<minDistance:distance<minDistanceMob){
                    if(b){
                        minDistance=distance;
                        player=(PlayerEntity) e;
                    }
                    else{
                        minDistanceMob = distance;
                        mob = e;
                    }
                }
            }
        }
        if(mob!=null){
            target=mob;
        }
        if(player!=null){
            target=player;
        }
        if(target==null||target.getPos().distanceTo(owner.getPos())>defRange*seeRangeMultiplier+changeDiff||!target.isAlive||target.killed()||!inFov(target)){
            target=null;
        }
    }
    public boolean inFov(Entity target){
        double a1=target.getPos().subtract(owner.getPos()).angle();
        double a2=owner.getRotation();
        if(a1<0) a1+=360;
        if(a2<0) a2+=360;
        return Math.abs(a1-a2)%360<=fov/2;
    }
}
