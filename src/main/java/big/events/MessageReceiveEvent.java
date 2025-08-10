package big.events;

public class MessageReceiveEvent {
    public static MessageReceiveEvent INSTANCE=new MessageReceiveEvent();
    public String text;
    public static MessageReceiveEvent get(String text){
        INSTANCE.text=text;
        return INSTANCE;
    }
}
