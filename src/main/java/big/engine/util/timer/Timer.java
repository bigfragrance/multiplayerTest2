package big.engine.util.timer;

public interface Timer {
    void update();
    boolean passed();
    void reset();
}
