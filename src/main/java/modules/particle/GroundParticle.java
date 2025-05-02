package modules.particle;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.Util;

import java.awt.*;

import static engine.render.Screen.SCREEN_BOX;
import static engine.render.Screen.tickDelta;

public class GroundParticle extends Particle{
    public static double size=5;
    public GroundParticle(Vec2d position) {
        super(position,new Vec2d(0,0),new Box(position,size,size));
    }
    public void update(){
        super.update();
    }
    public boolean shouldDelete(){
        return !this.boundingBox.intersects(SCREEN_BOX);
    }
    public void render(Graphics g){
        g.setColor(new Color(100,100,100,50));
        Util.render(g,new Box(Util.lerp(prevPosition,position,tickDelta),size,size).switchToJFrame());
    }
}
