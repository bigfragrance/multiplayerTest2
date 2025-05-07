package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Entity;

import static engine.math.util.Util.round;
import static engine.modules.EngineMain.cs;

public class Weapon0 extends Weapon{
    private double cooldown=0;
    public Weapon0(Entity owner) {
        super(owner);
    }
    public void update(){
        cooldown=Math.max(cooldown-1,0);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= Screen.INSTANCE.inputManager.getMouseVec();
            Vec2d pos=owner.position;
            Vec2d velocity=input.limit(speed*3).add(Util.randomVec().limit(0.5));
            double size=this.size*1.2;
            double health=this.health*2;
            double damage=this.damage*1.5;
            cs.networkHandler.sendBulletShoot(pos,velocity,size,health,damage);
            cooldown= reload*0.5;
        }
    }
}
