package big.modules.screen;

import big.engine.math.Vec2d;
import big.engine.math.util.Util;
import big.engine.math.util.timer.AutoList;
import big.engine.render.Screen;
import big.events.MessageReceiveEvent;
import big.events.RenderEvent;
import meteordevelopment.orbit.EventHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static big.engine.math.util.EntityUtils.scoreSize;
import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;
import static big.modules.entity.Entity.sizeMultiplier;

public class ChatMessageScreen {
    public static ChatMessageScreen INSTANCE=null;
    public List<String> messageList=new ArrayList<>();
    public AutoList<String> renderList=new AutoList<>();
    public static int textSize=12;
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
        Vec2d pos=getChatMessageRenderPosition();
        event.g.setColor(Color.DARK_GRAY);
        renderList.update(20000);
        List<String> strings=renderList.getList();
        for(int i=strings.size()-1;i>=0;i--){
            String s=strings.get(i);
            Util.renderString(event.g,s.substring(0,s.length()-1),pos,Util.round(textSize* sc.zoom*sizeMultiplier),false);
            pos.y-=15/sc.zoom2;
        }
        sc.restoreZoom();
    }
    @EventHandler
    public void onMessage(MessageReceiveEvent event){
        renderList.add(event.text+(char)Util.random.nextInt(65536));
        messageList.add(event.text);
        if(messageList.size()>1000){
            messageList.remove(0);
        }
    }

    private Vec2d getChatMessageRenderPosition(){
        return Screen.SCREEN_BOX.getMinXMaxY().add(100,-100).subtract(sc.getMiddle()).multiply(1/sc.zoom2).add(sc.getMiddle());
    }
}
