package big.engine.math;

public class SegmentY {
    public double minY;
    public double maxY;
    public double x;

    public SegmentY(double minY, double maxY, double x) {
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.x = x;
    }


    public boolean interact(SegmentY other) {
        return this.x == other.x && interact(other.minY, other.maxY);
    }


    public boolean interact(double minY, double maxY) {
        return this.minY < maxY && this.maxY > minY;
    }
}