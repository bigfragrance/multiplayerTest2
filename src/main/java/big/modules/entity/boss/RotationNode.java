package big.modules.entity.boss;

import big.engine.math.Vec2d;
import big.engine.math.util.NNPRecorder;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.modules.entity.Entity;
import big.modules.weapon.Node;

public class RotationNode implements Node {
    private Entity owner;
    private NNPRecorder<float> rotation;
    private float rotationSpeed=3;
    public RotationNode(Entity owner){
        this.owner=owner;
        rotation=new NNPRecorder<>(0d);
    }
    public void tick(){
        float r=rotation.get()+rotationSpeed;
        r=(r+540)%360-180;
        rotation.setNow(r);
    }
    public void setRotationSpeed(float s){
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
    public float getAimRotation() {
        return rotation.get();
    }

    @Override
    public float getRenderAimRotation() {
        return Util.lerp(rotation.getPrev(),rotation.get(), Screen.tickDelta);
    }
}
