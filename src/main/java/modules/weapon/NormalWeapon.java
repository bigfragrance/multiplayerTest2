package modules.weapon;

import modules.entity.Entity;

public class NormalWeapon extends Weapon{
    protected double cooldown=0;
    public NormalWeapon(Entity owner) {
        super(owner);
    }
    public void update(double time){
        cooldown=Math.max(cooldown-time,0);
    }
}
