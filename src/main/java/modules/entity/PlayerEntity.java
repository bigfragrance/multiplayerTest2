package modules.entity;

import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.ctrl.InputManager;
import org.json.JSONObject;

import java.awt.*;

import static engine.modules.EngineMain.cs;
import static engine.render.Screen.tickDelta;

public class PlayerEntity extends Entity{
    public InputManager inputManager=Screen.INSTANCE.inputManager;
    public double speed=5;
    public double size=10;
    public PlayerEntity(Vec2d position) {
        super();
        this.position=position;
        this.velocity=new Vec2d(0,0);
        this.prevPosition=position.copy();
        this.boundingBox=new Box(position,size,size);

        inputManager=Screen.INSTANCE.inputManager;
    }
    public void tick() {
        if(!cs.isServer){
            if(cs.player!=null&& this.id==cs.player.id) {
                Vec2d input=inputManager.getPlayerInput();
                this.velocity.multiply1(0.7);
                this.velocity.offset(input.multiply(speed));
                super.tick();
                cs.networkHandler.sendPlayerMove(this.position);
            }else{
                super.tick();
            }
        }
        else{
            JSONObject o = new JSONObject();
            o.put("type", "entity_update");
            super.addJSON(o);
            cs.multiClientHandler.clients.forEach(c -> c.serverNetworkHandler.send(o));
        }
    }
    public void update(JSONObject o){
        super.update(o);
    }
    public void render(Graphics g){
        g.setColor(Color.BLUE);
        Util.render(g,new Box(Util.lerp(prevPosition,position,tickDelta),size,size).switchToJFrame());
    }
    public JSONObject toJSON() {
        JSONObject o=new JSONObject();
        o.put("type","player");
        super.addJSON(o);
        return o;
    }
    public String getType(){
        return "player";
    }
    public static PlayerEntity fromJSON(JSONObject o){
        JSONObject basic=o.getJSONObject("basic");
        PlayerEntity e=new PlayerEntity(Vec2d.fromJSON(basic.getJSONObject("position")));
        e.id=basic.getLong("id");
        e.boundingBox=Box.fromJSON(basic.getJSONObject("boundingBox"));
        return e;
    }
    public JSONObject getUpdate(){
        JSONObject o=new JSONObject();
        o.put("type","entity_update");
        o.put("id",this.id);
        super.addJSON(o);
        return o;
    }
}
