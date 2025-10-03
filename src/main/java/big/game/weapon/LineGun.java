package big.game.weapon;

import big.engine.math.Vec2d;

public class LineGun extends GunArray{
    private int added=-1;
    public LineGun(CanAttack gun, int count, Vec2d offset, double[] startDelays){
        if(count<=0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        guns=new CanAttack[count];
        for(int i=0;i<count;i++){
            guns[i]=gun.another(0,gun.getOffset().add(offset.multiply(i)),getStartDelay(startDelays,gun));
            guns[i].setLayer(gun.getLayer()-i*0.01);
        }
    }
    private double getStartDelay(double[] startDelays,CanAttack g){
        if(startDelays==null) return g.getStartDelay();
        added++;
        return startDelays[added%startDelays.length];
    }
}
