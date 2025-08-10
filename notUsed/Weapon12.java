package big.modules.weapon;

import big.engine.math.Vec2d;
import big.modules.entity.Entity;
import big.modules.entity.bullet.AimBullet;
import big.modules.entity.bullet.BulletType;

public class Weapon12 extends NormalWeapon{
    public Weapon12(Entity owner) {
        super(owner);
    }
    public void update(double time){
        super.update(time);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input=owner.getTargetingPos();
            Vec2d pos=owner.getBulletPosition();
            Entity aim=getTarget(input);
            if(aim!=null){
                Vec2d target=extrapolate2(aim,pos,speed*5);
                input=target.subtract(pos);
            }
            if (input==null) return;
            double size=this.size*1.8;
            double health=this.health*20;
            double damage=this.damage*0.6;
            Vec2d velocity=input.limit(speed*4);
            AimBullet b= shootAimBullet(pos.add(velocity),velocity,size,health,damage);
            b.type=new BulletType(2,true,0.75);
            b.aimPos=owner.position.add(input);
            b.maxLifeTime=250;
            b.dragFactor=0.6;
            b.speedAdd=this.speed*0.7;
            b.knockBackFactor=100;
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*2.5;
        }
    }
}
