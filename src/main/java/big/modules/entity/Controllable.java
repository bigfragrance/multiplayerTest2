package big.modules.entity;

import big.engine.math.Vec2d;
import big.modules.ctrl.ServerInputManager;
import big.modules.weapon.GunList;

public interface Controllable {
    public void setRotation(double rotation);
    public ServerInputManager getInputManager();
    public Vec2d getPosition();
    public GunList getWeapon();
    Vec2d getRealVelocity();
    double getSpeed();
    double getFov();
}
