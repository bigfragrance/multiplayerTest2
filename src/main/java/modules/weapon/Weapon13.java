package modules.weapon;

import engine.math.Vec2d;
import modules.entity.Entity;
import modules.entity.bullet.AimBullet;
import modules.entity.bullet.BulletType;

public class Weapon13 extends NormalWeapon{
    public Weapon13(Entity owner) {
        super(owner);
    }
    public void update(double time){
        super.update(time);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input=owner.targetingPos;
            Vec2d pos=owner.getBulletPosition();
            double size=this.size*0.6;
            double health=this.health*20;
            double damage=this.damage*0.1;
            Vec2d velocity=input.limit(speed*4);
            AimBullet b= shootAimBullet(pos.add(velocity),velocity,size,health,damage);
            b.type=new BulletType(3,false,0.75);
            b.aimPos=owner.position.add(input);
            b.maxLifeTime=500;
            b.dragFactor=0.6;
            b.speedAdd=this.speed*1.6;
            b.knockBackFactor=20;
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*0.1;
        }
    }
}
