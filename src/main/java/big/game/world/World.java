package big.game.world;

import big.engine.math.Vec2i;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.util.SegmentBoxIntersectionChecker;
import big.engine.util.Util;
import big.events.TickEvent;
import big.game.entity.Entity;
import meteordevelopment.orbit.EventHandler;
import org.json.JSONObject;


import java.awt.*;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static big.engine.modules.EngineMain.cs;

public class World {
    public static boolean gravityEnabled=false;
    public static double gravity=-0.05;

    public ConcurrentHashMap<Long,Chunk> worldChunks=new  ConcurrentHashMap<>();
    public Chunk getChunk(int x,int y){
        return getChunk(x,y,true);
    }
    public Chunk getChunk(int x,int y,boolean create){
        long l=ChunkPos.toLong(x,y);
        if(!worldChunks.containsKey(l)){
            if(create){
                worldChunks.put(l,new Chunk());
            }else{
                return null;
            }
        }
        return worldChunks.get(ChunkPos.toLong(x,y));
    }
    public boolean isChunkLoaded(int x,int y){
        return worldChunks.containsKey(ChunkPos.toLong(x,y));
    }
    public BlockState getBlockState(int x,int y){
        int xc=Chunk.toChunkPos(x);
        int yc=Chunk.toChunkPos(y);
        Chunk chunk=getChunk(Chunk.toChunk(x),Chunk.toChunk(y));
        return chunk.getBlockState(xc,yc);
    }
    public BlockState getBlockState(Vec2i pos){
        return getBlockState(pos.x,pos.y);
    }
    public void setBlockState(int x,int y,BlockState blockState){
        int xc=Chunk.toChunkPos(x);
        int yc=Chunk.toChunkPos(y);
        Chunk chunk=getChunk(Chunk.toChunk(x),Chunk.toChunk(y));
        chunk.chunkBlocks[xc][yc]=blockState;
    }
    public Block getBlock(int x, int y){
        return getBlockState(x,y).getBlock();
    }
    public Block getBlock(Vec2i pos){
        return getBlockState(pos.x,pos.y).getBlock();
    }
    public JSONObject toJSON(){
        JSONObject json=new JSONObject();
        for(Long l:worldChunks.keySet()){
            json.put(String.valueOf(l),worldChunks.get(l).toJSON());
        }
        return json;
    }
    public void fromJSON(JSONObject json){
        for(String s:json.keySet()){
            worldChunks.put(Long.parseLong(s),Chunk.fromJSON(json.getJSONObject(s)));
        }
    }
    public Collection<Entity> getEntities(){
        return cs.entities.values();
    }
    public Collection<Entity> getEntities(Box section){
        return getEntities().stream().filter(p->section.intersects(p.boundingBox)).collect(Collectors.toList());
    }
    public Entity getEntity(long id){
        return Util.secondIfNull(cs.entities.get(id),cs.addingEntities.get(id));
    }
    public boolean raycast(Vec2d start,Vec2d end){
        Box b=new Box(start,end);
        for(int x = Util.floor(b.minX);x<=Util.floor(b.maxX);x++){
            for(int y = Util.floor(b.minY);y<=Util.floor(b.maxY);y++){
                if(!getBlock(x,y).solid) continue;
                if(SegmentBoxIntersectionChecker.segmentIntersectsBox(start,end,new Box(x,y))){
                    return true;
                }
            }
        }
        return false;
    }
    public void renderBackground(Graphics g){

    }
    @EventHandler
    public void onTick(TickEvent event){
        if(event.isPost()) return;
        tick();
    }
    public void tick(){

    }
    public void render(Graphics g){

    }
}
