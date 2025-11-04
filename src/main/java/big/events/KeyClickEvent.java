package big.events;

public class KeyClickEvent {
    public static KeyClickEvent INSTANCE=new KeyClickEvent();
    public char button;
    public KeyClickEvent(){
        button=0;
    }
    public static KeyClickEvent get(char button){
        INSTANCE.button=button;
        return INSTANCE;
    }
}
