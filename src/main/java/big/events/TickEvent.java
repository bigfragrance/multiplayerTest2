package big.events;

public class TickEvent {
    public static TickEvent INSTANCE=new TickEvent();
    public int state=0;
    public TickEvent(){

    }
    public boolean is(int i){
        return state==i;
    }
    public boolean isPre(){
        return is(0);
    }
    public boolean isPost(){
        return is(1);
    }
    public static TickEvent get(int state){
        INSTANCE.state=state;
        return INSTANCE;
    }
}
