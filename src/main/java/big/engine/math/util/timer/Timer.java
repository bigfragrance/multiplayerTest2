package big.engine.math.util.timer;

public interface Timer {
    void update();
    boolean passed();
    void reset();
}
