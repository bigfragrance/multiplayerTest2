package big.game.screen;

import big.engine.math.Direction;
import big.engine.math.Vec2d;
import big.engine.math.Vec2i;
import big.engine.util.Util;
import big.events.RenderEvent;
import big.events.TickEvent;
import big.game.entity.Entity;
import big.game.entity.bullet.BulletEntity;
import big.game.world.BlockState;
import big.game.world.ClientWorld;
import meteordevelopment.orbit.EventHandler;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static big.engine.modules.EngineMain.cs;

public class DarkEffectScreen {
    public static String DARK_EFFECT="Dark";
    public static DarkEffectScreen INSTANCE;
    private Vec2d player;
    private HashSet<Vec2i> positions=new HashSet<>();
    private HashSet<Vec2i> nextPositions=null;
    public DarkEffectScreen(){
        cs.EVENT_BUS.subscribe(this);
    }
    public static void init(){
        INSTANCE=new DarkEffectScreen();
    }
    @EventHandler
    public void onTick(TickEvent event){
        if(!event.isPost()) return;
        Vec2d player=cs.player.getRenderPosition();
        HashSet<Vec2i> set=new HashSet<>();
        int r=((ClientWorld)cs.world).getRenderRange();
        for(double d=0;d<360;d+=0.5){
            Vec2d dir=new Vec2d(d).multiply(r);
            Util.HitResult result=Util.raycast(cs.world,player,player.add(dir));
            if(result.hit()){
                if(set.contains(result.blockPos())) continue;
                set.add(result.blockPos());
            }
        }
        nextPositions=set;
    }
    public void onRender(Graphics g){
        if(!EffectScreen.INSTANCE.effectList.containsKey(DARK_EFFECT)) return;

        player=cs.player.getRenderPosition();
        if(nextPositions!=null){
            positions=nextPositions;
            nextPositions=null;
        }
        for(Vec2i pos:positions){
            renderShadow(g,pos,positions);
        }
        /*for(Entity e:cs.world.getEntities()){
            if(e.equals(cs.player)) continue;
            if(e instanceof BulletEntity) continue;
            renderShadow(g,e.getRenderPosition(),player,e.getRenderBoundingBox().avgSize()/2);
        }*/

    }
    private void renderShadow(Graphics g,Vec2d pos,double r){
        Vec2d sub=pos.subtract(player);
        double angle=sub.angle();
        Vec2d lastPos=null;
        for(double d=angle-90;d<=angle+90;d+=30){
            Vec2d p=pos.add(new Vec2d(d).multiply(r));
            if(lastPos!=null){
                renderShadow(g,lastPos,p,player);
            }
            lastPos=p;
        }
    }
    private void renderShadow(Graphics g,Vec2i pos,HashSet<Vec2i> positions){
        g.setColor(new Color(0,0,0,255));
        Vec2d center=pos.toCenterPos();
        for(Direction dir:Direction.values()){
            if(positions.contains(pos.offset(dir))){
                continue;
            }
            Vec2d point=center.add(dir,0.5);
            Vec2d sub=point.subtract(player);
            double dot=sub.dot(dir.getOffset().toVec2d());
            if(dot>0){
                Vec2d point1=point.add(Direction.previous(dir),0.5);
                Vec2d point2=point.add(Direction.next(dir),0.5);
                renderShadow(g,point1,point2,player);
            }
        }
    }
    private void renderShadow(Graphics g,Vec2d p1,Vec2d p2,Vec2d player){
         Vec2d sub1=p1.subtract(player);
         Vec2d sub2=p2.subtract(player);
         Vec2d p3=p1.add(sub1.limit(20));
         Vec2d p4=p2.add(sub2.limit(20));
         Util.render(g,false,true,p1,p2,p4,p3);
    }
    private boolean isSolid(Vec2i pos){
        BlockState b=cs.world.getBlockState(pos);
        return b!=null&&b.getBlock().isSolid();
    }
}
