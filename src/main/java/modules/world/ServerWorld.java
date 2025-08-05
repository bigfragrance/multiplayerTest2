package modules.world;

import engine.math.BlockPos;
import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.ColorUtils;
import engine.math.util.EntityUtils;
import engine.math.util.Util;
import engine.math.util.timer.IntTimer;
import engine.render.Screen;
import modules.entity.Entity;
import modules.entity.MobEntity;
import modules.entity.PolygonEntity;
import modules.entity.player.ServerBotEntity;
import modules.network.packet.s2c.BlockStateUpdateS2CPacket;

import java.awt.*;

import static engine.math.util.Util.random;
import static engine.math.util.Util.round;
import static engine.modules.EngineMain.cs;
import static modules.world.Chunk.CHUNK_SIZE;

public class ServerWorld extends World{
    public static int randomTickSpeed=80;
    private IntTimer mobSpawnTimer=new IntTimer(50);
    private IntTimer botSpawnTimer=new IntTimer(1);
    public void tick(){
        spawnMobs();
        spawnBot();
        randomTicks();
    }
    public void randomTicks(){
        for(long l: worldChunks.keySet()){
            Chunk chunk=worldChunks.get(l);
            for(int i=0;i<randomTickSpeed;i++){
                int x=random.nextInt(CHUNK_SIZE);
                int y=random.nextInt(CHUNK_SIZE);
                int[] chunkPos=ChunkPos.fromLong(l);
                BlockState state=chunk.getBlockState(x,y);
                state.getBlock().tick(state,x+chunkPos[0]*16,y+chunkPos[1]*16);
            }
        }
    }
    public void spawnBot(){
        botSpawnTimer.update();
        if(!botSpawnTimer.passed()) return;
        botSpawnTimer.reset();
        int count=0;
        for(Entity e:cs.entities.values()){
            if(e instanceof ServerBotEntity) count++;
        }
        if(count>=2) return;
        Vec2d pos= EntityUtils.getRandomSpawnPosition(cs.getTeam());
        ServerBotEntity player=new ServerBotEntity(pos);
        player.team=cs.getTeam();
        cs.addEntity(player);
    }
    public void spawnMobs(){
        mobSpawnTimer.update();
        if(!mobSpawnTimer.passed()) return;
        int count=0;
        for(Entity e:cs.entities.values()){
            if(e instanceof MobEntity) count++;
        }
        if(count>=600) return;
        for(int i=0;i<500;i++){
            Vec2d pos= Util.randomInBox(cs.borderBox);
            BlockPos blockPos= BlockPos.ofFloor(pos);
            BlockState blockState=cs.world.getBlockState(blockPos);
            if(blockState.getBlock().solid) continue;
            double s = blockState.getSpawnMobRarity()*7+Math.random()*1.6-0.8;
            double t = Math.pow(Math.random(),4)*2.5;
            int sides = (round(s) + 3);
            int type = round(t);
            if(Math.random()<blockState.getSpawnMobRarity()){
                cs.addEntity(new PolygonEntity(blockPos.toCenterPos(), sides, type));
                count++;
                if(count>=100) return;
            }
        }
        mobSpawnTimer.reset();
    }
    public void setBlockState(int x,int y,BlockState blockState){
        super.setBlockState(x,y,blockState);
        cs.multiClientHandler.clients.forEach(c->c.send(new BlockStateUpdateS2CPacket(x,y,blockState).toJSON()));
    }
    public void render(Graphics g){
        for(int x=-60;x<=60;x++){
            for(int y=-60;y<=60;y++){
                BlockPos pos=new BlockPos(x,y);
                BlockState b=cs.world.getBlockState(pos);
                if(b!=null){
                    b.getBlock().render(g,b,pos.x,pos.y);
                    if(Screen.sc.inputManager.isRenderingMobRarity()){
                        Block.render(g,new Box(new BlockPos(x,y)), ColorUtils.setAlpha(ColorUtils.getRainbowColor(b.getSpawnMobRarity()),50));
                    }
                }
            }
        }
    }
}
