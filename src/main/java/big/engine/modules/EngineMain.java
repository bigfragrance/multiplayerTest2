package big.engine.modules;


import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.Setting;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.modules.network.packet.s2c.MessageS2CPacket;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import big.modules.ChunkMap;
import big.modules.entity.BlockEntity;
import big.modules.entity.player.ClientPlayerEntity;
import big.modules.entity.Entity;
import big.modules.entity.player.PlayerEntity;
import big.modules.entity.PolygonEntity;
import big.modules.network.ClientNetworkHandler;
import big.modules.particle.GroundParticle;
import big.modules.particle.Particle;
import big.modules.screen.ChatMessageScreen;
import big.modules.server.ServerController;
import big.modules.weapon.GunList;
import big.modules.world.ClientWorld;
import big.modules.world.ServerWorld;
import big.modules.world.World;
import big.server.MultiClientHandler;
import big.server.ServerMain;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static big.engine.math.util.Util.round;
import static big.engine.render.Screen.sc;

public class EngineMain implements Runnable{
    public static String SETTING_PATH="setting.json";
    public static volatile EngineMain cs;
    public Map<Long, Entity> entities=new ConcurrentHashMap<>();
    public Map<Long,Entity> addingEntities=new ConcurrentHashMap<>();
    public ArrayList<BlockEntity> groundBlocks=new ArrayList<>();
    public ArrayList<Particle> particles=new ArrayList<>();
    public ArrayList<Particle> groundParticles=new ArrayList<>();
    public ArrayList<Entity> entityParticles =new ArrayList<>();
    public ArrayList<Entity> entityParticlesAdd =new ArrayList<>();
    public volatile AtomicLong lastEntityID=new AtomicLong(0);
    public static float TPS=20;
    public static float chunkSize=3;
    public boolean isServer=true;
    public ClientNetworkHandler networkHandler;
    public MultiClientHandler multiClientHandler=null;
    public static int maxTeams=2;
    public ClientPlayerEntity player;
    public ChunkMap chunkMap=new ChunkMap();
    public static long lastTick=0;
    public Vec2d camPos=new Vec2d(0,0);
    public Vec2d prevCamPos=new Vec2d(0,0);
    public Box borderBox=new Box(new Vec2d(0,0),60,60);
    public ServerController serverController;
    public boolean ticking=false;
    public Setting setting=null;
    public World world;
    public IEventBus EVENT_BUS=new EventBus();
    public EngineMain(String ip,int port,boolean isServer){
        cs=this;
        this.isServer=isServer;
        initEventBus();
        ChatMessageScreen.init();
        if(isServer){
            GunList.init();
            world=new ServerWorld();
            serverController=new ServerController();
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
            world=new ClientWorld();
            try {
                networkHandler = new ClientNetworkHandler(ip, port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void initEventBus(){
        EVENT_BUS.registerLambdaFactory("big",(lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        EVENT_BUS.subscribe(this);
    }
    @Override
    public void run() {
        while (true) {
            lastTick = System.currentTimeMillis();
            long start = System.currentTimeMillis();
            try {
                sc.renderTasks2.update(50);
                ticking = true;
                update();
                ticking = false;
                lastTick = System.currentTimeMillis();
                if (System.currentTimeMillis() - sc.lastRender >= 200) {
                    sc.renderTasks.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            ticking = false;
            try {
                long s = (long) (-(System.currentTimeMillis() - start) + 1000 / TPS);
                if (s > 0) Thread.sleep(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static float getTickDelta(){
        return  Math.max(0,Math.min(TPS*((float)(System.currentTimeMillis() - lastTick)) / 1000d,1));
    }
    public void clientUpdate(float time){
        if(player!=null){
            //player.updateBullet(time);
        }
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
        entityParticles.removeIf(b->b.killed());
        entityParticles.addAll(entityParticlesAdd);
        entityParticlesAdd.clear();
        for(Entity o: entityParticles){
            o.tick();
        }
        sc.tick();
        this.world.tick();
        if(isServer){
            serverController.update();
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
            for(Entity entity:entities.values()){
                entity.addScoreAdd();
            }
            sendEntitiesUpdate();
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.checkDeath());
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.clientHandler.checkConnecting());
        }else{
            updateEntityChunk();
            for(Entity entity:entities.values()){
                entity.tick();
            }
            if(networkHandler!=null) {
                networkHandler.sendKeepAlive();
            }

            generateGroundBlocks(false);
        }

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
    private long lastGenerateGround=0;
    public void generateGroundBlocks(boolean full){
        groundParticles.forEach(p->p.update());
        groundParticles.removeIf(p->!p.isAlive);
        particles.forEach(Particle::update);
        particles.removeIf(p->!p.isAlive);
        PlayerEntity player=this.player==null?new PlayerEntity(new Vec2d(0,0)):this.player;
        float zoomPlus=32;//Screen.INSTANCE.zoom*Screen.INSTANCE.zoom;
        if(full){
            for(int i=0;i<40*zoomPlus;i++){
                Vec2d vec=new Vec2d(Util.random((float) -sc.windowWidth /2-100, (float) sc.windowWidth /2+100),Util.random((float) -sc.windowHeight /2-100, (float) sc.windowHeight /2+100));
                float size=Util.random(15,25);
                vec=vec.add(sc.camX, sc.camY);
                GroundParticle ground=new GroundParticle(vec);
                groundParticles.add(ground);
                particles.add(ground);
            }
        }
        else{
            if(groundParticles.size()>=200*zoomPlus||System.currentTimeMillis()-lastGenerateGround<16*(10/player.velocity.length())) return;
            for(int i=1;i<2*zoomPlus;i++){
                Vec2d vec=new Vec2d(Util.random((float) -sc.windowWidth /2-100, (float) sc.windowWidth /2+100),Util.random((float) -sc.windowHeight /2-100, (float) sc.windowHeight /2+100));
                float size=Util.random(15,25);
                vec=vec.add(sc.camX, sc.camY).add(player.velocity.multiply(3));
                GroundParticle ground=new GroundParticle(vec);
                if(!ground.shouldKill(true)) continue;
                groundParticles.add(ground);
                particles.add(ground);
                if(groundParticles.size()>=200*zoomPlus) break;
            }
            lastGenerateGround=System.currentTimeMillis();
        }
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
    /*public void fastUpdate(float time){
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
            multiClientHandler.clients.forEach(c->c.send(new MessageS2CPacket("<server> "+str)));
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
