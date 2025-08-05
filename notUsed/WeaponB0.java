package modules.weapon;

import engine.math.Vec2d;
import modules.entity.Entity;
import modules.entity.bullet.AimBullet;
import modules.entity.bullet.BulletType;
import modules.entity.player.ServerPlayerEntity;

public class WeaponB0 extends NormalWeapon{
    public WeaponB0(Entity owner) {
        super(owner);
    }
    public void update(double time){
        super.update(time);
        shoot();
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d pos=owner.getBulletPosition();
            Vec2d input=owner.getTargetingPos();
            Vec2d velocity=input.limit(speed*5);
            double size=this.size*0.3;
            double health=this.health*0.3;
            double damage=this.damage*0.4;
            shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*0.6;
        }
    }
}
