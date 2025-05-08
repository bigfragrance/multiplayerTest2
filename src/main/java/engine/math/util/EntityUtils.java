package engine.math.util;

import engine.math.BlockPos2d;
import engine.math.Box;
import engine.math.Vec2d;
import engine.render.Screen;
import modules.entity.Entity;
import modules.entity.PlayerEntity;
import modules.entity.PolygonEntity;

import java.awt.*;
import java.util.function.Predicate;

import static engine.math.util.Util.round;
import static engine.modules.EngineMain.cs;
import static modules.entity.Entity.collisionMax;

public class EntityUtils {
    public static Color[] teamColors={
            new Color(255,100,100),
            new Color(100,100,255),
            new Color(100,255,100),
            new Color(255,255,100),
            new Color(255,100,255),
            new Color(100,255,255),
    };
    public static Color HealthBarColor=new Color(250,255,100,255);
    public static int nameSize=10;
    public static int scoreSize=7;
    public static double intersectCheckStep=0.1;
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
        for(double d=intersectCheckStep;d<=1;d+=intersectCheckStep){
            if(Util.lerp(pb1,b1,d).intersectsCircle(Util.lerp(pb2,b2,d))){
                return true;
            }
        }
        return false;
    }
    public static boolean intersectsCircle(Entity e1,Entity e2){
        return e1.boundingBox.intersects(e2.boundingBox)||EntityUtils.intersectsCircle(e1.prevBoundingBox,e1.boundingBox,e2.prevBoundingBox,e2.boundingBox);
    }
    public static Vec2d getPushVector(Entity e,Entity checking){
        Vec2d sub=e.position.subtract(checking.position);
        double subLength=sub.length();
        if(subLength<0.1) return new Vec2d(0,0);
        double length=Math.max(e.boundingBox.xSize(),e.boundingBox.ySize())+Math.max(checking.boundingBox.xSize(),checking.boundingBox.ySize());
        double mul=Entity.collisionVector*(length-subLength)/e.mass*checking.mass;
        if(mul<=0) return new Vec2d(0,0);
        return sub.limit(mul).limitOnlyOver(collisionMax);
    }
    public static Color getTeamColor(int team){
        if(team<0){
            return Color.PINK;
        }
        return teamColors[team%teamColors.length];
    }
    public static void render(Graphics g,Entity e){
        Color team=getTeamColor(e.team);
        if(e.isDamageTick){
            team=ColorUtils.brighter(team,0.5);
        }
        if(!e.isAlive){
            team=new Color(team.getRed(),team.getGreen(),team.getBlue(),50);
        }

        g.setColor(ColorUtils.darker(team,0.6));
        Util.render(g,Util.lerp(e.prevBoundingBox,e.boundingBox,e.tickDelta).switchToJFrame());
        g.setColor(team);
        Util.render(g,Util.lerp(e.prevBoundingBox,e.boundingBox,e.tickDelta).expand(-2,-2).switchToJFrame());

        if(e instanceof PlayerEntity){
            renderHealthBar(g,e,PlayerEntity.healthMax);
        }
        if(e instanceof PolygonEntity p){
            renderHealthBar(g,e,PolygonEntity.getHealthMax(p.sides,p.type));
        }
    }
    public static void renderHealthBar(Graphics g,Entity e,double maxHealth){
        if(e.health/maxHealth>0.95||e.health<=0) return;

        Box healthBar=new Box(e.getRenderBoundingBox().getCenter().add(0,-15),15,3);

        g.setColor(HealthBarColor);
        Vec2d a=healthBar.getMinPos();
        Vec2d b=healthBar.getMaxPos();
        double xDelta=b.x-a.x;
        Box healthBarPercentage=new Box(a,b.add(-xDelta*(1-e.getRenderHealth()/maxHealth),0));
        Util.renderCube(g,healthBarPercentage.switchToJFrame());

        g.setColor(Color.BLACK);
        Util.renderCubeLine(g,healthBar.switchToJFrame());
    }
    public static void renderPlayerName(Graphics g,PlayerEntity e){
        g.setColor(Color.DARK_GRAY);
        Vec2d renderPos=e.getRenderPosition().add(0,15);
        Util.renderString(g,e.name,renderPos.switchToJFrame(),round(nameSize*Screen.INSTANCE.zoom));
    }
    public static void renderScore(Graphics g,Entity e){
        g.setColor(Color.DARK_GRAY);
        Vec2d renderPos=e.getRenderPosition().add(0,30);
        Util.renderString(g, String.valueOf(round(e.score)),renderPos.switchToJFrame(),round(scoreSize*Screen.INSTANCE.zoom));
    }
    public static void renderSkillPoints(Vec2d pos,double[] skillPoints,int left){
        Screen.INSTANCE.renderAtLast(g->{
            g.setColor(Color.DARK_GRAY);

            Util.renderString(g,("Skill Points: "+left),pos,round(scoreSize*Screen.INSTANCE.zoom));
            pos.offset(0,-15);
            for(int i=0;i<skillPoints.length;i++){
                Util.renderString(g,PlayerEntity.skillNames[i]+": "+Util.getRoundedDouble(skillPoints[i],2),pos,round(scoreSize*Screen.INSTANCE.zoom));
                pos.offset(0,-15);
            }
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
    public static Vec2d getRandomSpawnPosition(){
        Vec2d farthest=Util.randomInBox(cs.borderBox);
        double farthestDistance=0;
        for(int i=0;i<10;i++){
            Vec2d pos=Util.randomInBox(cs.borderBox);;
            double distance=pos.length();
            if(distance>farthestDistance){
                farthest=pos;
                farthestDistance=distance;
            }
        }

        return farthest;
    }
    public static void updateCollision(Entity entity, Predicate<Entity> neverCheck,Predicate<Entity> check,AfterCheckTask<Entity> task){
        BlockPos2d pos=entity.getChunkPos();
        for(int x=-2;x<=2;x++){
            for(int y=-2;y<=2;y++){
                BlockPos2d pos2=pos.add(x,y);
                pos2=cs.chunkMap.blockPos(pos2);
                if(!cs.chunkMap.chunks.containsKey(pos2)) continue;
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
}
