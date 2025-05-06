package modules.weapon;

import modules.entity.Entity;

public class Weapon{
    public static double damageBase=10;
    public static double speedBase=10;
    public static double healthBase=10;
    public static int reloadBase=10;
    public static double sizeBase=5;
    public Entity owner;
    public double damage;
    public double speed;
    public double health;
    public double reload;
    public double size;
    public Weapon(Entity owner){
        this.owner=owner;
        this.damage=10;
        this.speed=10;
        this.health=10;
        this.reload=10;
        this.size=5;
    }
    public void setMultiplier(double multiplier){
        this.damage=damageBase*multiplier;
        this.speed=speedBase*multiplier;
        this.health=healthBase*multiplier;
        this.reload= (reloadBase/multiplier);
        this.size=sizeBase*multiplier;
    }
    public void update(){

    }
    public void shoot(){

    }
    public static Weapon get(Entity owner,int type){
        switch (type){
            case(0)->{
                return new Weapon0(owner);
            }
            case(1)->{
                return new Weapon1(owner);
            }
            case(2)->{
                return new Weapon2(owner);
            }
            case(3)->{
                return new Weapon3(owner);
            }
            case(4)->{
                return new Weapon4(owner);
            }
            case(5)->{
                return new Weapon5(owner);
            }
            case(6)->{
                return new Weapon6(owner);
            }
            case(7)->{
                return new Weapon7(owner);
            }
            case(1258764)->{
                return new Weapon00(owner);
            }
        }
        return null;
    }
}
