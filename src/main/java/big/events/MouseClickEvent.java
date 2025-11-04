package big.events;

public class MouseClickEvent {
    public static MouseClickEvent INSTANCE=new MouseClickEvent();
    public int button;
    public MouseClickEvent(){
        button=0;
    }
    public static MouseClickEvent get(int button){
        INSTANCE.button=button;
        return INSTANCE;
    }
}
