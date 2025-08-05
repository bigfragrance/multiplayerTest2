package modules.weapon;

import engine.math.Vec2d;
import modules.entity.Entity;

public class AutoGunList extends CanAttack{
    public Entity owner;
    public double offsetRotation;
    public Vec2d offset;
    public double fov;
    public double rotation;
    public double layer;
    public AutoGunList(Entity owner, double offsetRotation, Vec2d offset, double fov, double rotation, double layer) {
        this.owner = owner;
        this.offsetRotation = offsetRotation;
        this.offset = offset;
        this.fov = fov;
        this.rotation = rotation;
        this.layer = layer;
    }
    @Override
    public double getLayer() {
        return layer;
    }

}
