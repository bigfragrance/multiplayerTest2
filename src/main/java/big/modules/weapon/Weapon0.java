package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.Util;
import big.modules.entity.Entity;

import static big.engine.math.util.Util.round;

public class Weapon0 extends NormalWeapon{
    public Weapon0(Entity owner) {
        super(owner);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.getTargetingPos();
            if(input==null) return;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*3).add(Util.randomVec().limit(0.005));
            float size=this.size*1.2;
            float health=this.health*2;
            float damage=this.damage*1.8;
            //shootBullet(pos,velocity,size,health,damage);
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*0.5;
        }
    }
}
