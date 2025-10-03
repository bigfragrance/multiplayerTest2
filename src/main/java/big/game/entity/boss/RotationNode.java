package big.game.entity.boss;

import big.engine.math.Vec2d;
import big.engine.math.util.NNPRecorder;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.game.entity.Entity;
import big.game.weapon.Node;

public class RotationNode implements Node {
    private Entity owner;
    private NNPRecorder<Double> rotation;
    private double rotationSpeed=3;
    public RotationNode(Entity owner){
        this.owner=owner;
        rotation=new NNPRecorder<>(0d);
    }
    public void tick(){
        double r=rotation.get()+rotationSpeed;
        r=(r+540)%360-180;
        rotation.setNow(r);
    }
    public void setRotationSpeed(double s){
        rotationSpeed=s;
    }
    @Override
    public Vec2d getPos() {
        return owner.getPos();
    }

    @Override
    public Vec2d getRenderPos() {
        return owner.getRenderPosition();
    }

    @Override
    public double getAimRotation() {
        return rotation.get();
    }

    @Override
    public double getRenderAimRotation() {
        return Util.lerp(rotation.getPrev(),rotation.get(), Screen.tickDelta);
    }
}
