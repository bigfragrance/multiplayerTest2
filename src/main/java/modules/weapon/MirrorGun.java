package modules.weapon;

public class MirrorGun extends GunArray{
    public MirrorGun(Gun gun,int type){
        if(type<0||type>2) throw new IllegalArgumentException("type must be 0,1 or 2");
        switch (type){
            case 0->guns=MirrorGun.mirrorX(gun);
            case 1->guns=MirrorGun.mirrorY(gun);
            case 2->guns=MirrorGun.mirrorXY(gun);
        }
    }
    public static Gun[] mirrorX(Gun gun){
        return new Gun[]{
                gun,
                gun.another(-2*gun.offsetRotation,gun.offset.mirrorXAxis())
        };
    }
    public static Gun[] mirrorY(Gun gun){
        return new Gun[]{
                gun,
                gun.another(-2*gun.offsetRotation+180,gun.offset.mirrorXAxis())
        };
    }
    public static Gun[] mirrorXY(Gun gun){
        return new Gun[]{
                gun,
                gun.another(-2*gun.offsetRotation,gun.offset.mirrorXAxis()),
                gun.another(-2*gun.offsetRotation+180,gun.offset.mirrorXAxis()),
                gun.another(180)
        };
    }
}
