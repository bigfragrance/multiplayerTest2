package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Entity;

import static engine.modules.EngineMain.cs;

public class Weapon2 extends NormalWeapon{

    public Weapon2(Entity owner) {
        super(owner);
    }

    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.getTargetingPos();
            if (input==null) return;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*3).add(Util.randomVec().limit(0.005));
            double size=this.size*0.5;
            double health=this.health*0.5;
            double damage=this.damage*0.5;
            shootBullet(pos,velocity.multiply(3),size,health,damage,60);
            shootBullet(pos,velocity.multiply(2),size*2,health*4,damage*6,60);
            shootBullet(pos,velocity.multiply(1),size*4,health*6,damage*8,60);
            cooldown=reload*3;
        }
    }
}
