package big.engine.math.util.timer;

import java.util.ArrayList;

public class TimerList {
    public ArrayList<Timer> timers=new ArrayList<>();
    public void update(){
        for(Timer t:timers){
            t.update();
        }
    }
    public Timer add(Timer t){
        timers.add(t);
        return t;
    }
}
