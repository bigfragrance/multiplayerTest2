package big.modules.weapon;

import java.util.List;

public class MirrorGunList extends GunArray{
    public MirrorGunList(List<CanAttack> gun, int type, double[] startDelays){
        guns=new CanAttack[gun.size()*(type==2?4:2)];
        int index=0;
        for(CanAttack g:gun){
            MirrorGun mGun=new MirrorGun(g,type,startDelays);
            for(CanAttack sg:mGun.guns){
                guns[index++]=sg;
            }
        }
    }
}
