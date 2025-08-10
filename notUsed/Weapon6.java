package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.modules.entity.Entity;

import static big.engine.modules.EngineMain.cs;

public class Weapon6 extends NormalWeapon{

    public Weapon6(Entity owner) {
        super(owner);
    }

    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.getTargetingPos();
            if (input==null) return;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*3);
            float size=this.size*0.7;
            float health=this.health*0.5;
            float damage=this.damage*0.5;
            for(int i=0;i<30;i++){
                float r=Util.random(0.8,1.5);
                shootBullet(pos,velocity.multiply(Util.random(1,3)).add(Util.randomVec().limit(0.13)),size*r,health*r,damage*r);
            }

            cooldown=reload*3;
        }
    }
}
