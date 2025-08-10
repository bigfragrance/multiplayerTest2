package big.modules.weapon;

import big.engine.math.Vec2d;
import big.modules.entity.Entity;
import big.modules.entity.bullet.AimBullet;
import big.modules.entity.bullet.BulletType;
import big.modules.entity.player.ServerPlayerEntity;

public class Weapon11 extends NormalWeapon{
    public Weapon11(Entity owner) {
        super(owner);
    }
    public void update(float time){
        super.update(time);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input=owner.getTargetingPos();
            Vec2d pos=owner.getBulletPosition();
            if (input==null) return;
            float size=this.size*1.8;
            float health=this.health*20;
            float damage=this.damage*0.8;
            Vec2d velocity=input.limit(speed*4);
            for(int i=-3;i<=3;i++) {
                AimBullet b = shootAimBullet(pos.add(velocity), velocity.rotate(i*30).multiply(1.5), size, health, damage);
                b.type = new BulletType(2, true, 0.5);
                b.aimPos = owner.position.add(input);
                b.maxLifeTime = 250;
                b.dragFactor = 0.6;
                b.speedAdd = this.speed;
                b.knockBackFactor = 400;
            }
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload;
        }
    }
}
