package big.modules.entity;

import big.engine.math.Vec2d;
import big.modules.ctrl.ServerInputManager;
import big.modules.weapon.GunList;

public interface Controllable {
    public void setRotation(float rotation);
    public ServerInputManager getInputManager();
    public Vec2d getPosition();
    public GunList getWeapon();
    Vec2d getRealVelocity();
    float getSpeed();
    float getFov();
}
