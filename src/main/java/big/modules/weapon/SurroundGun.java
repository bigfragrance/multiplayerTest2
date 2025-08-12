package big.modules.weapon;

public class SurroundGun extends GunArray{
    private int added=-1;
    public SurroundGun(CanAttack gun,int count,double[] startDelays){
        if(count<=0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        guns=new CanAttack[count];
        double angle=360d/count;
        double start=0;
        for(int i=0;i<count;i++){
            guns[i]=gun.another(start,gun.getOffset(),getStartDelay(startDelays,gun));
            start+=angle;
        }
    }
    private double getStartDelay(double[] startDelays,CanAttack g){
        if(startDelays==null) return g.getStartDelay();
        added++;
        return startDelays[added%startDelays.length];
    }
}
