package modules;

import engine.math.BlockPos;
import modules.entity.Entity;
import modules.world.ChunkPos;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkMap {
    public ConcurrentHashMap<Long, ArrayList<Entity>> chunks;
    public ChunkMap(){
        chunks=new ConcurrentHashMap<>();
    }
    public ArrayList<Entity> getChunk(BlockPos pos){
        return chunks.getOrDefault(pos.toLong(),new ArrayList<>());
    }
    public void addEntity(Entity e,BlockPos pos){
        ArrayList<Entity> chunk=chunks.getOrDefault(pos.toLong(),new ArrayList<>());
        chunk.add(e);
        if(!chunks.containsKey(pos.toLong())){
            chunks.put(pos.toLong(),chunk);
        }
    }
    public void clear(){
        for(ArrayList<Entity> e:chunks.values()){
            e.clear();
        }
    }
}
