package modules.weapon;

import engine.math.Vec2d;
import modules.entity.Entity;
import modules.entity.bullet.AimBullet;
import modules.entity.bullet.BulletType;
import modules.entity.player.ServerPlayerEntity;

public class Weapon11 extends NormalWeapon{
    public Weapon11(Entity owner) {
        super(owner);
    }
    public void update(double time){
        super.update(time);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input=owner.targetingPos;
            Vec2d pos=owner.getBulletPosition();
            double size=this.size*1.8;
            double health=this.health*20;
            double damage=this.damage*0.5;
            Vec2d velocity=input.limit(speed*4);
            AimBullet b= shootAimBullet(pos.add(velocity),velocity,size,health,damage);
            b.type=new BulletType(5,true,0.75);
            b.aimPos=owner.position.add(input);
            b.maxLifeTime=250;
            b.dragFactor=0.6;
            b.speedAdd=this.speed*1.6;
            b.knockBackFactor=200;
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*1.5;
        }
    }
}
