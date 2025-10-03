package big.game.world.blocks;

import big.engine.math.BlockPos;
import big.engine.math.Box;
import big.engine.math.util.ColorUtils;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.Util;
import big.game.entity.Entity;
import big.game.entity.player.ServerPlayerEntity;
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
        Collection<Entity> entities=cs.world.getEntities(new Box(x,y));
        for(Entity e:entities){
            if(e.team!=state.getTeam()){
                e.addDamage(100,null);
            }else{
                if(e instanceof ServerPlayerEntity player){
                    player.instantRegen();
                }
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
