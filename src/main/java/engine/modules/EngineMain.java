package engine.modules;


import engine.math.BlockPos2d;
import engine.math.Vec2d;
import engine.math.util.Util;
import engine.render.Screen;
import modules.client.ClientNetwork;
import modules.entity.Entity;
import modules.entity.PlayerEntity;
import modules.network.ClientNetworkHandler;
import modules.network.ServerNetworkHandler;
import modules.particle.Particle;
import server.MultiClientHandler;
import server.ServerMain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class EngineMain implements Runnable{
    public static volatile EngineMain cs;
    public HashMap<Long, Entity> entities=new HashMap<>();
    public ArrayList<Particle> particles=new ArrayList<>();
    public volatile AtomicLong lastEntityID=new AtomicLong(0);
    public static double TPS=10;
    public boolean isServer=true;
    public ClientNetworkHandler networkHandler;
    public MultiClientHandler multiClientHandler=null;
    public PlayerEntity player;
    public static long lastTick=0;
    public Vec2d camPos=new Vec2d(0,0);
    public Vec2d prevCamPos=new Vec2d(0,0);
    public EngineMain(String ip,int port,boolean isServer){
        cs=this;
        this.isServer=isServer;
        if(isServer){
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
        return  TPS*((double)(System.currentTimeMillis() - lastTick)) / 1000d;
    }
    public void update(){
        if(isServer){
            for(Entity entity:entities.values()){
                entity.tick();
                multiClientHandler.clients.forEach(c->c.serverNetworkHandler.sendEntityUpdate(entity));
            }
        }else{
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
        }

        Screen.lastKeyPressed= (HashMap<Character, Boolean>) Screen.keyPressed.clone();
    }
    public void addParticles(){
        if(particles.size()>30) return;

    }
    public void generateGroundBlocks(boolean full){
        if(player==null) return;
        if(full){
            for(int i=0;i<40;i++){
                Vec2d vec=new Vec2d(Util.random((double) -Screen.INSTANCE.windowWidth /2-100, (double) Screen.INSTANCE.windowWidth /2+100),Util.random((double) -Screen.INSTANCE.windowHeight /2-100, (double) Screen.INSTANCE.windowHeight /2+100));
                double size=Util.random(15,25);
                vec=vec.add(camX,camY);
                Ground ground=new Ground(vec,new Box(vec,size,size),size);
                groundBlocks.add(ground);
                entities.add(ground);
            }
        }
        else{
            if(groundBlocks.size()>=200||System.currentTimeMillis()-lastGenerateGround<16*(10/player.velocity.length())) return;
            for(int i=1;i<2;i++){
                Vec2d vec=new Vec2d(Util.random((double) -Screen.INSTANCE.windowWidth /2-100, (double) Screen.INSTANCE.windowWidth /2+100),Util.random((double) -Screen.INSTANCE.windowHeight /2-100, (double) Screen.INSTANCE.windowHeight /2+100));
                double size=Util.random(15,25);
                vec=vec.add(camX,camY).add(player.velocity.multiply(3));
                Ground ground=new Ground(vec,new Box(vec,size,size),size);
                if(!ground.shouldKill(true)) continue;
                groundBlocks.add(ground);
                entities.add(ground);
                if(groundBlocks.size()>=200) break;
            }
            lastGenerateGround=System.currentTimeMillis();
        }
    }
    public Vec2d getCamPos(){
        return Util.lerp(prevCamPos,camPos,getTickDelta());
    }
    public void addEntity(Entity entity){
        if(isServer)entity.id=lastEntityID.get();
        entities.put(entity.id,entity);
        if(isServer)lastEntityID.incrementAndGet();
        if(isServer){
            multiClientHandler.clients.forEach(c->c.serverNetworkHandler.sendEntitySpawn(entity));
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
