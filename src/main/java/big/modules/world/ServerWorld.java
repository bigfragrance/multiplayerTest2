package big.modules.world;

import big.engine.math.BlockPos;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.ColorUtils;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.Util;
import big.engine.math.util.timer.IntTimer;
import big.engine.render.Screen;
import big.modules.entity.Entity;
import big.modules.entity.MobEntity;
import big.modules.entity.PolygonEntity;
import big.modules.entity.boss.VisitorEntity;
import big.modules.entity.player.ServerBotEntity;
import big.modules.network.packet.s2c.BlockStateUpdateS2CPacket;
import big.modules.world.blocks.Block;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static big.engine.math.util.Util.random;
import static big.engine.math.util.Util.round;
import static big.engine.modules.EngineMain.cs;
import static big.modules.world.Chunk.CHUNK_SIZE;

public class ServerWorld extends World{
    public static int randomTickSpeed=80;
    public static int botsCount=2;
    private IntTimer mobSpawnTimer=new IntTimer(50);
    private IntTimer botSpawnTimer=new IntTimer(1);
    private IntTimer visitorSpawnTimer=new IntTimer(10);
    private IntTimer waveTimer=new IntTimer(100);
    private int waitedSpawn=0;
    private Vec2d visitorSpawningPosition=null;
    private boolean waveStarted=false;
    private int currentWave=0;
    public ServerWorld(){
        super();
        visitorSpawnTimer.reset();
    }
    public void tick(){
        spawnMobs();
        spawnBot();
        updateVisitorSpawn();
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
    public void updateVisitorSpawnSiege(){
        visitorSpawnTimer.update();
        waveTimer.update();
        if(!waveTimer.passed()) return;

        if(waveStarted&&getVisitorCount()<1){
            cs.sendMessageServer("Wave "+currentWave+" finished!");
            waveTimer.reset();
            currentWave++;
            waveStarted=false;
            return;
        }
        if(waveStarted) return;
        if(getVisitorCount()>=currentWave+1) {
            waveStarted=true;
            cs.sendMessageServer("Wave "+currentWave+" started!");
            return;
        }
        Vec2d pos=EntityUtils.getVisitorSpawnPosition();
        if(pos==null) return;
        cs.addEntity(new VisitorEntity(pos,Util.floor(currentWave/3d)));
    }
    public void updateVisitorSpawn(){
        visitorSpawnTimer.update();
        if(!visitorSpawnTimer.passed()) return;
        if(getVisitorCount()>=1) return;
        if(Math.random()>0.05&&visitorSpawningPosition==null) {
            visitorSpawnTimer.reset();
            return;
        }
        if(visitorSpawningPosition==null)visitorSpawningPosition= EntityUtils.getVisitorSpawnPosition();
        List<Entity> tokill=new ArrayList<>();
        for(Entity e:cs.entities.values()){
            if(e instanceof PolygonEntity){
                Vec2d sub=e.position.subtract(visitorSpawningPosition);
                double l=sub.length();
                if(l>60) continue;
                if(l<2) tokill.add(e);
                double a=Util.lerp(0,90,Math.pow(1-l/60,10));
                Vec2d vel=sub.limit(1).multiply(-0.5/l).limitOnlyOver(0.5).rotate(a);
                e.velocity.offset(vel);
            }
        }
        waitedSpawn++;
        if(waitedSpawn==50){
            cs.sendMessageServer("A visitor is coming.");
        }
        if(waitedSpawn>=1000){
            for(Entity e:tokill){
                e.kill();
            }
            cs.addEntity(new VisitorEntity(visitorSpawningPosition,Util.floor(currentWave/10d)));
            cs.sendMessageServer("visitor has arrived.");
            visitorSpawningPosition=null;
            waitedSpawn=0;
            visitorSpawnTimer.reset();
        }
    }
    private int getVisitorCount(){
        int count=0;
        for(Entity e:cs.entities.values()){
            if(e instanceof VisitorEntity) count++;
        }
        return count;
    }
    public void spawnBot(){
        botSpawnTimer.update();
        if(!botSpawnTimer.passed()) return;
        botSpawnTimer.reset();
        int count=0;
        for(Entity e:cs.entities.values()){
            if(e instanceof ServerBotEntity) count++;
        }
        if(count>=botsCount) return;
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
