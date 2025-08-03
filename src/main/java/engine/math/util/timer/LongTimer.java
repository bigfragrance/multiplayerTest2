package engine.math.util.timer;

import engine.render.Screen;

public class LongTimer implements Timer{
    public long startTime;
    public long delay;
    public LongTimer(long delay){
        this.delay=delay;
        startTime=System.currentTimeMillis()+delay;
    }
    public void update() {

    }
    public boolean passed() {
        return System.currentTimeMillis()-startTime>=delay;
    }
    public void reset() {
        startTime=System.currentTimeMillis();
    }
}
