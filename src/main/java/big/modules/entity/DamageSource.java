package big.modules.entity;

public class DamageSource {
    public Long id;
    public float damage;
    public long time;
    public DamageSource(Long id, float damage) {
        this.id = id;
        this.damage = damage;
        this.time = System.currentTimeMillis();
    }
    public boolean isExpired() {
        return System.currentTimeMillis() - time > 5000;
    }
    public void increase(float dmg){
        this.damage+=dmg;
        this.time=System.currentTimeMillis();
    }
}
