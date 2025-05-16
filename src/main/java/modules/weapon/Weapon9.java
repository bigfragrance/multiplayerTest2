package modules.weapon;

import engine.math.Vec2d;
import engine.math.util.Util;
import modules.entity.Entity;
import modules.entity.bullet.AimBullet;
import modules.entity.bullet.BulletType;
import modules.entity.player.ServerPlayerEntity;

public class Weapon9 extends NormalWeapon{
    public static int maxCount=8;
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
            double size=this.size*1.5;
            double health=this.health*4;
            double damage=this.damage*1.5;
            for(int i=0;i<Math.min(can,4);i++){
                Vec2d input=new Vec2d(Math.cos(i*Math.PI/2)*10,Math.sin(i*Math.PI/2)*10);
                Vec2d velocity=input.limit(speed*2);
                AimBullet b= shootAimBullet(pos.add(velocity),velocity,size,health,damage);
                b.type=new BulletType(1,true,0.3);
                b.infinityLifeTime=true;
                b.owner=this.owner instanceof ServerPlayerEntity ? ((ServerPlayerEntity) this.owner) : null;
            }
            //shootBullet(pos,velocity,size,health,damage);
            cooldown= reload;
        }
    }
}
