package big.modules.entity.bullet;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.modules.entity.Entity;

import java.awt.*;

public class EntityParticle extends Entity {
    public static double expandFactor=0.3;
    public static int MAX_LIFETIME=6;
    private int maxLifeTime;
    private int lifeTime;
    public EntityParticle() {
        this.maxLifeTime=MAX_LIFETIME;
        lifeTime=0;
    }
    public void tick(){
        super.tick();
        if(lifeTime>maxLifeTime){
            kill();
        }
        Box b=this.boundingBox;
        this.boundingBox=b.expand(b.xSize()*expandFactor,b.ySize()*expandFactor);
    }
    public void render(Graphics g){
        //EntityUtils.renderBullet(g,this,(maxLifeTime-lifeTime)/(double)maxLifeTime);
    }
}
