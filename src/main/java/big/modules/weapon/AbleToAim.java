package big.modules.weapon;

import big.engine.math.Vec2d;

public interface AbleToAim {
    void setTarget(Vec2d vec);
    void setFire(boolean fire);
    Vec2d getPos();
    Vec2d getRealVelocity();
    float getBulletSpeed();
    float getRotation();
    int getTeam();
}
