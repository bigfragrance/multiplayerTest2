package modules.weapon;

import engine.math.util.FileUtil;
import engine.math.util.PacketUtil;
import engine.math.util.Util;
import modules.entity.Entity;
import modules.entity.player.AutoController;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GunList {
    public static JSONObject data=new JSONObject();
    public JSONObject extradata=createDefData();
    public ConcurrentHashMap<Long,CanAttack> list;
    public AtomicInteger id=new AtomicInteger(0);
    public GunList(){
        this.list=new ConcurrentHashMap<>();
    }
    public void tick(boolean fire,boolean server){
        for(CanAttack canAttack:list.values()){
            canAttack.tick(fire,server);
        }
    }
    public void render(Graphics g){
        ArrayList<CanAttack> list=new ArrayList<>(this.list.values());
        list.sort(Comparator.comparingDouble(CanAttack::getLayer));
        for(CanAttack canAttack:list){
            canAttack.render(g);
        }
    }
    public void setSize(double m){
        for(CanAttack canAttack:list.values()){
            canAttack.setSize(m);
        }
    }
    public void add0(CanAttack gun){
        gun.id=id.getAndIncrement();
        list.put(gun.id,gun);
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
    public JSONArray getUpdate(){
        JSONArray array=new JSONArray();
        for(CanAttack canAttack:list.values()) {
            array.put(canAttack.getUpdate());
        }
        return array;
    }
    public void update(JSONArray array){
        for(int i=0;i<array.length();i++){
            JSONObject obj=array.getJSONObject(i);
            long id=PacketUtil.getLong(obj,"id");
            if(!list.containsKey(id)) continue;
            CanAttack canAttack=list.get(id);
            canAttack.update(obj);
        }
    }
    public Gun getGoingToFire(){
        Gun bestGun=null;
        double minTime=1000;
        for(CanAttack ttgun:list.values()){
            if(ttgun instanceof Gun gun) {
                if (gun.getReload() < minTime) {
                    minTime = gun.getReload();
                    bestGun = gun;
                }
            }
        }
        return bestGun;
    }
    public static void init(){
        String s= Util.read("weapons.json");
        if(s==null){
            return;
        }
        try {
            data = new JSONObject(s);
        }catch (Exception e){
            System.out.println("Error reading weapons.json");
        }
    }
    public static GunList fromID(Entity owner,String id){
        return fromJSONServer(owner, data.getJSONObject(id));
    }
    public static GunList fromJSONServer(Entity owner, JSONObject obj){
        JSONArray array=obj.getJSONArray("data");
        GunList gunList=new GunList();
        for(int i=0;i<array.length();i++){
            JSONObject gunObj=array.getJSONObject(i);
            PacketUtil.put(gunObj,"owner",owner.id);
            CanAttack o=CanAttack.fromJSON(gunObj);
            if(o==null) {
                System.out.println("Error loading gun");
                continue;
            }
            gunList.add(o);
        }
        if(obj.has("extradata")){
            gunList.extradata=obj.getJSONObject("extradata");
            fixData(gunList.extradata);
        }
        return gunList;
    }
    public double getStopFollowDistance(){
        double d= AutoController.stopFollowDistance;
        try{
            double d2=this.extradata.getDouble("stopFollow");
            d=d2;
        }catch (Exception e){
            return d;
        }
        return d;
    }
    public static GunList fromJSONClient(JSONObject obj){
        JSONArray array=obj.getJSONArray("data");
        System.out.println(array.toString());
        GunList gunList=new GunList();
        for(int i=0;i<array.length();i++){
            JSONObject gunObj=array.getJSONObject(i);
            CanAttack gun=CanAttack.fromJSONClient(gunObj);
            if(gun==null) continue;
            gunList.list.put(gun.id,gun);
        }
        return gunList;
    }
    public static void fixData(JSONObject o){
        JSONObject def=createDefData();
        for (Iterator<String> it = def.keys(); it.hasNext(); ) {
            String s = it.next();
            if(!o.has(s)){
                o.put(s,def.get(s));
            }
        }
    }
    public static JSONObject createDefData(){
        JSONObject object=new JSONObject();
        object.put("stopFollow",2);
        object.put("addVelocity",true);
        return object;
    }
    public JSONObject toJSON(){
        JSONObject obj=new JSONObject();
        JSONArray array=new JSONArray();
        for(CanAttack o:list.values()){
            array.put(o.toJSON());
        }
        obj.put("data",array);
        return obj;
    }
}
