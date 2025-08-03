package modules.world;

import engine.math.BlockPos;
import engine.math.Box;
import engine.math.util.ColorUtils;
import engine.math.util.PacketUtil;
import engine.math.util.Util;
import engine.render.Screen;
import org.json.JSONObject;

import java.awt.*;

import static engine.math.util.Util.toMiniMap;

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
    public void tick(BlockState state,int x,int y){

    }
    public void render(Graphics g,BlockState state,int x,int y){
        if(this==Blocks.STONE){
            render(g,new Box(new BlockPos(x,y)),Color.GRAY);
        }
        if(this==Blocks.TEST){
            render(g,new Box(new BlockPos(x,y)),new Color(100,100,100,50));
        }
    }
    public void renderMini(Graphics g,BlockState state,int x,int y){
        if(this==Blocks.STONE){
            render(g,Util.toMiniMap(new Box(new BlockPos(x,y))),Color.DARK_GRAY);
        }
    }

    public static void render(Graphics g, Box box, Color c){
        g.setColor(ColorUtils.darker(c,0.6));
        Util.renderCube(g,box.switchToJFrame());
        g.setColor(c);
        Util.renderCube(g,box.expand(-0.005,-0.005).switchToJFrame());
    }
}
