package modules.world;

import engine.math.BlockPos;
import engine.math.Box;
import modules.entity.Entity;
import org.json.JSONObject;


import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static engine.modules.EngineMain.cs;

public class World {
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
    public BlockState getBlockState(BlockPos pos){
        return getBlockState(pos.x,pos.y);
    }
    public void setBlockState(int x,int y,BlockState blockState){
        int xc=Chunk.toChunkPos(x);
        int yc=Chunk.toChunkPos(y);
        Chunk chunk=getChunk(Chunk.toChunk(x),Chunk.toChunk(y));
        chunk.chunkBlocks[xc][yc]=blockState;
    }
    public Block getBlock(int x,int y){
        return getBlockState(x,y).getBlock();
    }
    public Block getBlock(BlockPos pos){
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
    public void renderBackground(Graphics g){

    }
    public void tick(){

    }
    public void render(Graphics g){

    }
}
