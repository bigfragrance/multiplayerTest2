package big.game.world;

import big.engine.math.BlockPos;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.ColorUtils;
import big.engine.math.util.EntityUtils;
import big.engine.math.util.Util;
import big.engine.math.util.timer.IntTimer;
import big.engine.modules.EngineMain;
import big.engine.render.Screen;
import big.game.entity.Entity;
import big.game.entity.MobEntity;
import big.game.entity.PolygonEntity;
import big.game.entity.boss.VisitorEntity;
import big.game.entity.player.ServerBotEntity;
import big.game.network.packet.s2c.BlockStateUpdateS2CPacket;
import big.game.network.packet.s2c.TickS2CPacket;
import big.game.world.blocks.Block;
import big.server.ClientHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static big.engine.math.util.Util.random;
import static big.engine.math.util.Util.round;
import static big.engine.modules.EngineMain.cs;
import static big.game.world.Chunk.CHUNK_SIZE;

public class ServerWorld extends World{
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
        if(!cs.setting.isSiege()){
            updateVisitorSpawn();
        }
        else {
            updateVisitorSpawnSiege();
        }
        randomTicks();
        EngineMain.maxTeams=cs.setting.getMaxTeam();
        EngineMain.damageExchangeSpeed=cs.setting.getDamageExchangeSpeed();
        visitorSpawnTimer.setDelay(cs.setting.getVisitorSpawnDelay());

        updateEntity();
    }
    public void updateEntity(){
        cs.serverController.update();
        for(Long id:cs.addingEntities.keySet()){
            cs.entities.put(id,cs.addingEntities.get(id));
        }
        cs.addingEntities.clear();
        cs.updateEntityChunk();
        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(coreCount  * 2, 32));

        List<Future<?>> futures = new ArrayList<>();
        for (Entity entity : cs.entities.values())  {
            futures.add(executor.submit(()  -> {
                try {
                    entity.tick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        try {
            for (Future<?> f : futures) {
                f.get(500,  TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
        for(Entity entity:cs.entities.values()){
            entity.addScoreAdd();
        }
        cs.sendEntitiesUpdate();
        for(int i=0;i<cs.multiClientHandler.clients.size();i++){
            ClientHandler c=cs.multiClientHandler.clients.get(i);
            c.serverNetworkHandler.checkDeath();
            //c.checkConnecting();
            c.send(new TickS2CPacket(System.currentTimeMillis()));
        }
    }
    public void randomTicks(){
        for(long l: worldChunks.keySet()){
            Chunk chunk=worldChunks.get(l);
            for(int i=0;i<cs.setting.getRandomTickSpeed();i++){
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
        if(getVisitorCount()>=cs.setting.getMaxVisitor()) return;
        if(Math.random()>cs.setting.getVisitorSpawnPossibility()&&visitorSpawningPosition==null) {
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
        if(waitedSpawn==1){
            cs.sendMessageServer("A visitor is coming.");
        }
        if(waitedSpawn>=cs.setting.getVisitorSpawnTime()){
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
        if(count>=cs.setting.getBotCount()) return;
        Vec2d pos= EntityUtils.getRandomSpawnPosition(cs.getTeam());
        ServerBotEntity player=new ServerBotEntity(pos,cs.getTeam());
        cs.addEntity(player);
    }
    public void spawnMobs(){
        mobSpawnTimer.update();
        if(!mobSpawnTimer.passed()) return;
        int count=0;
        for(Entity e:cs.entities.values()){
            if(e instanceof MobEntity) count++;
        }
        if(count>=cs.setting.getMaxPolygon()) return;
        double pow=cs.setting.getPolygonRandomPow();
        double typeMax=cs.setting.getPolygonType();
        for(int i=0;i<500;i++){
            Vec2d pos= Util.randomInBox(cs.borderBox);
            BlockPos blockPos= BlockPos.ofFloor(pos);
            BlockState blockState=cs.world.getBlockState(blockPos);
            if(blockState.getBlock().solid) continue;
            double s = blockState.getSpawnMobRarity()*7+Math.random()*1.6-0.8;
            double t = Math.pow(Math.random(),pow)*(typeMax-0.2);
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
