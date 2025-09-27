package big.modules.weapon;

import big.engine.math.Vec2d;

import java.util.List;

public class LineGunList extends GunArray{
    public LineGunList(List<CanAttack> guns, int count, Vec2d offset, double[] startDelays){
        this.guns=new CanAttack[guns.size()*count];
        int index=0;
        for(CanAttack g:guns){
            LineGun lGun=new LineGun(g,count,offset,startDelays);
            for(CanAttack sg:lGun.guns){
                this.guns[index++]=sg;
            }
        }
    }
}
