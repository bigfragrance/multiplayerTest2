package big.game.weapon;

public class MirrorGun extends GunArray{
    private int added=-1;
    private double[] startDelays;
    public MirrorGun(CanAttack gun,int type,double[] startDelays){
        if(type<0||type>2) throw new IllegalArgumentException("type must be 0,1 or 2");
        this.startDelays=startDelays;
        switch (type){
            case 0->guns=mirrorX(gun);
            case 1->guns=mirrorY(gun);
            case 2->guns=mirrorXY(gun);
        }
    }
    public CanAttack[] mirrorX(CanAttack gun){
        return new CanAttack[]{
                gun,
                gun.another(-2*gun.getOffsetRotation(),gun.getOffset().mirrorXAxis(),getStartDelay(gun))
        };
    }
    public CanAttack[] mirrorY(CanAttack gun){
        return new CanAttack[]{
                gun,
                gun.another(-2*gun.getOffsetRotation()+180,gun.getOffset().mirrorXAxis(),getStartDelay(gun))
        };
    }
    public CanAttack[] mirrorXY(CanAttack gun){
        return new CanAttack[]{
                gun,
                gun.another(-2*gun.getOffsetRotation(),gun.getOffset().mirrorXAxis(),getStartDelay(gun)),
                gun.another(-2*gun.getOffsetRotation()+180,gun.getOffset().mirrorXAxis(),getStartDelay(gun)),
                gun.another(180,gun.getOffset(),getStartDelay(gun))
        };
    }
    private double getStartDelay(CanAttack g){
        if(startDelays==null) return g.getStartDelay();
        added++;
        return startDelays[added%startDelays.length];
    }
}
