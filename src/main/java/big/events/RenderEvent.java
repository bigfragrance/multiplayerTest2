package big.events;

import java.awt.*;

public class RenderEvent {
    public static RenderEvent INSTANCE=new RenderEvent();
    public Graphics g;
    public static RenderEvent get(Graphics g){
        INSTANCE.g=g;
        return INSTANCE;
    }
}
