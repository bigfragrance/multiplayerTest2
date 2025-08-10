package big.engine.math;

public class SegmentX {
    public double minX;
    public double maxX;
    public double y;
    public SegmentX(double minX,double maxX,double y){
        this.minX=Math.min(minX,maxX);
        this.maxX=Math.max(minX,maxX);
        this.y=y;
    }
    public boolean interact(SegmentX other){
        return this.y==other.y&&interact(other.minX,other.maxX);
    }
    public boolean interact(double minX,double maxX){
        return this.minX<maxX&&this.maxX>minX;
    }
}
