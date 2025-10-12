package big.game.screen;

import big.engine.math.Vec2d;
import big.engine.util.Util;
import big.engine.util.timer.AutoList;
import big.engine.render.Screen;
import big.events.MessageReceiveEvent;
import big.events.RenderEvent;
import big.events.TickEvent;
import meteordevelopment.orbit.EventHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;
import static big.game.entity.Entity.sizeMultiplier;

public class ChatMessageScreen {
    public static ChatMessageScreen INSTANCE=null;
    public List<String> messageList=new ArrayList<>();
    public AutoList<String> renderList=new AutoList<>();
    public static int textSize=12;
    public int currentIndex=0;
    public boolean isFullRendering=false;
    public static void init(){
        INSTANCE=new ChatMessageScreen();
    }
    public ChatMessageScreen(){
        cs.EVENT_BUS.subscribe(this);
        System.out.println("ChatMessageScreen init");
    }
    @EventHandler
    public void onRender(RenderEvent event){
        sc.storeAndSetDef();
        currentIndex=Math.clamp(currentIndex,0,messageList.size());
        Vec2d pos=getChatMessageRenderPosition().add(0,currentIndex*18/sc.zoom2);
        event.g.setColor(Color.DARK_GRAY);
        renderList.update(20000);
        List<String> strings=getStrings();
        for(int i=strings.size()-1;i>=0;i--){
            String s=strings.get(i);
            Util.renderString(event.g,s.substring(0,s.length()-1),pos,Util.round(textSize* sc.zoom*sizeMultiplier),false);
            pos.y-=18/sc.zoom2;
        }
        sc.restoreZoom();
    }
    @EventHandler
    public void onMessage(MessageReceiveEvent event){
        String text=event.text+(char)Util.random.nextInt(65536);
        renderList.add(text,event.aheadTime);
        messageList.add(text);
        if(messageList.size()>1000){
            messageList.remove(0);
        }
    }
    @EventHandler
    public void onTick(TickEvent event){
        if(Screen.isKeyClicked('y')){
            isFullRendering=!isFullRendering;
            if(isFullRendering){
                currentIndex=0;
            }
        }
    }
    private List<String> getStrings(){
        if(!isFullRendering) return renderList.getList();
        else{
            return messageList;//.subList(Math.clamp(currentIndex,0,Math.max(0,messageList.size()-20)),Math.clamp(currentIndex,0,Math.max(0,messageList.size())));
        }
    }

    private Vec2d getChatMessageRenderPosition(){
        return Screen.SCREEN_BOX.getMinXMaxY().add(100,-100).subtract(sc.getMiddle()).multiply(1/sc.zoom2).add(sc.getMiddle());
    }
}
