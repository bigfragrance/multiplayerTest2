package modules.entity;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.ColorUtils;
import engine.math.util.EntityUtils;
import engine.math.util.PacketUtil;
import engine.math.util.Util;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;

public class PolygonEntity extends MobEntity{
    public static Color[] sidesColors={
            new Color(255, 115, 58),
            new Color(255, 211, 100),
            new Color(182, 78, 255),
            new Color(100, 255, 255),
            new Color(100, 255, 100),
            new Color(255, 100, 255),
            new Color(255, 100, 100),
            new Color(100, 100, 255),
            new Color(100, 255, 100),
            new Color(255, 100, 255),
            new Color(255, 100, 100),
            new Color(100, 100, 255),
            new Color(100, 255, 100),
            new Color(255, 100, 255),
    };
    public static double[] healths={50,100,200,300,500,600,700,800,900,1000};
    public static double[] healthMultipliers={0.5,2,4,7,10,14,18,23,28,35};
    public static double[] sizeMultipliers={0.7,1,1.5,2.2,2.9,3.8,4.5,5,5.4,5.6};
    public static double[] sizeMultipliers2={0.8,0.9,1,1.1,1.2,1.3,1.4,1.5,1.6,1.7};
    public static double[] damageMultipliers={0.5,1,2,2.5,3,3.5,4,4.5,5,5.5};
    public static double[] damageMultipliers2={0.5,0.7,1,1.2,1.3,1.4,1.45,1.5,1.55,1.6};
    public static double sizeBase=12;
    public static double damageBase=0.8;
    public static double speed=0.3;
    public Vec2d addVelocity=new Vec2d(0,0);
    public int velChangeTimer=0;
    public int sides;
    public int type;
    public double size;
    public double scoreBase=1;
    public PolygonEntity(Vec2d position,int sides,int type){
        super();
        this.position=position;
        this.prevPosition=position.copy();
        if(sides<3||sides>12) {
            sides=3;
        }
        if(type<0||type>9) {
            type=0;
        }
        this.sides=sides;
        this.type=type;
        this.size=sizeBase*sizeMultipliers[type]*sizeMultipliers2[sides-3];
        this.boundingBox=new Box(position,size,size);
        this.prevBoundingBox=boundingBox.copy();
        this.health=healths[sides-3]*healthMultipliers[type];
        this.damage=damageBase*damageMultipliers[sides-3]*damageMultipliers2[type];
        this.velocity=new Vec2d(0,0);
        this.score=this.health*Math.pow(this.damage,1.1)*scoreBase;
    }
    public void tick(){
        if(!cs.isServer) {
            super.tick();
            return;
        }
        if(velChangeTimer<=0){
            velChangeTimer=20;
            addVelocity=Util.randomVec().limit(speed);
        }
        velChangeTimer--;
        this.velocity.offset(addVelocity);
        this.velocity.multiply1(0.7);
        this.rotation+=1.5;
        this.health+=0.15;
        this.health=Math.min(this.health,getHealthMax(sides,type));
        if(this.prevRotation>280&&this.rotation>280){
            this.prevRotation-=360;
            this.rotation-=360;
        }
        super.tick();
    }
    public void update(JSONObject o){
        super.update(o);
    }
    public void render(Graphics g){
        super.render(g);
        Color team=ColorUtils.setAlpha(sidesColors[sides-3],getRenderAlpha());

        if(isDamageTick){
            team=ColorUtils.brighter(team,0.5);
        }

        double rotation=Util.lerp(this.prevRotation,this.rotation,getTickDelta());
        g.setColor(team);
        Util.renderPolygon(g,Util.lerp(prevBoundingBox,boundingBox,getTickDelta()).getCenter(),sides,size,rotation,true,true);

        g.setColor(ColorUtils.darker(team,0.6));
        double smaller=size;
        double rotAdd=0;
        double m=getSizeSmallerMultiplier(sides);
        Util.renderPolygon(g,Util.lerp(prevBoundingBox,boundingBox,getTickDelta()).getCenter(),sides,smaller,rotation+rotAdd,true,false);
        for(int i=0;i<type;i++){
            smaller*=m;
            rotAdd+=160d/sides;
            Util.renderPolygon(g,Util.lerp(prevBoundingBox,boundingBox,getTickDelta()).getCenter(),sides,smaller,rotation+rotAdd,true,false);
        }
        EntityUtils.renderHealthBar(g,this,getHealthMax(sides,type));
    }
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),"polygon");
        JSONObject o2=new JSONObject();
        o2.put("sides",sides);
        o2.put(PacketUtil.getShortVariableName("type"),type);
        o.put("polygon",o2);
        super.addJSON(o);
        return o;
    }
    public String getType(){
        return "polygon";
    }
    public static PolygonEntity fromJSON(JSONObject o){
        JSONObject basic=PacketUtil.getJSONObject(o,"basic");
        JSONObject polygon=o.getJSONObject("polygon");
        PolygonEntity e=new PolygonEntity(Vec2d.fromJSON(basic.getJSONObject(PacketUtil.getShortVariableName("position"))),polygon.getInt("sides"),polygon.getInt(PacketUtil.getShortVariableName("type")));
        e.id=basic.getLong(PacketUtil.getShortVariableName("id"));
        e.update(o);
        //e.boundingBox=Box.fromJSON(basic.getJSONObject(PacketUtil.getShortString("boundingBox")));
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,"entity_update");
        o.put(PacketUtil.getShortVariableName("id"),this.id);
        super.addJSON(o);
        return o;
    }
    public static double getHealthMax(int sides,int type){
        return healths[sides-3]*healthMultipliers[type];
    }
    private static double getSizeSmallerMultiplier(int sides){
        return Math.sqrt(0.25+0.25-0.5*(Util.cos(180-(360d/sides))));
    }
}
