package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Entity;

import static engine.modules.EngineMain.cs;

public class Weapon3 extends Weapon{
    private double cooldown;
    public Weapon3(Entity owner) {
        super(owner);
    }
    public void update(){
        cooldown=Math.max(cooldown-1,0);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= Screen.INSTANCE.inputManager.getMouseVec();
            Vec2d pos=owner.position;
            Vec2d velocity=input.limit(speed*7.5);
            double size=this.size*0.5;
            double health=this.health*0.7;
            double damage=this.damage*0.8;
            for(int i=0;i<15;i++) {
                cs.networkHandler.sendBulletShoot(pos.add(velocity.multiply(i/10d)), velocity.multiply(i/7d+0.5).add(Util.randomVec().limit(0.3)), size, health, damage);
            }
            cooldown=reload*2;
        }
    }
}
