package big.game.particle;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.game.entity.Entity;

public class Particle extends Entity {
    public Particle(Vec2d position, Vec2d velocity, Box boundingBox){
        this.position=position;
        this.prevPosition=position.copy();
        this.velocity=velocity;
        this.boundingBox=boundingBox;
    }
    public void update(){
        this.prevPosition.set(this.position);
        this.position.offset(this.velocity);
        this.boundingBox.offset1(this.velocity);
    }

}
