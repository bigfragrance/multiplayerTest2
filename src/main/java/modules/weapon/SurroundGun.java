package modules.weapon;

public class SurroundGun extends GunArray{
    public SurroundGun(CanAttack gun,int count){
        if(count<=0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        guns=new CanAttack[count];
        double angle=360d/count;
        double start=0;
        for(int i=0;i<count;i++){
            guns[i]=gun.another(start);
            start+=angle;
        }
    }
}
