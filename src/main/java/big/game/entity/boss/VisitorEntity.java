package big.game.entity.boss;

import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.game.entity.player.ServerBotEntity;
import big.game.weapon.CanAttack;
import big.game.weapon.GunList;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class VisitorEntity extends ServerBotEntity {
    //                                       {          "Damage[z]","Speed[x]","Health[c]","Size[v]","Reload[b]","MoveSpeed[n]","DamageAbsorb[m]","ShieldRegen[,]","HealthRegen[.]","Fov[/]"};
    public static double[] defSkillPoints= new double[]{0.5         ,1       ,0.5          ,5        ,1          ,0.5           ,10               ,0.1             ,0.05           ,2};
    private boolean inited=false;
    private ConcurrentHashMap<Integer,RotationNode> rotationNodes=new ConcurrentHashMap<>();
    public VisitorEntity(Vec2d position,int level){
        super(position,-1);
        this.team=-1;
        this.skillPointUsed=100000000;
        this.skillPoints=defSkillPoints;
        this.score=1000000;
        this.name="Visitor";
        this.weaponID=getRandom(new Random(System.nanoTime()).nextInt(4));
        this.damage*=5;
        this.autoController.dodge=false;
    }

    public void tick(){
        updateWeapon();
        super.tick();
        if(!inited&&this.weapon!=null){
            initWeapon();
        }
    }
    private void updateWeapon(){
        if(!inited) return;
        for(RotationNode node:rotationNodes.values()){
            node.tick();
        }
    }
    private void initWeapon(){
        weapon.tick(true,false,true);
        boolean r=false;
        for(CanAttack ca:weapon.list.values()){
            if(ca.lastNode==null){
                int layer=(int)Math.floor(ca.getLayer());
                if(!rotationNodes.containsKey(layer)){
                    RotationNode node=new RotationNode(this);
                    rotationNodes.put(layer,node);
                }
                ca.lastNode=(rotationNodes.get(layer));
            }
        }
        for(int i=-10;i<=20;i++){
            if(rotationNodes.containsKey(i)){
                rotationNodes.get(i).setRotationSpeed(r?3:-3);
                r=!r;
            }
        }
        inited=true;
        for(int i=0;i<50;i++){
            updateWeapon();
            weapon.tick(true,false,true);
        }
    }
    public void render(Graphics g){
        this.tickDelta= Screen.tickDelta;//Math.min(1,this.tickDelta+ Screen.tickDeltaAdd);
        if(weapon!=null){
            weapon.render(g);
        }
        EntityUtils.render(g,this);
        if(!cs.isServer)sc.renderAtLast((gr)->{
            sc.storeAndSetDef();
            EntityUtils.render(gr,Util.toMiniMap(this.boundingBox.expand(0.3,0.3)),EntityUtils.getTeamcolor(this.team));
            sc.restoreZoom();
        });
        //EntityUtils.renderHealthBar(g,this,);
        //System.out.println(this.name);
        EntityUtils.renderPlayerName(g,this);
        EntityUtils.renderScore(g,this);
    }
    private String getRandom(int level){
        ArrayList<String> list=new ArrayList<>();
        for (Iterator<String> it = GunList.data.keys(); it.hasNext(); ) {
            String s = it.next();
            if(!s.contains("visitor")) continue;
            int l=getValue(s.charAt(8));
            if(l>level) continue;
            list.add(s);
        }
        int r= Math.clamp((int) Math.floor(Util.random(0,list.size())),0,list.size()-1);
        return list.get(r);
    }
    private int getValue(char c){
        return switch (c) {
            case '0' -> 0;
            case '1' -> 1;
            case '2' -> 2;
            case '3' -> 3;
            case '4' -> 4;
            case '5' -> 5;
            case '6' -> 6;
            case '7' -> 7;
            case '8' -> 8;
            case '9' -> 9;
            default -> -1;
        };
    }
    private void add(ConcurrentHashMap<Integer,ArrayList<String>> map,int level,String s){
        if(!map.containsKey(level)){
            map.put(level,new ArrayList<>());
        }
        map.get(level).add(s);
    }
}
