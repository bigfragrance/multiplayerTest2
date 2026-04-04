package big.game.entity.effect;

import big.engine.render.Screen;
import big.engine.util.Util;

public class Effect {
    public String name;
    public int duration;
    public int prevDuration;
    public int maxTime;
    private long addTime;
    public Effect(String name,int duration,int maxTime){
        this.name=name;
        this.duration=duration;
        this.maxTime=maxTime;
        addTime=System.currentTimeMillis();
    }
    public String getName(){
        return name;
    }
    public int getDuration(){
        return duration;
    }
    public int getMaxTime(){
        return maxTime;
    }
    public void update(){
        prevDuration=duration;
        duration--;
    }
    public boolean isExpired(){
        return duration<=0;
    }
    public double getProgress(){
        double d=(Util.lerp(prevDuration,duration,Screen.tickDelta))/maxTime;
        return Math.clamp(d,0,1);
    }
    public int getRenderAlpha(){
        long l=System.currentTimeMillis()-addTime;
        if(l<=255){
            return (int)l;
        }
        return (int)Math.clamp(getProgress()*255*4,0,255);
    }

    public void setDuration(int duration) {
        this.duration=duration;
    }
}
