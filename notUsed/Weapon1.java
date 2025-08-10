package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.modules.entity.Entity;

import static big.engine.math.util.Util.round;
import static big.engine.modules.EngineMain.cs;

public class Weapon1 extends NormalWeapon{
    public Weapon1(Entity owner) {
        super(owner);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.getTargetingPos();
            if (input==null) return;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*6).add(Util.randomVec().limit(0.005));
            double size=this.size;
            double health=this.health;
            double damage=this.damage*0.4;
            shootBullet(pos,velocity,size,health,damage);
            cooldown=(reload*0.1);
        }
    }
}
