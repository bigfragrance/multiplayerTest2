package big.game.world.blocks;

import big.engine.math.Vec2d;
import big.engine.math.Vec2i;
import big.engine.math.Box;
import big.engine.util.ColorUtils;
import big.engine.util.EntityUtils;
import big.engine.util.Util;
import big.game.entity.Entity;
import big.game.entity.player.ServerPlayerEntity;
import big.game.world.Block;
import big.game.world.BlockState;

import java.awt.*;
import java.util.Collection;

import static big.engine.modules.EngineMain.cs;

public class BaseBlock extends Block {
    public BaseBlock(String name, int id) {
        super(name, id);
    }
    public void tick(BlockState state, int x, int y){
        if(state.getBlock()!=this) return;
        /*Collection<Entity> entities=cs.world.getEntities(new Box(x,y));
        for(Entity e:entities){
            if(e.team!=state.getTeam()){
                e.addDamage(100,null);
            }else{
                if(e instanceof ServerPlayerEntity player){
                    player.instantRegen();
                }
            }
        }*/
    }
    public void tick(BlockState state, int x, int y, Entity e){
        if(e.team!=state.getTeam()){
            e.addDamage(20,null);
        }
    }
    public void renderMini(Graphics g,BlockState state,int x,int y){
        Color c= ColorUtils.setAlpha(EntityUtils.getTeamcolor(state.getTeam()),50);
        render(g,Util.toMiniMap(new Box(new Vec2i(x,y))),c);
    }
    public void render(Graphics g,BlockState state, int x, int y){
        Color c= ColorUtils.setAlpha(EntityUtils.getTeamcolor(state.getTeam()),50);
        render(g,new Box(new Vec2i(x,y)),c);
    }
}
