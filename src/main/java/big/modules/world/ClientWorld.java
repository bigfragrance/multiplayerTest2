package big.modules.world;

import big.engine.math.BlockPos;
import big.engine.math.Box;
import big.modules.network.packet.c2s.WantChunkC2SPacket;
import big.modules.world.blocks.Block;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class ClientWorld extends World{
    public void render(Graphics g){
        int s= (int) (10*(cs.player==null?1:cs.player.getFov()));
        for(int x=-s;x<=s;x++){
            for(int y=-s;y<=s;y++){
                BlockPos pos=cs.player.getBlockPos().add(x,y);
                BlockState b=cs.world.getBlockState(pos);
                if(b!=null){
                    b.getBlock().render(g,b,pos.x,pos.y);
                }
            }
        }
        sc.storeAndSetDef();
        int s2=s*6;
        for(int x=-s2;x<=s2;x++){
            for(int y=-s2;y<=s2;y++){
                BlockPos pos=cs.player.getBlockPos().add(x,y);
                BlockState b=cs.world.getBlockState(pos);
                if(b!=null){
                    b.getBlock().renderMini(g,b,pos.x,pos.y);
                }
            }
        }
        sc.restoreZoom();
    }
    public Chunk getChunk(int x,int y,boolean create){
        long l=ChunkPos.toLong(x,y);
        if(!worldChunks.containsKey(l)){
            if(create){
                cs.networkHandler.sendPacket(new WantChunkC2SPacket(x,y));
                worldChunks.put(l,new Chunk());
            }else{
                return null;
            }
        }
        return worldChunks.get(ChunkPos.toLong(x,y));
    }
    public void renderBackground(Graphics g){
        int playerX=cs.player.getBlockPos().x;
        int playerY=cs.player.getBlockPos().y;
        if(cs.player!=null){
            for(int x=-10;x<=10;x++){
                for(int y=-10;y<=10;y++){
                    if((x+y+playerX+playerY)%3==0) {
                        Block.render(g, new Box(x+playerX,y+playerY),Blocks.AIR.color);
                    }
                }
            }
        }
    }
}
