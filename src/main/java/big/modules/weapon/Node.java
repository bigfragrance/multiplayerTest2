package big.modules.weapon;

import big.engine.math.Vec2d;

public interface Node {
    Vec2d getPos();
    Vec2d getRenderPos();
    float getAimRotation();
    float getRenderAimRotation();
}
