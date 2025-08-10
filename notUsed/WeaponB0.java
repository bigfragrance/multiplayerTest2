package big.modules.weapon;

import big.engine.math.Vec2d;
import big.modules.entity.Entity;
import big.modules.entity.bullet.AimBullet;
import big.modules.entity.bullet.BulletType;
import big.modules.entity.player.ServerPlayerEntity;

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
