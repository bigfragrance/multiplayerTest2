package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Entity;

import static engine.modules.EngineMain.cs;

public class Weapon3 extends NormalWeapon{

    public Weapon3(Entity owner) {
        super(owner);
    }

    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.targetingPos;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*7.5);
            double size=this.size*0.5;
            double health=this.health*0.7;
            double damage=this.damage*0.8;
            for(int i=0;i<15;i++) {
                shootBullet(pos.add(velocity.multiply(i/10d)), velocity.multiply(i/7d+0.5).add(Util.randomVec().limit(0.3)), size, health, damage);
            }
            cooldown=reload*2;
        }
    }
}
