package modules.entity;

import engine.math.Box;
import engine.math.Vec2d;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;

public abstract class Entity implements NetworkItem {
    public Box boundingBox;
    public Vec2d position;
    public Vec2d prevPosition;
    public Vec2d velocity;
    public long id;
    public boolean isAlive=true;
    @Override
    public void update(JSONObject o) {
        if(o.has("basic")){
            //this.prevPosition = this.position.copy();
            JSONObject basic = o.getJSONObject("basic");
            basic.keys().forEachRemaining(key -> {
                switch(key) {
                    case ("position") -> {
                        JSONObject position = basic.getJSONObject("position");
                        this.position =Vec2d.fromJSON(position);
                    }
                    case("boundingBox") -> {
                        JSONObject boundingBox = basic.getJSONObject("boundingBox");
                        this.boundingBox = Box.fromJSON(boundingBox);
                    }
                }
            });
        }
    }
    public void tick(){
        this.prevPosition.set(this.position);
        if(cs.isServer||this.id==cs.player.id) {
            this.position.offset(this.velocity);
            this.boundingBox.offset1(this.velocity);
        }
    }
    public void render(Graphics g){

    }
    public void setPosition(Vec2d position){
        this.boundingBox.offset1(position.subtract(this.position));
        this.position=position.copy();
    }
    public JSONObject addJSON(JSONObject o) {
        JSONObject basic = new JSONObject();
        basic.put("position", this.position.toJSON());
        basic.put("boundingBox", this.boundingBox.toJSON());
        basic.put("id", this.id);
        o.put("basic", basic);

        return o;
    }
    public JSONObject toJSON() {
        return null;
    }
    public String getType(){
        return "entity";
    }
    public JSONObject getUpdate(){
        return null;
    }
}
