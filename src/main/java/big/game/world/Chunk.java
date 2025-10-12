package big.game.world;

import big.engine.math.Vec2i;
import big.engine.math.Box;
import big.engine.util.ColorUtils;
import big.engine.render.Screen;
import org.json.JSONObject;

import java.awt.*;

public class Chunk {
    public static int CHUNK_SIZE=16;
    public static int CHUNK_TO=15;
    public BlockState[][] chunkBlocks;
    public Chunk(){
        chunkBlocks=new BlockState[CHUNK_SIZE][CHUNK_SIZE];
        create();
    }
    public BlockState getBlockState(int x,int y){
        return chunkBlocks[x][y];
    }
    public void create(){
        for(int x=0;x<CHUNK_SIZE;x++){
            for(int y=0;y<CHUNK_SIZE;y++){
                chunkBlocks[x][y]=new BlockState(Blocks.AIR);
            }
        }
    }
    public static int toChunk(int i){
        return i>>4;
    }
    public static int toChunkPos(int i){
        return (i-toChunk(i)*CHUNK_SIZE+CHUNK_SIZE)%CHUNK_SIZE;
    }
    public void render(Graphics g,int chunkX,int chunkY){
        for(int x=0;x<CHUNK_SIZE;x++){
            for(int y=0;y<CHUNK_SIZE;y++){
                chunkBlocks[x][y].getBlock().render(g,chunkBlocks[x][y],x+chunkX*CHUNK_SIZE,y+chunkY*CHUNK_SIZE);
            }
        }
        if(Screen.sc.inputManager.isRenderingMobRarity()){
            for(int x=0;x<CHUNK_SIZE;x++){
                for(int y=0;y<CHUNK_SIZE;y++){
                    Block.render(g,new Box(new Vec2i(x+chunkX*CHUNK_SIZE,y+chunkY*CHUNK_SIZE)), ColorUtils.setAlpha(ColorUtils.getRainbowColor(chunkBlocks[x][y].getSpawnMobRarity()),50));
                }
            }
        }

    }
    public static Chunk fromJSON(JSONObject json){
        Chunk chunk=new Chunk();
        for(int x=0;x<CHUNK_SIZE;x++){
            for(int y=0;y<CHUNK_SIZE;y++){
                chunk.chunkBlocks[x][y]=BlockState.fromJSON(json.getJSONObject(String.valueOf(x*16+y)));
            }
        }
        return chunk;
    }

    public JSONObject toJSON(){
        JSONObject json=new JSONObject();
        for(int x=0;x<CHUNK_SIZE;x++){
            for(int y=0;y<CHUNK_SIZE;y++){
                json.put(String.valueOf(x*16+y),chunkBlocks[x][y].toJSON());
            }
        }
        return json;
    }
    public JSONObject toClientJSON(){
        JSONObject json=new JSONObject();
        for(int x=0;x<CHUNK_SIZE;x++){
            for(int y=0;y<CHUNK_SIZE;y++){
                json.put(String.valueOf(x*16+y),chunkBlocks[x][y].toClientJSON());
            }
        }
        return json;
    }
    public int[] fromPacked(int packed){
        int[] pos=new int[2];
        pos[0]=packed>>4;
        pos[1]=packed&CHUNK_TO;
        return pos;
    }
}
