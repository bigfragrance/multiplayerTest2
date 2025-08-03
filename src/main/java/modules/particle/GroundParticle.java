package modules.particle;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;

import java.awt.*;

import static engine.render.Screen.SCREEN_BOX;

public class GroundParticle extends Particle{
    public static double size=0.1;
    long startTime;
    public GroundParticle(Vec2d position) {
        super(position,new Vec2d(0,0),new Box(position,size,size));
        this.startTime=System.currentTimeMillis();
    }
    public void update(){
        super.update();
        if(shouldKill(false)){
            kill();
        }
    }
    public boolean shouldDelete(){
        return !this.boundingBox.intersects(SCREEN_BOX);
    }
    public void render(Graphics g){
        super.render(g);
        g.setColor(new Color(0,0,0,20));
        Util.renderCube(g,new Box(Util.lerp(prevPosition,position,getTickDelta()),size,size).switchToJFrame());
    }
    public boolean shouldKill(boolean ignoreTime){
        if(!ignoreTime&&System.currentTimeMillis()-startTime<2000) return false;
        Vec2d pos=this.position.switchToJFrame(Screen.sc.getRealZoom());
        return pos.x < 0 - size || pos.y < 0 - size || pos.x > Screen.sc.windowWidth + size || pos.y > Screen.sc.windowHeight + size;
    }
}
