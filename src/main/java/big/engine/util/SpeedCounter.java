package big.engine.util;


import java.util.LinkedList;
import java.util.Queue;


public class SpeedCounter {
    private long checkPeriod=1000;
    private Queue<Long> times=new LinkedList<>();
    public SpeedCounter(){

    }
    public SpeedCounter(long checkPeriod){
        this.checkPeriod=checkPeriod;
    }
    public void add(){
        add(System.currentTimeMillis());
    }
    public void add(long time){
        times.add(time);
        while(true){
            if(times.isEmpty()){
                break;
            }
            if(times.peek()<time-checkPeriod){
                times.poll();
            }else{
                break;
            }
        }
    }
    public double getSpeed(){
        return 1000d*times.size()/checkPeriod;
    }
}
