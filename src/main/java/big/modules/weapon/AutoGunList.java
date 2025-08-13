package big.modules.weapon;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.ColorUtils;
import big.engine.math.util.PacketUtil;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.modules.entity.Entity;
import big.modules.entity.player.AutoController;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;
import static big.modules.weapon.Gun.color;

public class AutoGunList extends CanAttack implements AbleToAim,Node {
    private AtomicInteger lastID=new AtomicInteger(0);
    public double offsetRotation;
    public Vec2d offset;
    public double fov;
    public double rotation=0;
    public double layer;
    public double size;
    public ConcurrentHashMap<Long,CanAttack> guns;
    public AutoAim<AutoGunList> autoAim;
    private boolean fire=false;
    private double sizeMultiplier=1;
    public AutoGunList(Entity owner, double offsetRotation, Vec2d offset,double size, double fov,double layer,ConcurrentHashMap<Long,CanAttack> guns) {
        this.owner = owner;
        this.offsetRotation = offsetRotation;
        this.offset = offset;
        this.fov = fov;
        this.layer = layer;
        this.guns=guns;
        this.size=size;
        autoAim=new AutoAim<>(this,fov);
    }
    public AutoGunList another(double offsetRotation){
        AutoGunList gun=new AutoGunList(owner,this.offsetRotation+offsetRotation,offset,size,fov,layer,getCloneGuns());
        return gun;
    }
    public AutoGunList another(double offsetRotation,Vec2d offset){
        return new AutoGunList(owner,this.offsetRotation+offsetRotation,offset,size,fov,layer,getCloneGuns());
    }
    public AutoGunList another(double offsetRotation,Vec2d offset,double startDelay){
        return new AutoGunList(owner,this.offsetRotation+offsetRotation,offset,size,fov,layer,getCloneGuns());
    }
    public void tick(boolean fire,boolean server){
        if(server) {
            this.sizeMultiplier=owner.getSizeMultiplier();
            updateOffsetRotation();
            autoAim.seeRangeMultiplier=owner.getFovMultiplier();
            autoAim.tick();
            for (CanAttack canAttack : guns.values()) {
                canAttack.lastNode=this;
                canAttack.tick(fire,server);
            }
        }else{
            offsetRotationAll.next();
            for (CanAttack canAttack : guns.values()) {
                canAttack.lastNode=this;
                canAttack.tick(false,server);
            }
        }
    }
    public Vec2d getOffset(){
        return offset;
    }
    public double getOffsetRotation(){
        return offsetRotationAll.get();
    }



    public void updateOffsetRotation(){
        if(lastNode!=null){
            offsetRotationAll.setNow(offsetRotation+ lastNode.getAimRotation());
        }
        else{
            offsetRotationAll.setNow(offsetRotation+owner.rotation);
        }
    }
    public void add0(CanAttack gun){
        gun.id=lastID.getAndIncrement();
        guns.put(gun.id,gun);
    }
    public void add(GunArray array){
        for(CanAttack gun:array.guns){
            add0(gun);
        }
    }
    public void add(CanAttack canAttack){
        if(canAttack instanceof Gun gun){
            add0(gun);
        }
        else if(canAttack instanceof GunArray gunArray){
            add(gunArray);
        }else{
            add0(canAttack);
        }
    }
    public void render(Graphics g){
        ArrayList<CanAttack> list=new ArrayList<>(this.guns.values());
        list.sort(Comparator.comparingDouble(CanAttack::getLayer));
        for(CanAttack canAttack:list){
            canAttack.render(g);
        }
        Box box=new Box(getRenderPos(),size*sizeMultiplier);
        g.setColor(color);
        Util.render(g,box.switchToJFrame());
        Color dark= ColorUtils.darker(color,0.6);
        g.setColor(dark);
        Util.renderPolygon(g,getRenderPos(),32,size*sizeMultiplier,getRenderRotation(),true,false);
    }
    public Vec2d getRenderPos(){
        return getRenderStartPos().add(offset.multiply(sizeMultiplier).rotate(Util.lerp(offsetRotationAll.getPrev(),offsetRotationAll.get(), Screen.tickDelta)));
    }

    @Override
    public double getAimRotation() {
        return rotation;
    }

    @Override
    public double getRenderAimRotation() {
        return Util.lerp(prevRotation,rotation, Screen.tickDelta);
    }

    public Vec2d getPos(){
        return getStartPos().add(offset.multiply(sizeMultiplier).rotate(offsetRotationAll.get()));
    }
    public Vec2d getStartPos(){
        return lastNode==null?owner.position:lastNode.getPos();
    }
    public Vec2d getRenderStartPos(){
        return lastNode==null?owner.getRenderPosition():lastNode.getRenderPos();
    }
    public ConcurrentHashMap<Long,CanAttack> getCloneGuns(){
        ConcurrentHashMap<Long,CanAttack> cloneGuns=new ConcurrentHashMap<>();
        for(Long key:guns.keySet()){
            cloneGuns.put(key,guns.get(key).another(0));
        }
        return cloneGuns;
    }
    public JSONObject getUpdate(){
        JSONObject obj=new JSONObject();
        PacketUtil.put(obj,"id",id);
        PacketUtil.put(obj,"rotation",rotation);
        PacketUtil.put(obj,"size",sizeMultiplier);
        PacketUtil.put(obj,"offsetRotationAll",offsetRotationAll.get());
        JSONArray array=new JSONArray();
        for(CanAttack canAttack:guns.values()){
            array.put(canAttack.getUpdate());
        }
        PacketUtil.put(obj,"data",array);
        return obj;
    }
    public void update(JSONObject obj){
        rotation=PacketUtil.getDouble(obj,"rotation");
        sizeMultiplier=PacketUtil.getDouble(obj,"size");
        offsetRotationAll.setNext(PacketUtil.getDouble(obj,"offsetRotationAll"));
        JSONArray array=PacketUtil.getJSONArray(obj,"data");
        for(int i=0;i<array.length();i++){
            JSONObject gunObj=array.getJSONObject(i);
            guns.get(PacketUtil.getLong(gunObj,"id")).update(gunObj);
        }
    }
    public static AutoGunList fromJSONServer(JSONObject obj){
        try {
            JSONObject info = obj.getJSONObject("info");
            JSONArray array = obj.getJSONArray("data");
            Entity owner = cs.entities.get(PacketUtil.getLong(obj, "owner"));
            AutoGunList gunList = new AutoGunList(owner, info.getDouble("offsetRotation"), Vec2d.fromJSON(info.getJSONObject("offset")), info.getDouble("size"), info.getDouble("fov"), info.getDouble("layer"), new ConcurrentHashMap<>());
            for (int i = 0; i < array.length(); i++) {
                JSONObject gunObj = array.getJSONObject(i);
                PacketUtil.put(gunObj, "owner", owner.id);
                PacketUtil.put(gunObj, "id", 0);
                CanAttack o = CanAttack.fromJSON(gunObj);
                if (o == null) continue;
                gunList.add(o);
            }
            return gunList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static AutoGunList fromJSONClient(JSONObject obj){
        try {
            JSONObject info = PacketUtil.getJSONObject(obj,"info");
            JSONArray array = PacketUtil.getJSONArray(obj,"data");
            Entity owner = cs.entities.get(PacketUtil.getLong(obj, "owner"));
            AutoGunList gunList = new AutoGunList(owner, PacketUtil.getDouble(info,"offsetRotation"), Vec2d.fromJSON(PacketUtil.getJSONObject(info,"offset")), PacketUtil.getDouble(info,"size"),PacketUtil.getDouble(info,"fov"),PacketUtil.getDouble(info,"layer"), new ConcurrentHashMap<>());
            for (int i = 0; i < array.length(); i++) {
                JSONObject gunObj = array.getJSONObject(i);
                //PacketUtil.put(gunObj, "owner", owner.id);
                CanAttack o = CanAttack.fromJSONClient(gunObj);
                if (o == null) continue;
                gunList.add(o);
            }
            gunList.id=PacketUtil.getLong(obj,"id");
            return gunList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public Vec2d getRealVelocity(){
        return owner.getRealVelocity();
    }
    public JSONObject toJSON(){
        JSONObject obj=new JSONObject();
        PacketUtil.put(obj,"id",id);
        JSONObject info=new JSONObject();
        PacketUtil.put(info,"offsetRotation",offsetRotation);
        PacketUtil.put(info,"offset",offset);
        PacketUtil.put(info,"size",size);
        PacketUtil.put(info,"fov",fov);
        PacketUtil.put(info,"layer",layer);
        PacketUtil.put(obj,"info",info);
        PacketUtil.put(obj,"type","AutoGunList");
        PacketUtil.put(obj,"owner",owner.id);
        JSONArray array=new JSONArray();
        for(CanAttack canAttack:guns.values()){
            array.put(canAttack.toJSON());
        }
        PacketUtil.put(obj,"data",array);
        return obj;
    }
    @Override
    public double getLayer() {
        return layer;
    }
    public void setSize(double m){
        sizeMultiplier=m;
    }

    @Override
    public void setTarget(Vec2d vec) {
        rotation=vec.subtract(getPos()).angle();
    }

    @Override
    public void setFire(boolean fire) {
        this.fire=fire;
    }

    @Override
    public double getBulletSpeed() {
        Gun g=getGoingToFire();
        return g==null?1:g.getBulletSpeed(g.getBulletType());
    }
    public Gun getGoingToFire(){
        Gun bestGun=null;
        double minTime=1000;
        for(CanAttack ttgun:guns.values()){
            if(ttgun instanceof Gun gun) {
                if (gun.getReload() < minTime) {
                    minTime = gun.getReload();
                    bestGun = gun;
                }
            }
        }
        return bestGun;
    }
    @Override
    public double getRotation() {
        return offsetRotationAll.get();
    }

    @Override
    public int getTeam() {
        return owner.team;
    }
}
