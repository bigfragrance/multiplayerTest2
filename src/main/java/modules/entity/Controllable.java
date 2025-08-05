package modules.entity;

import engine.math.Vec2d;
import modules.ctrl.ServerInputManager;
import modules.weapon.GunList;

public interface Controllable {
    public void setRotation(double rotation);
    public ServerInputManager getInputManager();
    public Vec2d getPosition();
    public GunList getWeapon();
    Vec2d getRealVelocity();
    double getSpeed();
    double getFov();
}
