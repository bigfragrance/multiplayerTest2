package big.game.entity;

import big.engine.math.Vec2i;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.util.ColorUtils;
import big.engine.util.PacketUtil;
import big.engine.util.Util;
import org.json.JSONObject;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;


public class BlockEntity extends Entity{
    public static Color color=new Color(115, 115, 115);
    public Vec2i blockPos=new Vec2i(0,0);
    public boolean leftCheck=true;
    public boolean rightCheck=true;
    public boolean topCheck=true;
    public boolean buttonCheck=true;
    public BlockEntity(Box box){
        super();
        this.position=box.getCenter();
        this.prevPosition=position.copy();
        this.boundingBox=box;
        this.prevBoundingBox=box.copy();
        this.health=10000000;
        this.damage=0;
        this.velocity=new Vec2d(0,0);
        this.score=0;
        this.checkBorderCollision=false;
        this.mass=1000000;
    }
    public void tick(){
        if(!cs.isServer) {
            super.tick();
            return;
        }
        super.tick();
    }
    public void update(JSONObject o){
        super.update(o);
    }
    public void render(Graphics g){
        super.render(g);
        Color team=color;
        g.setColor(ColorUtils.darker(team,0.6));
        Util.renderCubeLine(g,boundingBox.switchToJFrame());
        g.setColor(team);
        Util.renderCube(g,boundingBox.switchToJFrame());
    }
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),"block");
        super.addJSON(o);
        return o;
    }
    public EntityType getType(){
        return EntityType.BLOCK;
    }
    public static BlockEntity fromJSON(JSONObject o){
        JSONObject basic=PacketUtil.getJSONObject(o,"basic");
        BlockEntity e=new BlockEntity(Box.fromJSON(PacketUtil.getJSONObject(basic,"boundingBox")));
        e.id=basic.getLong(PacketUtil.getShortVariableName("id"));
        e.update(o);
        //e.boundingBox=Box.fromJSON(basic.getJSONObject(PacketUtil.getShortString("boundingBox")));
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,"entity_update");
        o.put(PacketUtil.getShortVariableName("id"),this.id);
        addJSON(o);
        return o;
    }
    public JSONObject addJSON(JSONObject o) {
        JSONObject basic = new JSONObject();
        basic.put(PacketUtil.getShortVariableName("boundingBox"), this.boundingBox.toJSON());
        basic.put(PacketUtil.getShortVariableName("id"), this.id);
        basic.put(PacketUtil.getShortVariableName("isAlive"),this.isAlive);
        PacketUtil.put(o,"basic", basic);
        return o;
    }
}
