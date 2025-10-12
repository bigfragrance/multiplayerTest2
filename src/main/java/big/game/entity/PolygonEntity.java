package big.game.entity;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.util.ColorUtils;
import big.engine.util.EntityUtils;
import big.engine.util.PacketUtil;
import big.engine.util.Util;
import big.game.entity.player.PlayerEntity;
import big.game.world.World;
import org.json.JSONObject;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;

public class PolygonEntity extends MobEntity{
    public static Color[] sidesColors={
            new Color(255, 115, 58),//0
            new Color(255, 211, 100),//1
            new Color(182, 78, 255),//2
            new Color(100, 255, 255),//3
            new Color(100, 255, 100),//4
            new Color(255, 100, 255),//5
            new Color(255, 100, 100),//6
            new Color(100, 100, 255),//7
            new Color(175, 100, 255),//8
            new Color(253, 192, 192),//9
            new Color(255, 249, 188),//10
            new Color(171, 255, 124),//11
            new Color(132, 237, 255),//12
            new Color(221, 176, 251),//13
            new Color(255, 255, 194),//14
            new Color(200,200,200),//15
            new Color(150,150,150),//16
            new Color(100,100,100),//17
            new Color(75,75,75),//18
            new Color(50,50,50),//19
            new Color(35, 35, 35),//20
    };
    public static double[] healths={100,//0
            20,//1
            200,//2
            300,//3
            500,//4
            600,//5
            700,//6
            800,//7
            900,//8
            1000,//9
            1200,//10
            1400,//11
            1600,//12
            1800,//13
            2000,//14
            2300,//15
            2600,//16
            3000,//17
            3400,//18
            3800,//19
            4200};//20
    public static double[] healthMultipliers={0.5,//0
            2,//1
            4,//2
            7,//3
            10,//4
            14,//5
            18,//6
            23,//7
            28,//8
            35,//9
            42,//10
            48,//11
            55,//12
            60,//13
            75,//14
            80,//15
            85,//16
            90,//17
            95,//18
            100,//19
            120};//20
    public static double[] sizeMultipliers={0.7,//0
            1,//1
            1.5,//2
            2.2,//3
            2.9,//4
            3.8,//5
            4.5,//6
            5,//7
            5.2,//8
            5.3,//9
            5.4,//10
            5.4,//11
            5.4,//12
            5.4,//13
            5.4,//14
            5.4,//15
            5.2,//16
            5.2,//17
            5.2,//18
            5.2,//19
            5.2};//20
    public static double[] sizeMultipliers2={0.8,//0
            0.9,//1
            1, //2
            1.1,//3
            1.2,//4
            1.3,//5
            1.4,//6
            1.5,//7
            1.6,//8
            1.7,//9
            1.7,//10
            1.7,//11
            1.7,//12
            1.7,//13
            1.7,//14
            1.7,//15
            1.7,//16
            1.7,//17
            1.7,//18
            1.7,//19
            1.7//20
    };
    public static double[] damageMultipliers2 ={1,//0
            0.2,//1
            2,//2
            2.5,//3
            3,//4
            3.5,//5
            4,//6
            4.5,//7
            5,//8
            5.5,//9
            6,//10
            6.5,//11
            7,//12
            7.8,//13
            8.5,//14
            9.5,//15
            10.5,//16
            11.5,//17
            13,//18
            15,//19
            20//20
    };
    public static double[] damageMultipliers ={0.5,0.7,1,1.2,1.3,1.4,1.45,1.5,1.55,1.6};
    public static double[] spawnAreas={1,1,0.4,0.4,0.4,0.3,0.3,0.3,0.3,0.3};
    public static double sizeBase=12*sizeMultiplier;
    public static double damageBase=8;
    public static double speed=0.08*sizeMultiplier;
    public static double attackSpeed=1*sizeMultiplier;
    public Vec2d addVelocity=new Vec2d(0,0);
    public int velChangeTimer=0;
    public int sides;
    public int type;
    public double size;
    public double scoreBase=0.05;

    public PolygonEntity(Vec2d position,int sides,int type){
        super();
        this.position=position;
        this.prevPosition=position.copy();
        if(sides<3) {
            sides=3;
        }
        if(sides>=21+3){
            sides=23;
        }
        if(type<0||type>9) {
            type=0;
        }
        if(sides>=5||type>=7){
            attackPlayer=true;
        }
        if(sides>=7||type>=8){
            autoTargeting=true;
        }
        this.sides=sides;
        this.type=type;
        this.size=sizeBase*sizeMultipliers[type]*sizeMultipliers2[sides-3];
        this.boundingBox=new Box(position,size,size);
        this.prevBoundingBox=boundingBox.copy();
        this.health=healths[sides-3]*healthMultipliers[type];
        this.damage=damageBase* damageMultipliers2[sides-3]* damageMultipliers[type];
        this.velocity=new Vec2d(0,0);
        this.score=this.health*Math.pow(this.damage,1.2)*scoreBase;
    }
    public void tick(){
        if(!cs.isServer) {
            super.tick();
            return;
        }
        if(target==null) {
            if (velChangeTimer <= 0) {
                velChangeTimer = 20;
                addVelocity = Util.randomVec().limit(speed);
            }
            velChangeTimer--;
        }else{
            addVelocity=target.getPos().subtract(this.getPos()).limit(attackSpeed);
            this.rotation+=30;
            if(target.getPos().distanceTo(this.getPos())>=10||!target.isAlive){
                target=null;
            }
        }
        this.velocity=getRealVelocity();
        this.velocity.multiply1(0.9);
        this.velocity.offset(addVelocity);
        if(World.gravityEnabled){
            whenGravity();
        }
        this.rotation+=1;
        this.health+=0.15;
        this.health=Math.min(this.health,getHealthMax(sides,type));
        if(this.prevRotation>500&&this.rotation>500){
            this.prevRotation-=360;
            this.rotation-=360;
        }
        super.tick();
    }
    private void whenGravity(){
        this.velocity.offset(0,World.gravity);
        boolean shouldJump=EntityUtils.isInsideWall(this.boundingBox.expand(-0.02).offset(velocity.x*5,0))||target!=null;
        if(shouldJump&&isOnGround()){
            this.velocity.set(velocity.x*2,0.5);
        }
    }
    public void update(JSONObject o){
        super.update(o);
    }
    public void storeDamage(Entity e,double damage){
        super.storeDamage(e,damage);
        if(!attackPlayer) return;
        Entity attacker=cs.entities.get(e.getDamageSourceID());
        if(attacker instanceof PlayerEntity player){
            if(!player.isAlive) return;
            if(player.getPos().distanceTo(this.getPos())>=7) return;
            if(target==null){
                target=player;
            }
        }
    }
    public void kill(){
        super.kill();
        if(this.type>0&&cs.isServer){
            for(int i=0;i<cs.setting.getPolygonSplit();i++){
                cs.addEntity(new PolygonEntity(this.getPos().add(Util.randomVec().multiply(0.1)),this.sides,this.type-1));
            }
        }
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
    public void move(Vec2d vec){
        //Box b=this.boundingBox.stretch(this.velocity.x,this.velocity.y);
        /*EntityUtils.updateCollision(this,e->e.id==this.id||!e.isAlive||!(e instanceof BlockEntity),e->e.boundingBox.intersects(b),(e)->{
            BlockEntity block=(BlockEntity)e;
            vec.set(EntityUtils.getMaxMove(this.boundingBox,vec,e.boundingBox,block.leftCheck,block.rightCheck,block.topCheck,block.buttonCheck));
        });*/
        vec.set(EntityUtils.getMaxMove(this.boundingBox,vec));
        this.position.offset(vec);
        this.boundingBox=this.boundingBox.offset(vec);
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
        super.addMediumJSON(o);
        return o;
    }
    public static double getHealthMax(int sides,int type){
        return healths[sides-3]*healthMultipliers[type];
    }
    private static double getSizeSmallerMultiplier(int sides){
        return Math.sqrt(0.25+0.25-0.5*(Util.cos(180-(360d/sides))));
    }

    //0
    //1
    //2
    //3
    //4
    //5
    //6
    //7
    //8
    //9
    //10
    //11
    //12
    //13
    //14
    //15
    //16
    //17
    //18
    //19
    //20
}
