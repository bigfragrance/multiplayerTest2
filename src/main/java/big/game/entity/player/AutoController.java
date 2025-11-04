package big.game.entity.player;

import big.engine.math.Vec2i;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.util.AutoRecorder;
import big.engine.util.EntityUtils;
import big.engine.util.Util;
import big.engine.util.pathing.Calculator;
import big.engine.util.pathing.Path;
import big.engine.util.pathing.PathNode;
import big.game.ctrl.ServerInputManager;
import big.game.entity.Controllable;
import big.game.entity.Entity;
import big.game.entity.MobEntity;
import big.game.entity.PolygonEntity;
import big.game.weapon.Gun;
import big.game.world.BlockState;
import big.game.world.Blocks;
import big.game.world.blocks.BaseBlock;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class AutoController<T extends Entity&Controllable> {
    public static double followingRange=8;
    public static double stopFollowDistance=2;
    public static double changeDiff=0.8;
    public static int dodgeCheck=10;
    public T owner;
    public ServerInputManager inputManager;
    private Entity target=null;
    private AutoRecorder<Vec2d> positionRecorder=new AutoRecorder<>(4);
    public boolean rotationSet=true;
    public boolean dodge=true;
    private boolean attack=false;
    public Calculator calculator=null;
    private Path lastPath=null;
    public AutoController(T owner,ServerInputManager inputManager){
        this.owner=owner;
        this.inputManager=inputManager;
    }
    public void tick(){
        if(owner.getWeapon()==null) return;
        updateTarget();
        updateAim();
        updateMovement();
        inputManager.upgradingSkill= Util.random.nextInt(10);
    }
    public void updateAim(){
        owner.setRotation(owner.getRealVelocity().angle());
        inputManager.shoot=false;
        if(target==null) return;
        positionRecorder.add(target.position);
        if(positionRecorder.size()<3) return;
        Vec2d velocity=positionRecorder.getLast().subtract(positionRecorder.getFirst()).multiply(1d/(positionRecorder.size()-1));
        Gun gun=owner.getWeapon().getGoingToFire();
        if(gun==null) {
            inputManager.shoot=attack;
            return;
        }
        Vec2d realAim=target.getPos().add(target.getRealVelocity());
        if(owner.getWeapon().extradata.getBoolean("addVelocity")) {
            realAim = EntityUtils.extrapolate2(target.position, velocity.subtract(owner.getRealVelocity()), owner.getPosition(), gun.getBulletSpeed(gun.getBulletType()),null);
            //realAim = EntityUtils.extrapolate2(aimPos, owner.getRealVelocity().multiply(-1), owner.position, gun.getBulletSpeed(), Vec2d.zero());
        }
        inputManager.aimPos=realAim.subtract(owner.getPosition());
        inputManager.shoot=attack;
        if(rotationSet) owner.setRotation(inputManager.aimPos.angle());
    }
    public void updateMovement(){
        updateFollow();
        /*inputManager.side=0;
        inputManager.forward=0;*/
        //if(dodge)updateDodge();
        awayFromOthersBase();
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
                    Vec2d velocityAdd=new Vec2d(x,y).limit(owner.getSpeed());
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
        }if(vec.length()<owner.weapon.getStopFollowDistance()-0.5){
            vec=vec.rotate(90);
        }
        Vec2d v=getPathPos(vec.add(owner.getPos()));
        updateInput(v==null?vec:v.subtract(owner.getPos()));
    }
    private Vec2d getPathPos(Vec2d target){
        if(calculator==null) calculator=new Calculator(owner);
        Path p=calculator.getPath(Vec2i.ofFloor(owner.position.multiply(2)).toCenterPos().multiply(0.5),target);
        if(p==null) return null;
        if(lastPath!=null&&lastPath.isStillValid()){
            Vec2d lastEnd=lastPath.getLast();
            Vec2d newEnd=p.getLast();
            Path new2=calculator.getPath(newEnd,target);
            Path last2=calculator.getPath(lastEnd,target);
            double lastScore=last2==null?1000000000:last2.getLast().distanceTo(target);
            double newScore=new2==null?1000000000:new2.getLast().distanceTo(target);
            if(newScore>lastScore){
                p=lastPath;
            }
        }
        p.update();
        lastPath=p;
        Vec2d last=null;
        for(PathNode node:p.path){
            if(last!=null){
                Vec2d finalLast = last.switchToJFrame();
                sc.renderTasks2.add((t)->{
                    t.setColor(Color.RED);
                    Util.renderLine(t, finalLast,node.pos.switchToJFrame());
                });
            }
            last=node.pos;
        }
        return p.getMoveToNow();
    }
    public void awayFromOthersBase(){
        Vec2i pos=owner.getPos().ofFloor();
        Vec2i minDistPos=null;
        double minDist=10000000;
        for(int x=-3;x<=3;x++){
            for(int y=-3;y<=3;y++){
                if(x==0&&y==0) continue;
                Vec2i p=pos.add(x,y);
                BlockState state=cs.world.getBlockState(p);
                if(state.getTeam()!=owner.team&&state.getBlock()== Blocks.BASE_BLOCK&& BaseBlock.shouldDealDamage(state)){
                    double dist=p.distanceTo(pos);
                    if(dist<minDist){
                        minDist=dist;
                        minDistPos=p;
                    }
                }
            }
        }
        if(minDistPos!=null){
            Vec2d vec=owner.getPosition().subtract(minDistPos.toCenterPos());
            updateInput(vec);
        }
    }
    private void updateInput(Vec2d want){
        double rot=want.angle();
        double minDiff=25;
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
        double attackDist=followingRange*owner.getFov();
        double minDistance=target==null?1000000000:target.getPos().distanceTo(owner.getPos())-changeDiff;
        double minDistanceMob=attackDist;
        PlayerEntity player=null;
        Entity mob=null;
        for(Entity e:cs.entities.values()){
            if(e.team==owner.team) continue;
            if(!e.isAlive) continue;
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
        if(target==null||(target instanceof MobEntity&&target.getPos().distanceTo(owner.getPos())>followingRange*owner.getFov()+changeDiff)||!target.isAlive||target.killed()){
            target=null;
        }
        if(target!=null&&bl){
            positionRecorder.clear();
        }
        if(target!=null){
            attack=minDistance<attackDist+2;
        }
    }

}
