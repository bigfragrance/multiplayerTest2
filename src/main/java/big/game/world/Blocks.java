package big.game.world;

import big.game.world.blocks.BaseBlock;
import big.game.world.blocks.PushBlock;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Blocks {
    public static ConcurrentHashMap<Integer, Block> blocks_id=new ConcurrentHashMap<>();
    public static AtomicInteger id=new AtomicInteger(0);
    public static final Block AIR=register("air",false,new Color(150,150,150,20));
    public static final Block STONE=register("stone");
    public static final Block BASE_BLOCK=register(new BaseBlock("base_block",0),false,Color.WHITE);
    public static final Block PUSH_BLOCK=register(new PushBlock("push_block",0),false,PushBlock.BASE_COLOR);
    public static final Block TEST=register("test");
    public static Block register(String name){
        Block b=new Block(name,id.get());
        blocks_id.put(b.id,b);
        id.incrementAndGet();
        return b;
    }
    public static Block register(String name,boolean movement){
        Block b=new Block(name,id.get());
        b.solid =movement;
        blocks_id.put(b.id,b);
        id.incrementAndGet();
        return b;
    }
    public static Block register(String name, boolean movement, Color color){
        Block b=new Block(name,id.get());
        b.solid =movement;
        b.color=color;
        blocks_id.put(b.id,b);
        id.incrementAndGet();
        return b;
    }
    public static Block register(Block b, boolean movement, Color color){
        b.id=id.get();
        b.solid =movement;
        b.color=color;
        blocks_id.put(b.id,b);
        id.incrementAndGet();
        return b;
    }
}
