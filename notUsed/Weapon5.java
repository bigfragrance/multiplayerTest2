package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.entity.Entity;

import static engine.math.util.Util.round;
import static engine.modules.EngineMain.cs;

public class Weapon5 extends NormalWeapon{

    private int last=0;
    public Weapon5(Entity owner) {
        super(owner);
    }

    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.getTargetingPos();
            if (input==null) return;
            Vec2d pos=owner.getBulletPosition();
            Vec2d velocity=input.limit(speed*6).add(Util.randomVec().limit(0.005));
            double size=this.size*0.5;
            double health=this.health*300;
            double damage=this.damage*0.5;

            if(last==0){
                last=1;
                pos=pos.add(velocity.limit(0.1).rotate(90));
            }else if(last==1){
                last=0;
                pos=pos.add(velocity.limit(0.1).rotate(-90));
            }

            shootBullet(pos.add(velocity.multiply(0.1)),velocity,size,health,damage);
            cooldown=round(reload*0.1);
        }
    }
}
