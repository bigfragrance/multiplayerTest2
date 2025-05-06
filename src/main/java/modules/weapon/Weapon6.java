package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Entity;

import static engine.modules.EngineMain.cs;

public class Weapon6 extends Weapon{
    private double cooldown;
    public Weapon6(Entity owner) {
        super(owner);
    }
    public void update(){
        cooldown=Math.max(cooldown-1,0);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= Screen.INSTANCE.inputManager.getMouseVec();
            Vec2d pos=owner.position;
            Vec2d velocity=input.limit(speed*3);
            double size=this.size*0.7;
            double health=this.health*0.5;
            double damage=this.damage*0.5;
            for(int i=0;i<30;i++){
                double r=Util.random(0.8,1.5);
                cs.networkHandler.sendBulletShoot(pos,velocity.multiply(Util.random(1,3)).add(Util.randomVec().limit(5)),size*r,health*r,damage*r);
            }

            cooldown=reload*3;
        }
    }
}
