package big.modules.weapon;

import java.util.List;

public class SurroundGunList extends GunArray{
    public SurroundGunList(List<CanAttack> guns,int count,double[] startDelays){
        this.guns=new CanAttack[count*guns.size()];
        int index=0;
        for(CanAttack g:guns){
            SurroundGun  sGun=new SurroundGun(g,count,startDelays);
            for(CanAttack sg:sGun.guns){
                this.guns[index++]=sg;
            }
        }
    }
}
