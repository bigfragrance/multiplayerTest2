package big.game.weapon;

import big.engine.math.Vec2d;
import big.game.entity.Entity;
import big.game.entity.bullet.AimBullet;
import big.game.entity.bullet.BulletType;

public class Weapon14 extends NormalWeapon{
    public Weapon14(Entity owner) {
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
                Vec2d target=extrapolate2(aim,pos,speed*4);
                input=target.subtract(pos);
            }
            if (input==null) return;
            double size=this.size*0.6;
            double health=this.health*20;
            double damage=this.damage*0.1;
            Vec2d velocity=input.limit(speed*4);
            AimBullet b= shootAimBullet(pos.add(velocity),velocity,size,health,damage);
            b.type=new BulletType(2,false,0.75);
            b.aimPos=owner.position.add(input);
            b.maxLifeTime=500;
            b.dragFactor=0.6;
            b.speedAdd=this.speed*0.7;
            b.knockBackFactor=20;
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*0.1;
        }
    }
}
