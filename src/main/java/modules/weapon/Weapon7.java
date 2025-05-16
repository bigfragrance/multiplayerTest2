package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Entity;

import static engine.math.util.Util.round;
import static engine.modules.EngineMain.cs;

public class Weapon7 extends NormalWeapon{
    public Weapon7(Entity owner) {
        super(owner);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.targetingPos;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*9).add(Util.randomVec().limit(0.1));
            double size=this.size*0.5;
            double health=this.health*20;
            double damage=this.damage*5;
            shootBullet(pos,velocity,size,health,damage);
            cooldown= round(reload*5);
        }
    }
}
