package big.engine.math.util;

import java.util.ArrayList;

public class AutoRecorder<T> extends ArrayList<T> {
    private int maxLength;
    public AutoRecorder(int maxLength){
        super();
        this.maxLength=maxLength;
    }
    public void setMaxLength(int maxLength){
        this.maxLength=maxLength;
    }
    public int getMaxLength(){
        return maxLength;
    }
    public boolean add(T t){
        super.add(t);
        if(size()>maxLength){
            remove(0);
        }
        return true;
    }
}
