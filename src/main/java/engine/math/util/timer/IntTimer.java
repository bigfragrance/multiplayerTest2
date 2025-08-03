package engine.math.util.timer;

public class IntTimer implements Timer{
    public int time;
    public int delay;
    public IntTimer(int delay){
        this.delay=delay;
        time=delay;
    }

    @Override
    public void update() {
        time=Math.min(time+1,delay);
    }

    @Override
    public boolean passed() {
        return time>=delay;
    }

    @Override
    public void reset() {
        time=0;
    }
}
