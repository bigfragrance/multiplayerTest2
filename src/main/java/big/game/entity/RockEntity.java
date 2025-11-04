package big.game.entity;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.util.PacketUtil;
import big.engine.util.Util;

import org.json.JSONObject;

import java.awt.*;

public class RockEntity extends Entity{
    public RockEntity(Vec2d position,double size){
        super();
        this.position=position;
        this.prevPosition=position;
        this.boundingBox=new Box(position,size,size);
        this.prevBoundingBox=boundingBox;
        this.velocity=Vec2d.zero();
        this.rotation=0;
        this.mass=1e8;
    }
    public RockEntity(Box boundingBox){
        super();
        this.position=boundingBox.getCenter();
        this.prevPosition=position;
        this.boundingBox=boundingBox;
        this.prevBoundingBox=boundingBox;
        this.velocity=Vec2d.zero();
        this.rotation=0;
    }
    public void tick(){
        super.tick();
        this.health=1e8;
    }
    public void update(JSONObject o){
        super.update(o);
    }
    public void render(Graphics g){
        g.setColor(Color.GRAY);
        Util.render(g,boundingBox.switchToJFrame());
        g.setColor(Color.DARK_GRAY);
        Util.renderCLine(g,boundingBox.switchToJFrame());
    }
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        o.put(PacketUtil.getShortVariableName("type"),getType());
        super.addJSON(o);
        return o;
    }
    public EntityType getType(){
        return EntityType.ROCK;
    }
    public static RockEntity fromJSON(JSONObject o){
        JSONObject basic=PacketUtil.getJSONObject(o,"basic");
        RockEntity e=new RockEntity(PacketUtil.getBox(basic,"boundingBox"));
        e.id=basic.getLong(PacketUtil.getShortVariableName("id"));
        e.update(o);
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        PacketUtil.putPacketType(o,"entity_update");
        super.addSmallJSON(o);
        return o;
    }
}
