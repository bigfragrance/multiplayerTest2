package big.engine.math;

public class SegmentX {
    public float minX;
    public float maxX;
    public float y;
    public SegmentX(float minX,float maxX,float y){
        this.minX=Math.min(minX,maxX);
        this.maxX=Math.max(minX,maxX);
        this.y=y;
    }
    public boolean interact(SegmentX other){
        return this.y==other.y&&interact(other.minX,other.maxX);
    }
    public boolean interact(float minX,float maxX){
        return this.minX<maxX&&this.maxX>minX;
    }
}
