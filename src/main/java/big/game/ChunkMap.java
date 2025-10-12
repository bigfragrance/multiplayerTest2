package big.game;

import big.engine.math.Vec2i;
import big.game.entity.Entity;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkMap {
    public ConcurrentHashMap<Long, ArrayList<Entity>> chunks;
    public ChunkMap(){
        chunks=new ConcurrentHashMap<>();
    }
    public ArrayList<Entity> getChunk(Vec2i pos){
        return chunks.getOrDefault(pos.toLong(),new ArrayList<>());
    }
    public void addEntity(Entity e, Vec2i pos){
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
