package big.game.world;

import big.engine.math.Vec2i;
import big.engine.math.Box;
import big.engine.util.ColorUtils;
import big.engine.util.PacketUtil;
import big.engine.util.Util;
import big.game.entity.Entity;
import org.json.JSONObject;

import java.awt.*;

import static big.engine.util.Util.toMiniMap;

public class Block {
    public String name;
    public int id;
    public boolean solid =true;
    public Color color=new Color(0,0,0,0);
    public Block(String name,int id){
        this.name=name;
        this.id=id;
    }
    public JSONObject toJSON(){
        JSONObject json=new JSONObject();
        PacketUtil.put(json,"id",id);
        return json;
    }
    public void tick(BlockState state, int x, int y){

    }
    public void tick(BlockState state, int x, int y, Entity e){

    }
    public void render(Graphics g,BlockState state,int x,int y){
        if(this== Blocks.STONE){
            render(g,new Box(new Vec2i(x,y)),Color.GRAY);
        }
        if(this==Blocks.TEST){
            render(g,new Box(new Vec2i(x,y)),new Color(100,100,100,50));
        }
    }
    public void renderMini(Graphics g,BlockState state,int x,int y){
        if(this==Blocks.STONE){
            render(g,Util.toMiniMap(new Box(new Vec2i(x,y))),ColorUtils.setAlpha(Color.DARK_GRAY,100));
        }
    }

    public static void render(Graphics g, Box box, Color c){
        g.setColor(ColorUtils.darker(c,0.6));
        Util.renderCube(g,box.switchToJFrame());
        g.setColor(c);
        Util.renderCube(g,box.expand(-0.005,-0.005).switchToJFrame());
    }
}
