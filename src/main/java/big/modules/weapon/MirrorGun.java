package big.modules.weapon;

public class MirrorGun extends GunArray{
    public MirrorGun(CanAttack gun,int type){
        if(type<0||type>2) throw new IllegalArgumentException("type must be 0,1 or 2");
        switch (type){
            case 0->guns=MirrorGun.mirrorX(gun);
            case 1->guns=MirrorGun.mirrorY(gun);
            case 2->guns=MirrorGun.mirrorXY(gun);
        }
    }
    public static CanAttack[] mirrorX(CanAttack gun){
        return new CanAttack[]{
                gun,
                gun.another(-2*gun.getOffsetRotation(),gun.getOffset().mirrorXAxis())
        };
    }
    public static CanAttack[] mirrorY(CanAttack gun){
        return new CanAttack[]{
                gun,
                gun.another(-2*gun.getOffsetRotation()+180,gun.getOffset().mirrorXAxis())
        };
    }
    public static CanAttack[] mirrorXY(CanAttack gun){
        return new CanAttack[]{
                gun,
                gun.another(-2*gun.getOffsetRotation(),gun.getOffset().mirrorXAxis()),
                gun.another(-2*gun.getOffsetRotation()+180,gun.getOffset().mirrorXAxis()),
                gun.another(180)
        };
    }
}
