package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Entity;

import static engine.math.util.Util.round;
import static engine.modules.EngineMain.cs;

public class Weapon00 extends NormalWeapon{
    public Weapon00(Entity owner) {
        super(owner);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.getTargetingPos();
            if (input==null) return;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*3);
            double size=this.size*2;
            double health=this.health*20;
            double damage=this.damage*150;
            shootBullet(pos,velocity,size,health,damage);
            cooldown= (reload*0.2);
        }
    }
}
