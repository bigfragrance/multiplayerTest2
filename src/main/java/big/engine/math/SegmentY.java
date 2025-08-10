package big.engine.math;

public class SegmentY {
    public float minY;
    public float maxY;
    public float x;

    public SegmentY(float minY, float maxY, float x) {
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.x = x;
    }


    public boolean interact(SegmentY other) {
        return this.x == other.x && interact(other.minY, other.maxY);
    }


    public boolean interact(float minY, float maxY) {
        return this.minY < maxY && this.maxY > minY;
    }
}