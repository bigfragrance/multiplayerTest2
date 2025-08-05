package modules.entity.boss;

import engine.math.Vec2d;
import modules.ctrl.ServerInputManager;
import modules.entity.Attackable;
import modules.entity.Controllable;
import modules.entity.MobEntity;
import modules.entity.player.ServerPlayerEntity;
import modules.weapon.GunList;

public class VisitorEntity extends ServerPlayerEntity implements Attackable, Controllable {
    public static double SPEED=0.3*sizeMultiplier;
    public VisitorEntity(Vec2d position,int level){
        super(position);
        this.team=-1;
    }
    public void tick(){
        super.tick();
    }
    @Override
    public Vec2d getAimPos() {
        return inputManager.aimPos.add(getPosition());
    }

    @Override
    public boolean isFiring() {
        return inputManager.shoot;
    }

    @Override
    public void setRotation(double rotation) {
        this.rotation=rotation;
    }

    @Override
    public ServerInputManager getInputManager() {
        return inputManager;
    }

    @Override
    public Vec2d getPosition() {
        return this.position;
    }

    @Override
    public GunList getWeapon() {
        return weapon;
    }

    @Override
    public double getSpeed() {
        return SPEED;
    }

    @Override
    public double getFov() {
        return 1;
    }
}
