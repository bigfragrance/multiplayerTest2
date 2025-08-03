package modules.entity.player;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.AutoRecorder;
import engine.math.util.EntityUtils;
import engine.math.util.Util;
import modules.ctrl.ServerInputManager;
import modules.entity.Entity;
import modules.entity.MobEntity;
import modules.entity.PolygonEntity;
import modules.weapon.Gun;
import modules.weapon.Weapon;

import java.util.ArrayList;
import java.util.List;

import static engine.modules.EngineMain.cs;

public class AutoController {
    public static double followingRange=8;
    public static double stopFollowDistance=2;
    public static double changeDiff=0.8;
    public static int dodgeCheck=10;
    public ServerPlayerEntity owner;
    public ServerInputManager inputManager;
    private Entity target=null;
    private AutoRecorder<Vec2d> positionRecorder=new AutoRecorder<>(4);
    public AutoController(ServerPlayerEntity owner,ServerInputManager inputManager){
        this.owner=owner;
        this.inputManager=inputManager;
    }
    public void tick(){
        if(owner.weapon==null) return;
        updateTarget();
        updateAim();
        updateMovement();
        inputManager.upgradingSkill= Util.random.nextInt(10);
    }
    public void updateAim(){
        owner.rotation=owner.getRealVelocity().angle();
        inputManager.shoot=false;
        if(target==null) return;
        positionRecorder.add(target.position);
        if(positionRecorder.size()<3) return;
        Vec2d velocity=positionRecorder.getLast().subtract(positionRecorder.getFirst()).multiply(1d/(positionRecorder.size()-1));
        Gun gun=owner.weapon.getGoingToFire();
        if(gun==null) return;
        Vec2d realAim=target.getPos().add(target.getRealVelocity());
        if(owner.weapon.extradata.getBoolean("addVelocity")) {
            realAim = EntityUtils.extrapolate2(target.position, velocity, owner.position, gun.getBulletSpeed(gun.getBulletType()), owner.getRealVelocity());
            //realAim = EntityUtils.extrapolate2(aimPos, owner.getRealVelocity().multiply(-1), owner.position, gun.getBulletSpeed(), Vec2d.zero());
        }
        inputManager.aimPos=realAim.subtract(owner.position);
        inputManager.shoot=true;
        owner.rotation=inputManager.aimPos.angle();
    }
    public void updateMovement(){
        updateFollow();
        /*inputManager.side=0;
        inputManager.forward=0;*/
        updateDodge();
    }
    public void updateDodge(){
        List<Entity> willCollide=new ArrayList<>();
        for(Entity e:cs.entities.values()){
            if(e.team==owner.team) continue;
            if(e instanceof PolygonEntity&&e.damage<10) continue;
            if(e.damage>0&&willCollide(owner.boundingBox,e.boundingBox.expand(0.05),owner.velocity,e.velocity,dodgeCheck)){
                willCollide.add(e);
            }
        }
        if(willCollide.isEmpty()) return;
        int[] bestChoice=new int[2];
        int[][] movements=new int[][]{{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0},{0,0}};//idk use [9][2] or [2][9]
        Vec2d[] velocities=create(owner.velocity);
        Box[] boxes=create(owner.boundingBox);
        double[] damageTaken=create(0.0);

        double minDamage=10000000;
        for(int i=0;i<dodgeCheck;i++){
            int index=0;
            for(int x=-1;x<=1;x++){
                for(int y=-1;y<=1;y++){
                    movements[index][0]=x;
                    movements[index][1]=y;
                    Vec2d velocityAdd=new Vec2d(x,y).limit(owner.speed);
                    velocities[index].multiply1(ServerPlayerEntity.drag);
                    velocities[index].offset(velocityAdd);
                    boxes[index].offset1(velocities[index]);
                    for(Entity e:willCollide){
                        if(willCollide(boxes[index],e.boundingBox.expand(0.05).offset(e.velocity.multiply(i)),velocities[index],e.velocity,1)){
                            damageTaken[index]+=e.damage;
                        }
                    }
                    index++;
                }
            }
        }
        for(int i=0;i<9;i++){
            if(damageTaken[i]<minDamage){
                minDamage=damageTaken[i];
                bestChoice[0]=movements[i][0];
                bestChoice[1]=movements[i][1];
            }
        }
        inputManager.side=bestChoice[0];
        inputManager.forward=bestChoice[1];
    }
    private Vec2d[] create(Vec2d vel){
        Vec2d[] velocities=new Vec2d[10];
        for(int i=0;i<9;i++){
            velocities[i]=vel.copy();
        }
        return velocities;
    }
    private Box[] create(Box box){
        Box[] boxes=new Box[10];
        for(int i=0;i<9;i++){
            boxes[i]=box.copy();
        }
        return boxes;
    }
    private double[] create(double d){
        double[] ds=new double[10];
        for(int i=0;i<9;i++){
            ds[i]=d;
        }
        return ds;
    }
    private boolean willCollide(Box self,Box box,Vec2d selfVel,Vec2d vel,int times){
        for(double i=0;i<times;i+=0.2){
            Box after=box.offset(vel.multiply(i));
            Box selfAfter=self.offset(selfVel.multiply(i));
            if(selfAfter.intersects(after)){
                return true;
            }
        }
        return false;
    }
    public void updateFollow(){
        if(target==null) {
            Vec2d vec=owner.position.multiply(-1);
            if(vec.length()<3){
                vec=vec.rotate(90);
            }
            updateInput(vec);
            return;
        }
        Vec2d vec=target.getPos().subtract(owner.getPos());
        if(vec.length()<owner.weapon.getStopFollowDistance()){
            vec=vec.rotate(90);
        }
        updateInput(vec);
    }
    private void updateInput(Vec2d want){
        double rot=want.angle();
        double minDiff=23;
        for(int x=-1;x<=1;x++){
            for(int y=-1;y<=1;y++){
                if(x==0&&y==0) continue;
                Vec2d v=new Vec2d(x,y);
                double diff=v.angle()-rot;
                if(Math.abs(diff)<minDiff){
                    minDiff=diff;
                    inputManager.side=x;
                    inputManager.forward=y;
                }
            }
        }
    }
    public void updateTarget(){
        boolean bl=target==null;
        double minDistance=target==null?followingRange*owner.getFov():target.getPos().distanceTo(owner.getPos())-changeDiff;
        double minDistanceMob=minDistance;
        PlayerEntity player=null;
        Entity mob=null;
        for(Entity e:cs.entities.values()){
            if(e.team==owner.team) continue;
            if(e instanceof PlayerEntity||e instanceof MobEntity){
                boolean b=e instanceof PlayerEntity;
                if(b){
                    if(((PlayerEntity) e).name.equals("God")) continue;
                }
                double distance=owner.getPos().distanceTo(e.getPos());
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
            target=player;
        }
        else if(mob!=null){
            target=mob;
        }
        if(target==null||target.getPos().distanceTo(owner.getPos())>followingRange*owner.getFov()+changeDiff||!target.isAlive||target.killed()){
            target=null;
        }
        if(target!=null&&bl){
            positionRecorder.clear();
        }
    }

}
