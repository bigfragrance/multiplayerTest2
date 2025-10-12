package big.engine.math;


import big.engine.util.Util;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class Box {
    public double minX;
    public double maxX;
    public double minY;
    public double maxY;
    public Box(double maxX, double minX, double maxY, double minY){
        this.minX=Math.min(minX,maxX);
        this.maxX=Math.max(minX,maxX);
        this.minY=Math.min(minY,maxY);
        this.maxY=Math.max(minY,maxY);
    }
    public Box(Vec2i pos){
        this(pos.x+1,pos.x,pos.y+1,pos.y);
    }
    public Box(int x,int y){
        this(x+1,x,y+1,y);
    }
    public Box(Vec2d v1,Vec2d v2){
        this(v1.x,v2.x,v1.y,v2.y);
    }
    public Box(Vec2d center, double dx, double dy){
        this(center.x-dx, center.x+dx, center.y -dy,center.y+dy);
    }
    public Box(Vec2d center, double d){
        this(center,d,d);
    }
    public boolean intersects(Box box) {
        return this.intersects(box.minX, box.minY, box.maxX, box.maxY);
    }
    public boolean intersectsCircle(Box box){
        double d=box.getCenter().distanceTo(this.getCenter());
        return d*2<=Math.max(box.xSize(),box.ySize())+Math.max(this.xSize(),this.ySize());
    }
    public Box expand(double x,double y){
        return new Box(maxX+x,minX-x,maxY+y,minY-y);

    }
    public Box expand(double d){
        return expand(d,d);
    }
    public Box stretch(double x, double y) {
        double d = this.minX;
        double e = this.minY;
        double g = this.maxX;
        double h = this.maxY;
        if (x < 0.0) {
            d += x;
        } else if (x > 0.0) {
            g += x;
        }

        if (y < 0.0) {
            e += y;
        } else if (y > 0.0) {
            h += y;
        }


        return new Box(d, g, e, h);
    }
    public SegmentY getRightSegment() {
        return new SegmentY(minY, maxY, maxX);
    }

    public SegmentY getLeftSegment() {
        return new SegmentY(minY, maxY, minX);
    }

    public SegmentX getTopSegment() {
        return new SegmentX(minX, maxX, maxY);
    }

    public SegmentX getBottomSegment() {
        return new SegmentX(minX, maxX, minY);
    }
    /**
     * Checks if this box intersects the box of the given coordinates.
     */
    public boolean intersects(double minX, double minY, double maxX, double maxY) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY;
    }

    /**
     * Checks if this box intersects the box of the given positions as
     * corners.
     */
    public boolean intersects(Vec2d pos1, Vec2d pos2) {
        return this.intersects(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y));
    }
    /**
     * Checks if the given position is in this box.
     */
    public boolean contains(Vec2d pos) {
        return this.contains(pos.x, pos.y);
    }

    /**
     * Checks if the given position is in this box.
     */
    public boolean contains(double x, double y) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY;
    }
    public void offset1(double x,double y){
        this.minX+=x;
        this.maxX+=x;
        this.minY+=y;
        this.maxY+=y;
    }
    public void offset1(Vec2d vec){
        offset1(vec.x,vec.y);
    }
    public Box offset(double x,double y){
        return new Box(minX+x,maxX+x,minY+y,maxY+y);
    }
    public Box offset(Vec2d vec){
        return offset(vec.x,vec.y);
    }
    public double xSize(){
        return this.maxX-this.minX;
    }
    public double ySize(){
        return this.maxY-this.minY;
    }
    public double avgSize(){
        return (xSize()+ySize())/2;
    }
    public Set<Vec2i> getContaining(){
        Set<Vec2i> set=new HashSet<>();
        for(int x=(int)Math.floor(minX);x<=(int)Math.floor(maxX);x++){
            for(int y=(int)Math.floor(minY);y<=(int)Math.floor(maxY);y++){
                set.add(new Vec2i(x,y));
            }
        }
        return set;
    }
    public Box switchToJFrame(){
        return new Box(Util.switchXToJFrame(minX), Util.switchXToJFrame(maxX),Util.switchYToJFrame(minY),Util.switchYToJFrame(maxY));
    }
    public Box switchToGame(){
        return new Box(Util.switchXToGame(minX),Util.switchXToGame(maxX),Util.switchYToGame(minY),Util.switchYToGame(maxY));
    }
    public Vec2d getCenter(){
        return new Vec2d((minX+maxX)/2,(minY+maxY)/2);
    }
    public String toString(){
        return "Box("+minX+","+maxX+","+minY+","+maxY+")";
    }
    public Box copy(){
        return new Box(minX,maxX,minY,maxY);
    }
    public static Box fromString(String str) {

        str = str.replace("Box(", "").replace(")", "");


        String[] parts = str.split(",");


        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid string format for Box");
        }


        double minX = Double.parseDouble(parts[0]);
        double maxX = Double.parseDouble(parts[1]);
        double minY = Double.parseDouble(parts[2]);
        double maxY = Double.parseDouble(parts[3]);


        return new Box(minX, maxX, minY, maxY);
    }
    public JSONObject toJSON(){
        JSONObject json=new JSONObject();
        json.put("a",Util.getRoundedDouble(minX,1));
        json.put("b",Util.getRoundedDouble(maxX,1));
        json.put("c",Util.getRoundedDouble(minY,1));
        json.put("d",Util.getRoundedDouble(maxY,1));
        return json;
    }
    public static Box fromJSON(JSONObject json){
        return new Box(json.getDouble("a"),json.getDouble("b"),json.getDouble("c"),json.getDouble("d"));
    }
    public int hashCode() {
        long bitsA = Double.doubleToLongBits(minX);
        long bitsB = Double.doubleToLongBits(maxX);
        long bitsC = Double.doubleToLongBits(minY);
        long bitsD = Double.doubleToLongBits(maxY);
        long combined = 17;
        combined = 31 * combined + bitsA;
        combined = 31 * combined + bitsB;
        combined = 31 * combined + bitsC;
        combined = 31 * combined + bitsD;
        return Long.hashCode(combined);
    }
    public Vec2d getMinPos(){
        return new Vec2d(minX,minY);
    }
    public Vec2d getMaxPos(){
        return new Vec2d(maxX,maxY);
    }
    public Vec2d getMinXMaxY(){
        return new Vec2d(minX,maxY);
    }
    public Vec2d getMaxXMinY(){
        return new Vec2d(maxX,minY);
    }
    public boolean valueEquals(Box other){
        return minX==other.minX&&maxX==other.maxX&&minY==other.minY&&maxY==other.maxY;
    }
}
