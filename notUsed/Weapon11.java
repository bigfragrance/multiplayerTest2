package big.game.weapon;

import big.engine.math.Vec2d;
import big.game.entity.Entity;
import big.game.entity.bullet.AimBullet;
import big.game.entity.bullet.BulletType;
import big.game.entity.player.ServerPlayerEntity;

public class Weapon11 extends NormalWeapon{
    public Weapon11(Entity owner) {
        super(owner);
    }
    public void update(double time){
        super.update(time);
    }
    public void shoot(){
        if(cooldown<=0){
            Vec2d input=owner.getTargetingPos();
            Vec2d pos=owner.getBulletPosition();
            if (input==null) return;
            double size=this.size*1.8;
            double health=this.health*20;
            double damage=this.damage*0.8;
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
