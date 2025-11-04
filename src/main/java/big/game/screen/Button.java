package big.game.screen;

import big.engine.util.Getter;

public class Button {
    private Getter<String> text;
    private Runnable action;
    private boolean clearItems=true;
    public Button(Getter<String> text,Runnable action,boolean clearItems){
        this.text=text;
        this.action=action;
        this.clearItems=clearItems;
    }
    public Button(String text,Runnable action,boolean clearItems){
        this(()->text,action,clearItems);
    }
    public Button(String text,Runnable action){
        this(()->text,action,true);
    }
    public boolean shouldClearItems(){
        return clearItems;
    }
    public void onClick(){
        action.run();
    }
    public String getText(){
        return text.get();
    }
}
