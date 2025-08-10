package big.modules.weapon;

import big.engine.math.Vec2d;
import big.modules.entity.Entity;
import big.modules.entity.bullet.AimBullet;
import big.modules.entity.bullet.BulletType;
import big.modules.entity.player.ServerPlayerEntity;

public class Weapon10 extends NormalWeapon{
    public Weapon10(Entity owner) {
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
            float size=this.size*0.8;
            float health=this.health*0.7;
            float damage=this.damage*0.9;
            for(int i=-1;i<=1;i++){
                Vec2d in2=new Vec2d(input.angle()+i*30);
                Vec2d velocity=in2.limit(speed*2);
                AimBullet b= shootAimBullet(pos.add(velocity),velocity,size,health,damage);
                b.type=new BulletType(1,true,0.8);
                b.owner=this.owner instanceof ServerPlayerEntity ? ((ServerPlayerEntity) this.owner) : null;
                b.maxLifeTime=80;
                b.speedAdd=speed*0.7;
            }
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*0.5;
        }
    }
}
