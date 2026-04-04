package big.game.screen;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.render.Screen;
import big.engine.util.TaskManagerMemoryApproximator;
import big.engine.util.Util;
import big.events.RenderEvent;
import big.events.TickEvent;
import big.game.entity.effect.Effect;
import big.game.network.packet.s2c.AssetsS2CPacket;
import meteordevelopment.orbit.EventHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;
import static big.game.entity.Entity.sizeMultiplier;

public class EffectScreen {
    public static EffectScreen INSTANCE=null;
    public AtomicInteger idCounter=new AtomicInteger(0);
    public ConcurrentHashMap<String, Effect> effectList=new ConcurrentHashMap<>();
    public static int gapSize=30;
    public static int gap2Size=40;
    public static int arcSize=20;
    public static void init(){
        INSTANCE=new EffectScreen();
    }
    public EffectScreen(){
        cs.EVENT_BUS.subscribe(this);
        System.out.println("EffectScreen init");
    }
    @EventHandler
    public void onRender(RenderEvent event){
        sc.storeAndSetDef();
        Vec2d pos= getDebugMessageRenderPosition();

        ArrayList<Effect> effects=new ArrayList<>(effectList.values());
        effects.sort((a,b)->Integer.compare(b.duration,a.duration));
        for(int i=0;i<effects.size();i++){
            Effect effect=effects.get(i);
            Util.renderString(event.g,effect.name,pos,Util.round(gapSize/sc.zoom2),false,new Color(255, 255, 255,effect.getRenderAlpha())
                    ,new Color(193, 193, 193,effect.getRenderAlpha()), (float) (3/sc.zoom2));
            event.g.setColor(new Color(251, 85, 85,effect.getRenderAlpha()));
            ((Graphics2D)event.g).setStroke(new BasicStroke((float) (5/sc.zoom2)));
            Util.renderArc(event.g,new Box(pos.add(-gap2Size/sc.zoom2,-gapSize/2d/sc.zoom2),arcSize/sc.zoom2),0,360*effect.getProgress());
            //Util.renderCubeLine(event.g,new Box(pos.add(gap2Size/sc.zoom2,0),arcSize/sc.zoom2));
            pos.y+=gapSize/sc.zoom2*2;
        }
        sc.restoreZoom();
    }
    public void addEffect(Effect effect){
        if(effectList.containsKey(effect.name)) {
            effectList.get(effect.getName()).setDuration(effect.getDuration());
            return;
        }
        effectList.put(effect.name,effect);
    }
    public void removeEffect(String name) {
        effectList.remove(name);
    }
    @EventHandler
    public void onTick(TickEvent event){
        if(event.isPre()) return;
        effectList.values().forEach(Effect::update);
        effectList.values().removeIf(Effect::isExpired);
    }


    private Vec2d getDebugMessageRenderPosition(){
        return Screen.SCREEN_BOX.getMinPos().add(150,400).subtract(sc.getMiddle()).multiply(1/sc.zoom2).add(sc.getMiddle());
    }


}
