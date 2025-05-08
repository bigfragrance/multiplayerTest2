package engine.modules;


import engine.math.BlockPos2d;
import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.Setting;
import engine.math.util.Util;
import engine.render.Screen;
import modules.ChunkMap;
import modules.client.ClientNetwork;
import modules.entity.ClientPlayerEntity;
import modules.entity.Entity;
import modules.entity.PlayerEntity;
import modules.entity.PolygonEntity;
import modules.network.ClientNetworkHandler;
import modules.network.ServerNetworkHandler;
import modules.particle.GroundParticle;
import modules.particle.Particle;
import server.MultiClientHandler;
import server.ServerMain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static engine.math.util.Util.round;
import static java.lang.Math.floor;

public class EngineMain implements Runnable{
    public static volatile EngineMain cs;
    public HashMap<Long, Entity> entities=new HashMap<>();
    public HashMap<Long,Entity> addingEntities=new HashMap<>();
    public ArrayList<Particle> particles=new ArrayList<>();
    public ArrayList<Particle> groundParticles=new ArrayList<>();
    public volatile AtomicLong lastEntityID=new AtomicLong(0);
    public static double TPS=10;
    public static double chunkSize=100;
    public boolean isServer=true;
    public ClientNetworkHandler networkHandler;
    public MultiClientHandler multiClientHandler=null;
    public static int maxTeams=2;
    public ClientPlayerEntity player;
    public ChunkMap chunkMap=new ChunkMap();
    public static long lastTick=0;
    public Vec2d camPos=new Vec2d(0,0);
    public Vec2d prevCamPos=new Vec2d(0,0);
    public Box borderBox=new Box(new Vec2d(0,0),500,500);
    public boolean ticking=false;
    public Setting setting=null;
    public int polygonSpawnTimer=0;
    public EngineMain(String ip,int port,boolean isServer){
        cs=this;
        this.isServer=isServer;
        if(isServer){
            Screen.INSTANCE.zoom=0.3;
            try {
                new Thread(()->{
                    try {
                        ServerMain.main(new String[]{});
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).start();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            multiClientHandler=new MultiClientHandler();
        }else {
            try {
                networkHandler = new ClientNetworkHandler(ip, port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void run() {
        while (true) {
            lastTick= System.currentTimeMillis();
            long start=System.currentTimeMillis();
            try {
                update();
                lastTick= System.currentTimeMillis();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            try {
                long s= (long) (-(System.currentTimeMillis() - start) +1000 / TPS);
                if(s>0)Thread.sleep(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static double getTickDelta(){
        return  Math.max(0,Math.min(TPS*((double)(System.currentTimeMillis() - lastTick)) / 1000d,1));
    }
    public void clientUpdate(double time){
        if(player!=null){
            player.updateBullet(time);
        }
    }
    public void update(){
        List<Long> toRemove=new ArrayList<>();
        for(Entity entity:entities.values()){
            if(entity.killed()){
                toRemove.add(entity.id);
            }
        }
        for(Long id:toRemove){
            removeEntity(id);
        }

        if(isServer){
            spawnPolygons();
            for(Long id:addingEntities.keySet()){
                entities.put(id,addingEntities.get(id));
            }
            addingEntities.clear();
            updateEntityChunk();
            int coreCount = Runtime.getRuntime().availableProcessors();
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(coreCount  * 2, 32));

            List<Future<?>> futures = new ArrayList<>();
            for (Entity entity : entities.values())  {
                futures.add(executor.submit(()  -> {
                    try {
                        entity.tick();
                    } catch (Exception e) {
                    }
                }));
            }

            try {
                for (Future<?> f : futures) {
                    f.get(500,  TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                // 超时/执行异常处理
            } finally {
                executor.shutdownNow();
            }

            for(Entity entity:entities.values()){
                multiClientHandler.clients.forEach(c->c.serverNetworkHandler.sendEntityUpdate(entity));
            }
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.checkDeath());
        }else{
            updateEntityChunk();
            for(Entity entity:entities.values()){
                entity.tick();
            }
            if(networkHandler!=null) {
                networkHandler.sendKeepAlive();
            }
            if(cs.player!=null){
                prevCamPos.set(camPos);
                Vec2d sub=player.position.subtract(camPos);
                camPos=camPos.add(sub.multiply(0.2));
            }
            generateGroundBlocks(false);
        }

        Screen.lastKeyPressed= (HashMap<Character, Boolean>) Screen.keyPressed.clone();
    }

    private long lastGenerateGround=0;
    public void generateGroundBlocks(boolean full){
        groundParticles.forEach(p->p.update());
        groundParticles.removeIf(p->!p.isAlive);
        particles.forEach(Particle::update);
        particles.removeIf(p->!p.isAlive);
        PlayerEntity player=this.player==null?new PlayerEntity(new Vec2d(0,0)):this.player;
        double zoomPlus=32;//Screen.INSTANCE.zoom*Screen.INSTANCE.zoom;
        if(full){
            for(int i=0;i<40*zoomPlus;i++){
                Vec2d vec=new Vec2d(Util.random((double) -Screen.INSTANCE.windowWidth /2-100, (double) Screen.INSTANCE.windowWidth /2+100),Util.random((double) -Screen.INSTANCE.windowHeight /2-100, (double) Screen.INSTANCE.windowHeight /2+100));
                double size=Util.random(15,25);
                vec=vec.add(Screen.INSTANCE.camX,Screen.INSTANCE.camY);
                GroundParticle ground=new GroundParticle(vec);
                groundParticles.add(ground);
                particles.add(ground);
            }
        }
        else{
            if(groundParticles.size()>=200*zoomPlus||System.currentTimeMillis()-lastGenerateGround<16*(10/player.velocity.length())) return;
            for(int i=1;i<2*zoomPlus;i++){
                Vec2d vec=new Vec2d(Util.random((double) -Screen.INSTANCE.windowWidth /2-100, (double) Screen.INSTANCE.windowWidth /2+100),Util.random((double) -Screen.INSTANCE.windowHeight /2-100, (double) Screen.INSTANCE.windowHeight /2+100));
                double size=Util.random(15,25);
                vec=vec.add(Screen.INSTANCE.camX,Screen.INSTANCE.camY).add(player.velocity.multiply(3));
                GroundParticle ground=new GroundParticle(vec);
                if(!ground.shouldKill(true)) continue;
                groundParticles.add(ground);
                particles.add(ground);
                if(groundParticles.size()>=200*zoomPlus) break;
            }
            lastGenerateGround=System.currentTimeMillis();
        }
    }
    public int getTeam(){
        HashMap<Integer,Integer> team=new HashMap<>();

        for(int i=0;i<maxTeams;i++){
            team.put(i,0);
        }

        for(Entity entity:entities.values()){
            if(!(entity instanceof PlayerEntity)) continue;
            team.put(entity.team,team.getOrDefault(entity.team,0)+1);
        }
        int min=10000;
        int minTeam=0;
        for(int i=0;i<maxTeams;i++){
            if(team.get(i)<min){
                min=team.get(i);
                minTeam=i;
            }
        }
        return minTeam;
    }
    public void updateEntityChunk(){
        chunkMap.clear();
        for(Entity entity:entities.values()){
            entity.updateChunk();
        }
    }
    public void spawnPolygons(){
        int count=getPolygonCount();
        if(count>100) return;
        if(polygonSpawnTimer<=0){
            for(int i=0;i<10;i++) {
                polygonSpawnTimer = round(Util.random(5,8));
                double s = Math.pow(Util.random(0,1),2)*9;
                double t = Math.pow(Math.random(),6)*4;
                int sides = (round(s) + 3);
                int type = round(t);
                Vec2d pos = Util.randomInBox(borderBox);
                addEntity(new PolygonEntity(pos, sides, type));
            }
        }else{
            polygonSpawnTimer--;
        }
    }
    private int getPolygonCount(){
        int count=0;
        for(Entity entity:entities.values()){
            if(entity instanceof PolygonEntity) count++;
        }
        return count;
    }
    public Vec2d getCamPos(){
        return Util.lerp(prevCamPos,camPos,getTickDelta());
    }
    public void addEntity(Entity entity){
        if(isServer){
            entity.id=lastEntityID.get();
            addingEntities.put(entity.id,entity);
            lastEntityID.incrementAndGet();
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.sendEntitySpawn(entity));
        }
        else{
            entities.put(entity.id,entity);
        }
    }
    public void removeEntity(Entity entity){
        removeEntity(entity.id);
    }
    public void removeEntity(Long id){
        entities.remove(id);
        if(isServer){
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.sendEntityRemove(id));
        }
    }
}
