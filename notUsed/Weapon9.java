package big.modules.weapon;

import big.engine.math.Vec2d;
import big.engine.math.util.Util;
import big.modules.entity.Entity;
import big.modules.entity.bullet.AimBullet;
import big.modules.entity.bullet.BulletType;
import big.modules.entity.player.ServerPlayerEntity;

public class Weapon9 extends NormalWeapon{
    public static int maxCount=12;
    public Weapon9(Entity owner) {
        super(owner);
    }
    public void update(double time){
        super.update(time);
        shoot();
    }
    public void shoot(){
        if(cooldown<=0){
            int count=getOwnedBullets();
            if(count>=maxCount) return;
            int can=maxCount-count;
            Vec2d pos=owner.getBulletPosition();
            double size=this.size*1.2;
            double health=this.health*6;
            double damage=this.damage*0.4;
            for(int i=0;i<Math.min(can,4);i++){
                Vec2d input=new Vec2d(Math.cos(i*Math.PI/2)*10,Math.sin(i*Math.PI/2)*10).add(Util.randomVec().limit(0.05));
                Vec2d velocity=input.limit(speed*2);
                AimBullet b= shootAimBullet(pos.add(velocity),velocity,size,health,damage);
                b.type=new BulletType(1,true,0.45);
                b.infinityLifeTime=true;
                b.speedAdd=speed;
                b.owner=this.owner instanceof ServerPlayerEntity ? ((ServerPlayerEntity) this.owner) : null;
                b.dragFactor=0.55;
                b.weapon=Weapon.getB(b,0);
                b.attackDistance=1.6;
            }
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload*2;
        }
    }
}
