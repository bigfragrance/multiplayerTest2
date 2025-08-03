package modules.entity;

import engine.math.Vec2d;

public interface Attackable {
    Vec2d getAimPos();
    boolean isFiring();
    Vec2d getPosition();
}
