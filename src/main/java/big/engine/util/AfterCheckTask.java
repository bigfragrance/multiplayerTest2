package big.engine.util;

public interface AfterCheckTask<T> {
    void run(T t);
    default <U> AfterCheckTask2O<T,U> convert(){
        return (t, u) -> run(t);
    }
}
