package big.modules.weapon;

import big.modules.entity.Entity;

public class NormalWeapon extends Weapon{
    protected float cooldown=0;
    public NormalWeapon(Entity owner) {
        super(owner);
    }
    public void update(float time){
        cooldown=Math.max(cooldown-time,0);
    }
}
