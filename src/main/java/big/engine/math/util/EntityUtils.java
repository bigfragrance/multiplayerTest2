package big.engine.math.util;

import big.engine.math.*;
import big.modules.entity.Entity;
import big.modules.entity.bullet.BulletEntity;
import big.modules.entity.player.PlayerEntity;
import big.modules.entity.PolygonEntity;
import big.modules.world.BlockState;
import big.modules.world.Blocks;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static big.engine.math.util.Util.round;
import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.defZoom;
import static big.engine.render.Screen.sc;
import static big.modules.entity.Entity.collisionMax;
import static big.modules.entity.Entity.sizeMultiplier;

public class EntityUtils {
    public static Color[] teacsolors={
            new Color(255,100,100),
            new Color(100,100,255),
            new Color(100,255,100),
            new Color(255,255,100),
            new Color(255,100,255),
            new Color(100,255,255),
    };
    public static Color HealthBarColor=new Color(250,255,100,255);
    public static Color ShieldBarColor=new Color(100, 255, 255,255);
    public static int nameSize=10;
    public static int scoreSize=7;
    public static double intersectCheckStep=0.5;
    public static double extrapolateBase=1.5;
    public static double extrapolateCheckMax=10;
    public static double extrapolateCheckStep=0.5;
    public static boolean intersects(Box pb1,Box b1,Box pb2,Box b2) {
        if(pb1==null) pb1=b1;
        if(pb2==null) pb2=b2;
        if(b1==null||b2==null) return false;
        for(double d=intersectCheckStep;d<=1;d+=intersectCheckStep){
            if(Util.lerp(pb1,b1,d).intersects(Util.lerp(pb2,b2,d))){
                return true;
            }
        }
        return false;
    }
    public static boolean intersectsCircle(Box pb1,Box b1,Box pb2,Box b2) {
        if(pb1==null) pb1=b1;
        if(pb2==null) pb2=b2;
        if(b1==null||b2==null) return false;
        return ThickLineIntersectionNoCaps.isThickLineIntersect(pb1.getCenter(),b1.getCenter(),b1.avgSize()*0.5,pb2.getCenter(),b2.getCenter(),b2.avgSize()*0.5);
    }
    public static boolean intersectsCircle(Entity e1,Entity e2){
        return e1.boundingBox.intersects(e2.boundingBox)||EntityUtils.intersectsCircle(e1.prevBoundingBox,e1.boundingBox,e2.prevBoundingBox,e2.boundingBox);
    }
    public static Vec2d getPushVector(Entity e,Entity checking){

        Vec2d sub=e.position.subtract(checking.position);
        double subLength=sub.length();
        if(subLength<0.0000001) return new Vec2d(0,0);
        double length=Math.max(e.boundingBox.xSize(),e.boundingBox.ySize())+Math.max(checking.boundingBox.xSize(),checking.boundingBox.ySize());
        double mul=Entity.collisionVector*(length-subLength)/e.mass*checking.mass;
        if(mul<=0) return new Vec2d(0,0);
        return sub.limit(mul).limitOnlyOver(collisionMax);
    }
    public static Color getTeamcolor(int team){
        if(team<0){
            return Color.PINK;
        }
        return teacsolors[team%teacsolors.length];
    }
    public static void render(Graphics g,Entity e){
        render(g,e,true);
    }
    public static void render(Graphics g,Entity e,boolean healthBar){
        Color team=ColorUtils.setAlpha(getTeamcolor(e.team),e.getRenderAlpha());
        if(e.isDamageTick){
            team=ColorUtils.brighter(team,0.5);
        }
        if(!e.isAlive){
            team=new Color(team.getRed(),team.getGreen(),team.getBlue(),50);
        }
        if(e.team>=0) {
            g.setColor(ColorUtils.darker(team, 0.6));
            Util.render(g, Util.lerp(e.prevBoundingBox, e.boundingBox, e.getTickDelta()).switchToJFrame());
            g.setColor(team);
            Util.render(g, smaller(Util.lerp(e.prevBoundingBox, e.boundingBox, e.getTickDelta())).switchToJFrame());
        }else{
            g.setColor(team);
            Util.renderPolygon(g, Util.lerp(e.prevBoundingBox, e.boundingBox, e.getTickDelta()).getCenter(),3,e.boundingBox.avgSize()*0.5,e.getRenderRotation(),false,true);
            g.setColor(ColorUtils.darker(team, 0.6));
            Util.renderPolygon(g, Util.lerp(e.prevBoundingBox, e.boundingBox, e.getTickDelta()).getCenter(),3,e.boundingBox.avgSize()*0.5,e.getRenderRotation(),true,false);
        }
        if(!healthBar) return;
        if(e instanceof PlayerEntity){
            renderHealthBar(g,e,PlayerEntity.healthMax);
            renderShieldBar(g,e,PlayerEntity.shieldMax);
        }
        if(e instanceof PolygonEntity p){
            renderHealthBar(g,e,PolygonEntity.getHealthMax(p.sides,p.type));
        }
    }
    public static Box smaller(Box box){
        double e=Math.min(0.05,(box.xSize()+box.ySize())/4*0.1);
        return box.expand(-e,-e);
    }
    public static Box smallerBullet(Box box){
        double e=Math.min(0.05,(box.xSize()+box.ySize())/4*0.4);
        return box.expand(-e,-e);
    }
    public static void render(Graphics g,Box box,Color team){
        g.setColor(ColorUtils.darker(team,0.6));
        Util.render(g,box.switchToJFrame());
        g.setColor(team);
        Util.render(g,box.switchToJFrame());
    }
    public static void renderBullet(Graphics g, BulletEntity e,double bright){
        Color team=ColorUtils.setAlpha(getTeamcolor(e.team), bright);
        if(e.isDamageTick){
            team=ColorUtils.brighter(team,0.5);
        }
        if(!e.isAlive){
            team=new Color(team.getRed(),team.getGreen(),team.getBlue(),50);
        }
        if(e.type.type==0) {
            g.setColor(ColorUtils.darker(team, 0.6));
            Util.render(g, Util.lerp(e.prevBoundingBox, e.boundingBox, e.getTickDelta()).switchToJFrame());
            g.setColor(team);
            Util.render(g,smallerBullet (Util.lerp(e.prevBoundingBox, e.boundingBox, e.getTickDelta())).switchToJFrame());
        }else{
            g.setColor(ColorUtils.darker(team, 0.6));
            Util.renderPolygon(g,e.getRenderPosition(),e.type.type+2,e.boundingBox.avgSize()/2,e.getRenderRotation(),true,true,e.type.sharp,e.type.sharpFactor);
            g.setColor(team);
            Util.renderPolygon(g,e.getRenderPosition(),e.type.type+2,e.boundingBox.avgSize()/2*0.95,e.getRenderRotation(),true,true,e.type.sharp,e.type.sharpFactor);
        }
    }
    public static void renderBullet(Graphics g, BulletEntity e){
        renderBullet(g,e,e.getRenderAlpha());
    }
    public static void renderHealthBar(Graphics g,Entity e,double maxHealth){
        if(e.health/maxHealth>0.95||e.health<=0) return;
        double d=sizeMultiplier*e.getSizeMultiplier();
        Box healthBar=new Box(e.getRenderBoundingBox().getCenter().add(0,-15*d),15*d,3*d);

        g.setColor(HealthBarColor);
        Vec2d a=healthBar.getMinPos();
        Vec2d b=healthBar.getMaxPos();
        double xDelta=b.x-a.x;
        Box healthBarPercentage=new Box(a,b.add(-xDelta*(1-e.getRenderHealth()/maxHealth),0));
        Util.renderCube(g,healthBarPercentage.switchToJFrame());

        g.setColor(Color.BLACK);
        Util.renderCubeLine(g,healthBar.switchToJFrame());
    }
    public static void renderShieldBar(Graphics g,Entity e,double shieldMax){
        if(e.shield/shieldMax>0.95||e.shield<=0) return;
        double d=sizeMultiplier*e.getSizeMultiplier();
        Box healthBar=new Box(e.getRenderBoundingBox().getCenter().add(0,-23*d),15*d,3*d);

        g.setColor(ShieldBarColor);
        Vec2d a=healthBar.getMinPos();
        Vec2d b=healthBar.getMaxPos();
        double xDelta=b.x-a.x;
        Box shieldBarPercentage=new Box(a,b.add(-xDelta*(1-e.getRenderShield()/shieldMax),0));
        Util.renderCube(g,shieldBarPercentage.switchToJFrame());

        g.setColor(Color.BLACK);
        Util.renderCubeLine(g,healthBar.switchToJFrame());
    }
    public static void renderPlayerName(Graphics g,PlayerEntity e){
        g.setColor(ColorUtils.setAlpha(Color.DARK_GRAY,e.getRenderAlpha()));
        Vec2d renderPos=e.getRenderPosition().add(0,30*sizeMultiplier*e.getSizeMultiplier());
        Util.renderString(g,e.name,renderPos.switchToJFrame(),round(nameSize* sc.zoom*sizeMultiplier*e.getSizeMultiplier()));
    }
    public static void renderScore(Graphics g,Entity e){
        g.setColor(ColorUtils.setAlpha(Color.DARK_GRAY,e.getRenderAlpha()));
        Vec2d renderPos=e.getRenderPosition().add(0,40*sizeMultiplier*e.getSizeMultiplier());
        Util.renderString(g, String.valueOf(round(e.score)),renderPos.switchToJFrame(),round(scoreSize* sc.zoom*sizeMultiplier*e.getSizeMultiplier()));
    }
    public static void renderSkillPoints(Vec2d pos,double[] skillPoints,int left){
        sc.renderAtLast(g->{
            sc.storeAndSetDef();
            g.setColor(Color.DARK_GRAY);

            Util.renderString(g,("Skill Points: "+left),pos,round(scoreSize* sc.zoom*sizeMultiplier));
            pos.offset(0,-15/sc.zoom2);
            for(int i=0;i<skillPoints.length;i++){
                Util.renderString(g,PlayerEntity.skillNames[i]+": "+Util.formatDouble(skillPoints[i]),pos,round(scoreSize* sc.zoom*sizeMultiplier));
                pos.offset(0,-15/sc.zoom2);
            }
            sc.restoreZoom();
        });
        //Util.renderString(g,getSkillPointsString(skillPoints,left),pos,round(scoreSize*Screen.INSTANCE.zoom));
    }
    public static String getSkillPointsString(double[] skillPoints,int left){
        StringBuilder sb=new StringBuilder();
        sb.append("Skill Points: "+left+"\n");
        for(int i=0;i<skillPoints.length;i++){
            sb.append(PlayerEntity.skillNames[i]+": "+skillPoints[i]+"\n");
        }
        return sb.toString();
    }
    public static Vec2d getRandomSpawnPosition(int team){
        Vec2d farthest=Util.randomInBox(cs.borderBox);
        double farthestDistance=0;
        for(int i=0;i<500;i++){
            Vec2d pos=Util.randomInBox(cs.borderBox);
            if(cs.world.getBlockState(BlockPos.ofFloor(pos)).getTeam()==team){
                return pos;
            }
            if(EntityUtils.isInsideWall(new Box(pos,0.19,0.19))){
                continue;
            }
            double distance=pos.length();
            if(distance>farthestDistance){
                farthest=pos;
                farthestDistance=distance;
            }
        }

        return farthest;
    }
    public static Vec2d getVisitorSpawnPosition(){
        Vec2d closest=Util.randomInBox(cs.borderBox);
        double closestDistance=100000;
        for(int i=0;i<700;i++){
            Vec2d pos=Util.randomInBox(cs.borderBox);
            if(EntityUtils.isInsideWall(new Box(pos,1.5,1.5))){
                continue;
            }
            BlockState state=cs.world.getBlockState(BlockPos.ofFloor(pos));
            if(state.getTeam()<0&&state.getBlock()== Blocks.BASE_BLOCK){
                return pos;
            }
        }
        for(int i=0;i<7;i++){
            Vec2d pos=Util.randomInBox(cs.borderBox);
            if(cs.world.getBlockState(BlockPos.ofFloor(pos)).getTeam()>=0){
                continue;
            }
            if(EntityUtils.isInsideWall(new Box(pos,1.5,1.5))){
                continue;
            }
            double distance=pos.length();
            if(distance<closestDistance){
                closest=pos;
                closestDistance=distance;
            }
        }

        return closest;
    }
    public static double[] getBetterDamage(double thisHealth,double thisDamage,double othersHealth,double othersDamage){
        //[0] to enemy, [1] to self
        double d1=Math.clamp(othersHealth/thisDamage,0,0.5);
        double d2=Math.clamp(thisHealth/othersDamage,0,0.5);
        return new double[]{d2*thisDamage,d1*othersDamage};
    }
    public static void takeDamage(Entity e1,Entity e2){
        double[] d=getBetterDamage(e1.health,e1.damage,e2.health,e2.damage);
        e2.addDamage(d[0]);
        e2.storeDamage(e1,d[0]);
        e1.addDamage(d[1]);
        e1.storeDamage(e2,d[1]);
    }
    public static void updateCollision(Entity entity, Predicate<Entity> neverCheck,Predicate<Entity> check,AfterCheckTask<Entity> task){
        updateCollision(entity,neverCheck,check,task,1);
    }
    public static void updateCollision(Entity entity, Predicate<Entity> neverCheck,Predicate<Entity> check,AfterCheckTask<Entity> task,int r){
        BlockPos pos=entity.getChunkPos();
        for(int x=-r;x<=r;x++){
            for(int y=-r;y<=r;y++){
                BlockPos pos2=pos.add(x,y);
                if(!cs.chunkMap.chunks.containsKey(pos2.toLong())) continue;
                for(Entity e:cs.chunkMap.getChunk(pos2)){
                    if(e.id==entity.id) continue;
                    if(neverCheck.test(e)) continue;
                    if(check.test(e)){
                        task.run(e);
                    }
                }
            }
        }
    }
    public static Vec2d getKnockBackVector(Entity self,Entity other,double f){
        f=Math.min(f,1);
        double d=self.velocity.dot(other.velocity.limit(1));
        double d2=other.velocity.multiply(f).length();
        if(d>d2) return new Vec2d(0,0);
        return other.velocity.limit(d2-d);
    }
    public static Vec2d getMaxMove(Box b,Vec2d v){
        if(v.length()<=0.001) return new Vec2d(0,0);
        Box b2=b.stretch(v.x,v.y).expand(0.1,0.1);
        //if(!isInsideWall(b2)) return v;
        for(CheckContent c:getCheckBoxes(b2)){
            v.set(getMaxMoveOld(b,v,c.box,c.left,c.right,c.top,c.bottom));
        }
        return v;
    }
    public static Vec2d getMaxMoveNoStuck(Box b,Vec2d v){
        Vec2d x=new Vec2d(v.x,0);
        Vec2d y=new Vec2d(0,v.y);
        Vec2d x2=getMaxMove(b,x);
        Vec2d y2=getMaxMove(b,y);
        Box after=b.offset(x2.x,y2.y);
        if(EntityUtils.isInsideWall(after.expand(-0.01))){
            return getMaxMove(b,v);
        }
        return new Vec2d(x2.x,y2.y);
    }
    public static Vec2d getMaxMoveCircle(Box box,Vec2d vec){
        List<Box> boxes=createCircleBox(box);
        Vec2d maxMove=vec.copy();
        for(Box b2:boxes){
            maxMove.set(getMaxMoveNoStuck(b2,maxMove));
        }
        return maxMove;
    }
    public static List<Box> createCircleBox(Box box){
        Vec2d center=box.getCenter();
        Vec2d vec=new Vec2d(box.avgSize()/2,0);
        List<Box> boxes=new ArrayList<>();
        for(int i=0;i<360;i+=1){
            boxes.add(new Box(center.add(vec.rotate(i)),0.02,0.02));
        }
        return boxes;
    }
    public static List<CheckContent> getCheckBoxes(Box b){
        List<CheckContent> boxes=new ArrayList<>();
        for(int x=Util.floor(b.minX);x<Util.ceil(b.maxX);x++){
            for(int y=Util.floor(b.minY);y<Util.ceil(b.maxY);y++){
                BlockPos pos= new BlockPos(x,y);
                if(cs.world.getBlockState(pos).getBlock().solid)boxes.add(new CheckContent(new Box(x,y),sl(pos),sr(pos),st(pos),sb(pos)));
            }
        }

        /*if(cs.world.getBlockState(pos).getBlock().solid)boxes.add(new CheckContent(new Box(pos),sl(pos),sr(pos),st(pos),sb(pos)));
        pos= BlockPos.ofFloor(b.maxX,b.minY);
        if(cs.world.getBlockState(pos).getBlock().solid)boxes.add(new CheckContent(new Box(pos),sl(pos),sr(pos),st(pos),sb(pos)));
        pos= BlockPos.ofFloor(b.minX,b.maxY);
        if(cs.world.getBlockState(pos).getBlock().solid)boxes.add(new CheckContent(new Box(pos),sl(pos),sr(pos),st(pos),sb(pos)));
        pos= BlockPos.ofFloor(b.maxX,b.maxY);
        if(cs.world.getBlockState(pos).getBlock().solid)boxes.add(new CheckContent(new Box(pos),sl(pos),sr(pos),st(pos),sb(pos)));*/
        return boxes;
    }
    public static boolean sl(BlockPos pos){
        return true||!cs.world.getBlockState(pos.add(-1,0)).getBlock().solid;
    }
    public static boolean sr(BlockPos pos){
        return true||!cs.world.getBlockState(pos.add(1,0)).getBlock().solid;
    }
    public static boolean st(BlockPos pos){
        return  true||!cs.world.getBlockState(pos.add(0,1)).getBlock().solid;
    }
    public static boolean sb(BlockPos pos){
        return true||!cs.world.getBlockState(pos.add(0,-1)).getBlock().solid;
    }
    public static Vec2d getMaxMoveOld(Box box, Vec2d velocity, Box checking,
                                   boolean checkLeft, boolean checkRight,
                                   boolean checkTop, boolean checkBottom) {

        if (velocity.x == 0 && velocity.y == 0) {
            return new Vec2d(0, 0);
        }


        //Box predictedBox = box.offset(velocity);


        /*if (!predictedBox.intersects(checking)) {
            return velocity;
        }*/


        double maxX = velocity.x;
        double maxY = velocity.y;


        if (velocity.x != 0) {
            boolean shouldCheck = (velocity.x > 0 && checkRight) || (velocity.x < 0 && checkLeft);
            if (shouldCheck) {
                Box xOnlyBox = box.offset(new Vec2d(velocity.x, 0));
                if (xOnlyBox.intersects(checking)) {
                    if (velocity.x > 0) {

                        maxX = checking.minX - box.maxX;
                        if (maxX < 0) maxX = velocity.x;
                    } else {

                        maxX = checking.maxX - box.minX;
                        if (maxX > 0) maxX = velocity.x;
                    }
                }
            }
        }


        if (velocity.y != 0) {
            boolean shouldCheck = (velocity.y > 0 && checkTop) || (velocity.y < 0 && checkBottom);
            if (shouldCheck) {
                Box yOnlyBox = box.offset(new Vec2d(0, velocity.y));
                if (yOnlyBox.intersects(checking)) {
                    if (velocity.y > 0) {

                        maxY = checking.minY - box.maxY;
                        if (maxY < 0) maxY = velocity.y;
                    } else {

                        maxY = checking.maxY - box.minY;
                        if (maxY > 0) maxY = velocity.y;
                    }
                }
            }
        }


        Box candidateBox = box.offset(new Vec2d(maxX, maxY));


        if (candidateBox.intersects(checking)) {

            Box xAdjustedBox = box.offset(new Vec2d(maxX, 0));
            boolean xCollision = xAdjustedBox.intersects(checking);


            Box yAdjustedBox = box.offset(new Vec2d(0, maxY));
            boolean yCollision = yAdjustedBox.intersects(checking);


            if (!xCollision) {

                return new Vec2d(maxX, 0);
            } else if (!yCollision) {

                return new Vec2d(0, maxY);
            } else {

                if (Math.abs(maxX) < Math.abs(maxY)) {
                    return new Vec2d(maxX, 0);
                } else {
                    return new Vec2d(0, maxY);
                }
            }
        }


        return new Vec2d(maxX, maxY);
    }
    public static Vec2d getMaxMove(Box box, Vec2d velocity, Box checking,
                                   boolean checkLeft, boolean checkRight,
                                   boolean checkTop, boolean checkBottom){
        //if(!box.offset(velocity).intersects(checking)) return velocity;
        double x=velocity.x;
        double y=velocity.y;

        Box xOnly=box.offset(x,0);
        Box yOnly=box.offset(0,y);
        if(xOnly.intersects(checking)){
            if(x>0&&checkLeft){
                x=checking.minX-box.maxX;
            }
            if(x<0&&checkRight){
                x=checking.maxX-box.minX;
            }
        }
        if(yOnly.intersects(checking)){
            if(y>0&&checkBottom){
                y=checking.minY-box.maxY;
            }
            if(y<0&&checkTop){
                y=checking.maxY-box.minY;
            }
        }
        Box xAndY=box.offset(x,y);
        if(xAndY.intersects(checking)){
            if(Math.abs(x)>Math.abs(y)){
                y=0;
            }else{
                x=0;
            }
        }
        return new Vec2d(x,y);

        /*if(checkRight){
            SegmentY movingSeg=box.getLeftSegment();
            SegmentY checkingSeg=checking.getRightSegment();
            x=getMaxMoveX(movingSeg,checkingSeg,velocity);
        }
        if(checkLeft){
            SegmentY movingSeg=box.getRightSegment();
            SegmentY checkingSeg=checking.getLeftSegment();
            x=getMaxMoveX(movingSeg,checkingSeg,velocity);
        }
        if(checkBottom){
            SegmentX movingSeg=box.getTopSegment();
            SegmentX checkingSeg=checking.getBottomSegment();
            y=getMaxMoveY(movingSeg,checkingSeg,velocity);
        }
        if(checkTop){
            SegmentX movingSeg=box.getBottomSegment();
            SegmentX checkingSeg=checking.getTopSegment();
            y=getMaxMoveY(movingSeg,checkingSeg,velocity);
        }*/
        //return new Vec2d(x,y);
    }
    public static double getMaxMoveY(SegmentX moving,SegmentX checking,Vec2d velocity){
        double diff=checking.y-moving.y;
        if((diff<velocity.y&&velocity.y>0)||(diff>velocity.y&&velocity.y<0)) {
            if (moving.interact(checking.minX,checking.maxX)) {
                return diff;
            }
        }
        return velocity.y;
    }
    public static double getMaxMoveX(SegmentY moving, SegmentY checking, Vec2d velocity){
        double diff=checking.x-moving.x;
        if((diff<velocity.x&&velocity.x>0)||(diff>velocity.x&&velocity.x<0)) {
            if (moving.interact(checking.minY, checking.maxY)) {
                return diff;
            }
        }
        return velocity.x;
    }
    public static Vec2d getReboundVelocity(Vec2d o,Box box){
        Vec2d maxMove=getMaxMove(box,o);
        box=box.offset(maxMove);
        if(!isInsideWall(box.expand(0.001,0.001))) return o;
        Box x=box.offset(o.x,0);
        Box y=box.offset(0,o.y);
        double newX=o.x;
        double newY=o.y;
        if(isInsideWall(x)){
            newX=-newX;
        }
        if(isInsideWall(y)){
            newY=-newY;
        }
        return new Vec2d(newX,newY);
    }
    private static final double S=0.001;
    public static boolean isInsideWall(Box box){
        return cs.serverController.isWall(box.minX+S,box.minY+S)||cs.serverController.isWall(box.maxX,box.minY+S)||cs.serverController.isWall(box.minX+S,box.maxY)||cs.serverController.isWall(box.maxX,box.maxY);
    }
    public static Vec2d extrapolate(Vec2d targetPos,Vec2d targetVel,double tick,Vec2d addVel){
        return targetPos.add(targetVel.add(addVel).multiply(tick));
    }
    /*public Vec2d extrapolate(Entity e,Vec2d shootPos, double bulletSpeed){
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
    }*/
    public static Vec2d extrapolate2(Vec2d targetPos,Vec2d targetVel,Vec2d shootPos, double bulletSpeed,Vec2d addVel){
        Vec2d bestPos=null;
        double minDiff=1000;
        if(addVel!=null)addVel=addVel.multiply(-1);
        for(double i=0;i<extrapolateCheckMax;i+=extrapolateCheckStep){
            Vec2d pos=extrapolate(targetPos,targetVel,i,addVel);
            double dist=pos.subtract(shootPos).length();
            double diff=Math.abs(i-dist/bulletSpeed);
            if(diff<minDiff){
                minDiff=diff;
                bestPos=pos;
            }
        }
        return bestPos==null?targetPos:bestPos;
    }
}
