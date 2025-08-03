package modules.world;

import engine.math.BlockPos;
import engine.math.Box;
import engine.math.util.ColorUtils;
import engine.math.util.EntityUtils;
import engine.math.util.Util;
import modules.entity.Entity;

import java.awt.*;
import java.util.Collection;

import static engine.modules.EngineMain.cs;

public class BaseBlock extends Block{
    public BaseBlock(String name, int id) {
        super(name, id);
    }
    public void tick(BlockState state, int x,int y){
        if(state.getBlock()!=this) return;
        Collection<Entity> entities=cs.world.getEntities(new Box(x,y));
        for(Entity e:entities){
            if(e.team!=state.getTeam()){
                e.addDamage(100);
            }
        }
    }
    public void renderMini(Graphics g,BlockState state,int x,int y){
        Color c= ColorUtils.setAlpha(EntityUtils.getTeamcolor(state.getTeam()),50);
        render(g,Util.toMiniMap(new Box(new BlockPos(x,y))),c);
    }
    public void render(Graphics g,BlockState state, int x, int y){
        Color c= ColorUtils.setAlpha(EntityUtils.getTeamcolor(state.getTeam()),50);
        render(g,new Box(new BlockPos(x,y)),c);
    }
}
