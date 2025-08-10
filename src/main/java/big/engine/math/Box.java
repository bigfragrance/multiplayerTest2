package big.engine.math;


import big.engine.math.util.Util;
import org.json.JSONObject;

public class Box {
    public float minX;
    public float maxX;
    public float minY;
    public float maxY;
    public Box(float maxX, float minX, float maxY, float minY){
        this.minX=Math.min(minX,maxX);
        this.maxX=Math.max(minX,maxX);
        this.minY=Math.min(minY,maxY);
        this.maxY=Math.max(minY,maxY);
    }
    public Box(BlockPos pos){
        this(pos.x+1,pos.x,pos.y+1,pos.y);
    }
    public Box(int x,int y){
        this(x+1,x,y+1,y);
    }
    public Box(Vec2d v1,Vec2d v2){
        this(v1.x,v2.x,v1.y,v2.y);
    }
    public Box(Vec2d center, float dx, float dy){
        this(center.x-dx, center.x+dx, center.y -dy,center.y+dy);
    }
    public Box(Vec2d center, float d){
        this(center,d,d);
    }
    public boolean intersects(Box box) {
        return this.intersects(box.minX, box.minY, box.maxX, box.maxY);
    }
    public boolean intersectsCircle(Box box){
        float d=box.getCenter().distanceTo(this.getCenter());
        return d*2<=Math.max(box.xSize(),box.ySize())+Math.max(this.xSize(),this.ySize());
    }
    public Box expand(float x,float y){
        return new Box(maxX+x,minX-x,maxY+y,minY-y);

    }
    public Box expand(float d){
        return expand(d,d);
    }
    public Box stretch(float x, float y) {
        float d = this.minX;
        float e = this.minY;
        float g = this.maxX;
        float h = this.maxY;
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
    public boolean intersects(float minX, float minY, float maxX, float maxY) {
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
    public boolean contains(float x, float y) {
        return x >= this.minX && x < this.maxX && y >= this.minY && y < this.maxY;
    }
    public void offset1(float x,float y){
        this.minX+=x;
        this.maxX+=x;
        this.minY+=y;
        this.maxY+=y;
    }
    public void offset1(Vec2d vec){
        offset1(vec.x,vec.y);
    }
    public Box offset(float x,float y){
        return new Box(minX+x,maxX+x,minY+y,maxY+y);
    }
    public Box offset(Vec2d vec){
        return offset(vec.x,vec.y);
    }
    public float xSize(){
        return this.maxX-this.minX;
    }
    public float ySize(){
        return this.maxY-this.minY;
    }
    public float avgSize(){
        return (xSize()+ySize())/2;
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


        float minX = float.parsefloat(parts[0]);
        float maxX = float.parsefloat(parts[1]);
        float minY = float.parsefloat(parts[2]);
        float maxY = float.parsefloat(parts[3]);


        return new Box(minX, maxX, minY, maxY);
    }
    public JSONObject toJSON(){
        JSONObject json=new JSONObject();
        json.put("a",Util.getRoundedfloat(minX,1));
        json.put("b",Util.getRoundedfloat(maxX,1));
        json.put("c",Util.getRoundedfloat(minY,1));
        json.put("d",Util.getRoundedfloat(maxY,1));
        return json;
    }
    public static Box fromJSON(JSONObject json){
        return new Box(json.getfloat("a"),json.getfloat("b"),json.getfloat("c"),json.getfloat("d"));
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
