package big.game.world.blocks;

import big.engine.math.Box;
import big.engine.math.Direction;
import big.engine.math.Vec2d;
import big.engine.math.Vec2i;
import big.engine.util.*;
import big.game.entity.Entity;
import big.game.entity.player.ServerPlayerEntity;
import big.game.world.Block;
import big.game.world.BlockState;

import java.awt.*;
import java.util.Collection;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class PushBlock extends Block {
    public static Color BASE_COLOR=new Color(46, 46, 46);
    public static Color ARROW_COLOR=new Color(150,150,150);
    public PushBlock(String name, int id) {
        super(name, id);
    }
    public void tick(BlockState state, int x, int y, Entity e){
        Direction dir=getPushDirection(state);
        if(dir==null) return;
        Vec2d push=dir.getOffset().toVec2d();
        double s=push.dot(e.velocity);
        e.velocity.offset(push.multiply(Math.pow(Math.E,-s)*0.15));
    }
    public static Direction getPushDirection(BlockState state){
        if(!PacketUtil.contains(state.getData(),"direction")){
            //setPushDirection(state,Direction.RIGHT);
            return null;
        }
        return Direction.fromName(PacketUtil.getString(state.getData(),"direction"));
    }
    public void renderMini(Graphics g, BlockState state, int x, int y){
        render(g, Util.toMiniMap(new Box(new Vec2i(x,y))),BASE_COLOR);
    }
    public void render(Graphics g,BlockState state, int x, int y){
        render(g,new Box(new Vec2i(x,y)),BASE_COLOR);
        renderArrow(g,Vec2d.center(x,y),getPushDirection(state));
    }
    public void renderArrow(Graphics g,Vec2d center,Direction dir){
        if(dir==null) return;
        g.setColor(ARROW_COLOR);
        ((Graphics2D)g).setStroke(new BasicStroke((float) (0.001/sc.zoom2*sc.zoom),BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Vec2dList list=new Vec2dList();
        list.add(center.add(-0.4,0));
        list.add(center.add(0.4,0));
        list.add(center.add(0.1,0.3));
        list.add(center.add(0.1,-0.3));
        list.rotate(dir.getAngle(),center);
        list.switchToJFrame();
        Util.renderLine(g,list.get(0),list.get(1));
        Util.renderLine(g,list.get(1),list.get(2));
        Util.renderLine(g,list.get(1),list.get(3));
    }
    public static void setPushDirection(BlockState state,Direction dir){
        PacketUtil.put(state.getData(),"direction",dir.name());
    }
}
