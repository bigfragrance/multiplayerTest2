package big.engine.math;


import big.engine.math.util.Util;
import big.engine.render.Screen;
import org.json.JSONObject;

public class Vec2d {
    public float x;
    public float y;

    public Vec2d(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public Vec2d(float rotation){
        this(Util.cos(rotation),Util.sin(rotation));
    }

    public static Vec2d zero() {
        return new Vec2d(0, 0);
    }

    public Vec2d add(float x, float y) {
        return new Vec2d(this.x + x, this.y + y);
    }

    public Vec2d add(Vec2d v) {
        return add(v.x, v.y);
    }

    public float length() {
        return Math.sqrt(x * x + y * y);
    }

    public Vec2d subtract(float x, float y) {
        return this.add(-x, -y);
    }

    public Vec2d multiply(float a) {
        return new Vec2d(x * a, y * a);
    }

    public void multiply1(float a) {
        set(x * a, y * a);
    }

    public Vec2d subtract(Vec2d pos) {
        return subtract(pos.x, pos.y);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void offset(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public Vec2d copy() {
        return new Vec2d(x, y);
    }

    public void offset(Vec2d velocity) {
        offset(velocity.x, velocity.y);
    }

    public void set(Vec2d vec) {
        set(vec.x, vec.y);
    }
    public Vec2d mirrorXAxis(){
        return new Vec2d(x,-y);
    }
    public Vec2d mirrorYAxis(){
        return new Vec2d(-x,y);
    }
    public Vec2d switchToJFrame() {
        return new Vec2d(Util.switchXToJFrame(x), Util.switchYToJFrame(y));
    }
    public Vec2d switchToJFrame(float zoom) {
        return new Vec2d(Util.switchXToJFrame(x,zoom), Util.switchYToJFrame(y,zoom));
    }
    public Vec2d switchToJFrameOld(float zoom) {
        return new Vec2d(Util.switchXToJFrameOld(x,zoom), Util.switchYToJFrameOld(y,zoom));
    }
    public Vec2d switchToGame() {
        return new Vec2d(Util.switchXToGame(x), Util.switchYToGame(y));
    }
    public Vec2d switchToGame(float zoom) {
        return new Vec2d(Util.switchXToGame(x,zoom), Util.switchYToGame(y,zoom));
    }
    public Vec2d switchToGame1() {
        return new Vec2d((x - (float) Screen.sc.windowWidth / 2)/Screen.sc.getRealZoom(), (-y + (float) Screen.sc.windowHeight / 2)/Screen.sc.getRealZoom());
    }

    public Vec2d limit(float l) {
        if (this.length() <= 0.0001) return this;
        float e = l / length();
        return this.multiply(e);
    }
    public Vec2d limitOnlyOver(float l){
        if (this.length() <= l) return this.copy();
        return this.limit(l);
    }
    public float dot(Vec2d pos) {
        return x * pos.x + y * pos.y;
    }
    public float cross(Vec2d pos) {
        return x * pos.y - y * pos.x;
    }
    public float angle(){
        if(this.length()<=0.00000001) return 0;
        return Math.toDegrees(Math.atan2(y, x));
    }
    public float distanceTo(Vec2d pos) {
        return this.subtract(pos).length();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof Vec2d v) {
            return x == v.x && y == v.y;
        }
        return false;
    }
    public Vec2d rotate(float angle) {
        float cos = Util.cos(angle);
        float sin = Util.sin(angle);
        return new Vec2d(x * cos - y * sin, x * sin + y * cos);
    }
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("x",Util.getRoundedfloat(x,1));
        json.put("y",Util.getRoundedfloat(y,1));
        return json;
    }
    public BlockPos ofFloor(){
        return BlockPos.ofFloor(this);
    }
    public static Vec2d fromJSON(JSONObject json) {
        return new Vec2d(json.getfloat("x"), json.getfloat("y"));
    }
}