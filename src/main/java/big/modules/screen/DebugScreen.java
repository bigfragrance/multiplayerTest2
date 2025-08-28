package big.modules.screen;

import big.engine.math.Vec2d;
import big.engine.math.util.TaskManagerMemoryApproximator;
import big.engine.math.util.Util;
import big.engine.render.Screen;
import big.events.RenderEvent;
import big.events.TickEvent;
import meteordevelopment.orbit.EventHandler;

import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;
import static big.modules.entity.Entity.sizeMultiplier;

public class DebugScreen {
    public static DebugScreen INSTANCE=null;
    public AtomicInteger idCounter=new AtomicInteger(0);
    public ConcurrentHashMap<Integer,String> messageList=new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer,StringUpdater> updaterList=new ConcurrentHashMap<>();
    public static int textSize=12;
    private long lastUpdate=0;
    public static void init(){
        INSTANCE=new DebugScreen();
        INSTANCE.addToRender(() -> "DebugScreen");
        INSTANCE.addToRender(()->"TPS: "+String.format("%.1f",cs.currentTPS.getAvg()));
        INSTANCE.addToRender(()->"MSPT: "+String.format("%.1f",cs.nspt/1000000d));
        INSTANCE.addToRender(()->"RunTime: "+String.format("%.1f",cs.runTime/20d));
        INSTANCE.addToRender(()->"MemoryUsed: "+ String.format("%.1f",TaskManagerMemoryApproximator.getMemoryUsed()/(1024d*1024d))+"MB");
        INSTANCE.addToRender(()->"Entities: "+cs.entities.size());
        INSTANCE.addToRender(()->"Players: "+(cs.multiClientHandler==null?0: cs.multiClientHandler.clients.size()));
    }
    public DebugScreen(){
        cs.EVENT_BUS.subscribe(this);
        System.out.println("DebugScreen init");
    }
    @EventHandler
    public void onRender(RenderEvent event){
        sc.storeAndSetDef();
        Vec2d pos= getDebugMessageRenderPosition();
        event.g.setColor(Color.MAGENTA);

        for(int i=0;i<idCounter.get();i++){
            String s=messageList.get(i);
            /*if(updaterList.containsKey(i)){
                s=updaterList.get(i).getString();
            }*/
            Util.renderString(event.g,s,pos,Util.round(textSize* sc.zoom*sizeMultiplier),false);
            pos.y+=20/sc.zoom2;
        }
        sc.restoreZoom();
    }
    @EventHandler
    public void onTick(TickEvent event){
        if(event.isPre()) return;
        if(System.currentTimeMillis()-lastUpdate<100) return;
        lastUpdate=System.currentTimeMillis();
        for(int i=0;i<idCounter.get();i++){
            if(updaterList.containsKey(i)){
                messageList.put(i,updaterList.get(i).getString());
            }
        }
    }
    public void addToRender(StringUpdater updater){
        int id=idCounter.getAndIncrement();
        messageList.put(id,updater.getString());
        updaterList.put(id,updater);
    }

    private Vec2d getDebugMessageRenderPosition(){
        return Screen.SCREEN_BOX.getMinPos().add(100,100).subtract(sc.getMiddle()).multiply(1/sc.zoom2).add(sc.getMiddle());
    }
}
