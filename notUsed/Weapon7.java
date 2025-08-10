package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.modules.entity.Entity;

import static big.engine.math.util.Util.round;
import static big.engine.modules.EngineMain.cs;

public class Weapon7 extends NormalWeapon{
    public Weapon7(Entity owner) {
        super(owner);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.getTargetingPos();
            if (input==null) return;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*9).add(Util.randomVec().limit(0.001));
            double size=this.size*0.5;
            double health=this.health*20;
            double damage=this.damage*5;
            shootBullet(pos,velocity,size,health,damage);
            cooldown= round(reload*5);
        }
    }
}
