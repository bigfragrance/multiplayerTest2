package big.engine.math.util;

public class NNPRecorder<T> {
    private T[] data;
    private boolean isEmpty=true;
    public NNPRecorder(){
        data=(T[])new Object[3];
    }
    public NNPRecorder(T def){
        data=(T[])new Object[3];
        setDefault(def);
    }
    public NNPRecorder<T> setDefault(T def){
        data[0]=def;
        data[1]=def;
        data[2]=def;
        return this;
    }
    public void setNext(T t){
        if(isEmpty){
            isEmpty=false;
            data[0]=t;
            data[1]=t;
            data[2]=t;
            return;
        }
        data[2]=t;
    }
    public void setNow(T t){
        if(isEmpty){
            isEmpty=false;
            data[0]=t;
            data[1]=t;
            data[2]=t;
        }
        nextPrev();
        data[1]=t;
    }
    public void next(){
        if(data[2]==null) return;
        data[0]=data[1];
        data[1]=data[2];
        data[2]=null;
    }
    public void nextPrev(){
        data[0]=data[1];
    }
    public T get(){
        return data[1];
    }
    public T getPrev(){
        return data[0];
    }
    public T getNext(){
        return data[2];
    }
}
