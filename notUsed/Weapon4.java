package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.modules.entity.Entity;

import static big.engine.modules.EngineMain.cs;

public class Weapon4 extends NormalWeapon{

    public Weapon4(Entity owner) {
        super(owner);
    }

    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.getTargetingPos();
            if (input==null) return;
            Vec2d pos=owner.getBulletPosition();

            float size=this.size*0.5;
            float health=this.health*0.7;
            float damage=this.damage*0.8;
            float rot=0;
            for(int i=0;i<20;i++) {
                rot+=18d;
                Vec2d velocity=input.limit(speed*7.5).rotate(rot);
                shootBullet(pos, velocity.add(Util.randomVec().limit(0.003)), size, health, damage);
            }
            cooldown=reload;
        }
    }
}
