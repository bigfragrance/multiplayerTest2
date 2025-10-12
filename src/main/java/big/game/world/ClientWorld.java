package big.game.world;

import big.engine.math.Vec2i;
import big.engine.math.Box;
import big.game.entity.Entity;
import big.game.network.packet.c2s.WantChunkC2SPacket;

import java.awt.*;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class ClientWorld extends World{
    public void render(Graphics g){
        if(cs.player==null) return;
        int s= (int) (10*(cs.player==null?1:cs.player.getFov()));
        for(int x=-s;x<=s;x++){
            for(int y=-s;y<=s;y++){
                Vec2i pos=cs.player.getBlockPos().add(x,y);
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
                Vec2i pos=cs.player.getBlockPos().add(x,y);
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
        if(cs.player==null) return;
        int playerX=cs.player.getBlockPos().x;
        int playerY=cs.player.getBlockPos().y;
        if(cs.player!=null){
            for(int x=-10;x<=10;x++){
                for(int y=-10;y<=10;y++){
                    if((x+y+playerX+playerY)%2==0) {
                        Block.render(g, new Box(x+playerX,y+playerY),Blocks.AIR.color);
                    }
                }
            }
        }
    }
    public void tick(){
        cs.updateEntityChunk();
        for(Entity entity:cs.entities.values()){
            entity.tick();
        }
        if(cs.networkHandler!=null) {
            cs.networkHandler.sendKeepAlive();
        }
    }
}
