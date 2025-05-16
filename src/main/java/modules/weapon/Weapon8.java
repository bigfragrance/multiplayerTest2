package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import modules.entity.Entity;
import modules.entity.bullet.BulletType;

public class Weapon8 extends NormalWeapon{
    public Weapon8(Entity owner) {
        super(owner);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input= owner.targetingPos;
            if(input==null) return;
            Vec2d pos=owner.getBulletPosition();
            Entity aim=getTarget(input);
            if(aim!=null){
                Vec2d target=extrapolate2(aim,pos,speed*4);
                input=target.subtract(pos);
            }
            Vec2d velocity=input.limit(speed*4).add(Util.randomVec().limit(0.3));
            double size=this.size*1.5;
            double health=this.health*0.7;
            double damage=this.damage;
            shootBullet(pos,velocity,size,health,damage).type=new BulletType(1);
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*0.4;
        }
    }
}
