package big.engine.modules;


import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.util.Setting;
import big.engine.util.AvgCounter;
import big.engine.util.Util;
import big.engine.render.Screen;
import big.events.TickEvent;
import big.game.entity.RockEntity;
import big.game.network.packet.s2c.MessageS2CPacket;
import big.game.screen.DebugScreen;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import big.game.ChunkMap;
import big.game.entity.BlockEntity;
import big.game.entity.player.ClientPlayerEntity;
import big.game.entity.Entity;
import big.game.entity.player.PlayerEntity;
import big.game.entity.PolygonEntity;
import big.game.network.ClientNetworkHandler;
import big.game.particle.Particle;
import big.game.screen.ChatMessageScreen;
import big.game.server.ServerController;
import big.game.weapon.GunList;
import big.game.world.ClientWorld;
import big.game.world.ServerWorld;
import big.game.world.World;
import big.server.MultiClientHandler;
import big.server.ServerMain;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static big.engine.util.Util.round;
import static big.engine.render.Screen.sc;

public class EngineMain implements Runnable{
    public static double TPS=20;
    public static double chunkSize=8;
    public static double damageExchangeSpeed=1;
    public static int maxTeams=2;

    public static volatile EngineMain cs;
    private long lastGc=System.currentTimeMillis();
    public Map<Long, Entity> entities=new ConcurrentHashMap<>();
    public ArrayList<RockEntity > rocks=new ArrayList<>();
    public Map<Long,Entity> addingEntities=new ConcurrentHashMap<>();
    public ArrayList<BlockEntity> groundBlocks=new ArrayList<>();
    public ArrayList<Particle> particles=new ArrayList<>();
    public ArrayList<Entity> entityParticles =new ArrayList<>();
    public volatile AtomicLong lastEntityID=new AtomicLong(0);
    public boolean isServer=true;
    public ClientNetworkHandler networkHandler;
    public MultiClientHandler multiClientHandler=null;
    public long runTime=0;
    public ClientPlayerEntity player;
    public ChunkMap chunkMap=new ChunkMap();
    public static long lastTick=0;
    public Vec2d camPos=new Vec2d(0,0);
    public Vec2d prevCamPos=new Vec2d(0,0);
    public Box borderBox=new Box(new Vec2d(0,0),100,100);
    public ServerController serverController;
    public boolean ticking=false;
    public Setting setting=null;
    public World world;
    public IEventBus EVENT_BUS=new EventBus();
    public AvgCounter currentTPS=new AvgCounter(50);
    public long nspt=0;
    public EngineMain(String ip,int port,boolean isServer){
        cs=this;
        this.isServer=isServer;
        initEventBus();
        ChatMessageScreen.init();
        DebugScreen.init();
        if(isServer){
            GunList.init();
            world=new ServerWorld();
            serverController=new ServerController();
            EVENT_BUS.subscribe(serverController);
            sc.zoom=10/ sc.zoom2;
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
            TPS+=0.01;
            world=new ClientWorld();
            try {
                networkHandler = new ClientNetworkHandler(ip, port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        EVENT_BUS.subscribe(world);
        EVENT_BUS.subscribe(sc);


    }
    private void initEventBus(){
        EVENT_BUS.registerLambdaFactory("big",(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        EVENT_BUS.subscribe(this);
    }
    @Override
    public void run() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            lastTick = System.currentTimeMillis();
            long start = System.nanoTime();
            try {
                sc.renderTasks2.update(50);
                ticking = true;
                update();
                runTime++;
                EVENT_BUS.post(TickEvent.get(1));
                ticking = false;
                if (System.currentTimeMillis() - sc.lastRender >= 200) {
                    sc.renderTasks.clear();
                }
                if(System.currentTimeMillis()-lastGc>10000){
                    System.gc();
                    lastGc=System.currentTimeMillis();
                }
                nspt=System.nanoTime() - start;
                currentTPS.add(Math.min(TPS,1000000000d/nspt));
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*try {
                int target= (int) (1000000000d/TPS);
                int nspt=(int) (System.nanoTime() - start);
                int s=target-nspt;
                long m=Util.floor(s/1000000d);
                s-=(int) (m*1000000d);
                if (s > 0&&m>0) Thread.sleep(m, s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        };

        scheduler.scheduleAtFixedRate(task, 0, (long)Math.floor(1000000000/TPS), TimeUnit.NANOSECONDS);
    }
    public static double getTickDelta(){
        return  Math.max(0,Math.min(TPS*((double)(System.currentTimeMillis() - lastTick)) / 1000d,1));
    }
    public void update(){
        List<Long> toRemove=new ArrayList<>();
        for(Entity entity:entities.values()){
            if(entity.killed()){
                toRemove.add(entity.id);
                spawnParticle(entity);
            }
        }
        for(Long id:toRemove){
            removeEntity(id);
        }
        entityParticles.removeIf(Entity::killed);
        for(Entity o: entityParticles){
            o.tick();
        }
        rocks.removeIf(entity -> !entities.containsValue(entity));
        EVENT_BUS.post(TickEvent.get(0));
        Screen.lastKeyPressed= getCloneKeyPressed();
    }
    public ConcurrentHashMap<Character,Boolean> getCloneKeyPressed(){
        ConcurrentHashMap<Character,Boolean> clone=new ConcurrentHashMap<>();
        clone.putAll(Screen.keyPressed);
        return clone;
    }
    public void updateCamPos(){
        /*if(cs.player!=null){
            prevCamPos.set(camPos);
            Vec2d sub=player.getRenderPosition().subtract(camPos);
            camPos=camPos.add(sub.multiply(0.8));
        }*/
        prevCamPos.set(camPos);
        if(player==null) return;
        camPos.set(player.position);
    }
    public void spawnParticle(Entity e){
        if(isServer||e==null||e instanceof PlayerEntity) return;
        e.isParticle=true;
        e.lifeTime=0;
        e.isAlive=true;
        e.id=-1;
        //e.velocity=e.position.subtract(e.prevPosition);
        addParticle(e);
    }
    public int getTeam(){
         ConcurrentHashMap<Integer,Integer> team=new  ConcurrentHashMap<>();

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
    public void sendEntitiesUpdate(){
        /*JSONObject json=new JSONObject();
        PacketUtil.putPacketType(json,"entity_update");
        JSONObject array=new JSONObject();
        int count=0;
        for(Entity entity:entities.values()){
            array.put(String.valueOf(count),entity.getUpdate());
            count++;
        }
        PacketUtil.put(json,"data",array);
        PacketUtil.put(json,"max",count);
        multiClientHandler.clients.forEach(c->c.serverNetworkHandler.send(json));*/
        for(Entity entity:entities.values()){
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.sendEntityUpdate(entity));
        }
        multiClientHandler.clients.forEach(c->c.serverNetworkHandler.clearTemp());
    }
    public void updateEntityChunk(){
        chunkMap.clear();
        for(Entity entity:entities.values()){
            entity.updateChunk();
        }
    }
    /*public void fastUpdate(double time){
        time=Math.min(time,1);
        for(Entity entity:entities.values()){
            if(entity instanceof ServerPlayerEntity player){
                player.updateBullet(time);
            }
        }
    }*/
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
        if(entity instanceof RockEntity){
            rocks.add((RockEntity) entity);
        }
    }
    public void addEntityBlock(BlockEntity entity){
        if(isServer){
            entity.id=lastEntityID.get();
            addingEntities.put(entity.id,entity);
            lastEntityID.incrementAndGet();
            groundBlocks.add(entity);
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.sendEntitySpawn(entity));
        }
        else{
            entities.put(entity.id,entity);
            groundBlocks.add(entity);
        }
    }
    public void sendMessageServer(String str){
        if(isServer){
            multiClientHandler.clients.forEach(c->c.send(new MessageS2CPacket("<server> "+str).addHistory()));
        }
    }
    public void addParticle(Entity particle){
        entityParticles.add(particle);
    }
    public void removeEntity(Entity entity){
        removeEntity(entity.id);
    }
    public void removeEntity(Long id){
        Entity e=entities.get(id);
        spawnParticle(e);
        entities.remove(id);
        addingEntities.remove(id);
        entities.values().remove(e);
        addingEntities.values().remove(e);
        if(isServer){
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.sendEntityRemove(id));
        }
    }
}
