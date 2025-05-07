package modules;

import engine.math.BlockPos2d;
import modules.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;

public class ChunkMap {
    public HashMap<BlockPos2d, ArrayList<Entity>> chunks;
    public ArrayList<BlockPos2d> created=new ArrayList<>();
    public ChunkMap(){
        chunks=new HashMap<>();
    }
    public ArrayList<Entity> getChunk(BlockPos2d pos){
        return chunks.getOrDefault(pos,new ArrayList<>());
    }
    public void addEntity(Entity e,BlockPos2d pos){
        ArrayList<Entity> chunk=chunks.getOrDefault(pos,new ArrayList<>());
        chunk.add(e);
        if(!chunks.containsKey(pos)){
            chunks.put(pos,chunk);
        }
    }
    public void clear(){
        for(ArrayList<Entity> e:chunks.values()){
            e.clear();
        }
    }
    public BlockPos2d blockPos(BlockPos2d pos){
        for(BlockPos2d b:created){
            if(b.equals(pos)){
                return b;
            }
        }
        created.add(pos);
        return pos;
    }
}
