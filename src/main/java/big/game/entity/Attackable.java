package big.game.entity;

import big.engine.math.Vec2d;

import java.util.concurrent.ConcurrentHashMap;

public interface Attackable {
    Vec2d getAimPos();
    boolean isFiring();
    boolean isDefending();
    Vec2d getPosition();
    int getTeam();
    double getFov();
    double getRotation();
    ConcurrentHashMap<Entity,Vec2d> getControllingShieldBullets();
}
