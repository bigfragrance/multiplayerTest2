package big.events;

public class MessageReceiveEvent {
    public static MessageReceiveEvent INSTANCE=new MessageReceiveEvent();
    public String text;
    public int aheadTime;
    public static MessageReceiveEvent get(String text,int aheadTime){
        INSTANCE.text=text;
        INSTANCE.aheadTime=aheadTime;
        return INSTANCE;
    }
}
