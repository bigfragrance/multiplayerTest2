package big.engine.math;


import big.engine.math.util.Util;
import big.engine.render.Screen;
import org.json.JSONObject;

public class Vec2d {
    public double x;
    public double y;

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Vec2d(double rotation){
        this(Util.cos(rotation),Util.sin(rotation));
    }

    public static Vec2d zero() {
        return new Vec2d(0, 0);
    }

    public Vec2d add(double x, double y) {
        return new Vec2d(this.x + x, this.y + y);
    }

    public Vec2d add(Vec2d v) {
        if(v==null) return this;
        return add(v.x, v.y);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Vec2d subtract(double x, double y) {
        return this.add(-x, -y);
    }

    public Vec2d multiply(double a) {
        return new Vec2d(x * a, y * a);
    }

    public void multiply1(double a) {
        set(x * a, y * a);
    }

    public Vec2d subtract(Vec2d pos) {
        return subtract(pos.x, pos.y);
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void offset(double x, double y) {
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
    public Vec2d switchToJFrame(double zoom) {
        return new Vec2d(Util.switchXToJFrame(x,zoom), Util.switchYToJFrame(y,zoom));
    }
    public Vec2d switchToJFrameOld(double zoom) {
        return new Vec2d(Util.switchXToJFrameOld(x,zoom), Util.switchYToJFrameOld(y,zoom));
    }
    public Vec2d switchToGame() {
        return new Vec2d(Util.switchXToGame(x), Util.switchYToGame(y));
    }
    public Vec2d switchToGame(double zoom) {
        return new Vec2d(Util.switchXToGame(x,zoom), Util.switchYToGame(y,zoom));
    }
    public Vec2d switchToGame1() {
        return new Vec2d((x - (double) Screen.sc.windowWidth / 2)/Screen.sc.getRealZoom(), (-y + (double) Screen.sc.windowHeight / 2)/Screen.sc.getRealZoom());
    }

    public Vec2d limit(double l) {
        if (this.length() <= 0.0001) return this;
        double e = l / length();
        return this.multiply(e);
    }
    public Vec2d limitOnlyOver(double l){
        if (this.length() <= l) return this.copy();
        return this.limit(l);
    }
    public double dot(Vec2d pos) {
        return x * pos.x + y * pos.y;
    }
    public double cross(Vec2d pos) {
        return x * pos.y - y * pos.x;
    }
    public double angle(){
        if(this.length()<=0.00000001) return 0;
        return Math.toDegrees(Math.atan2(y, x));
    }
    public double distanceTo(Vec2d pos) {
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
    public Vec2d rotate(double angle) {
        double cos = Util.cos(angle);
        double sin = Util.sin(angle);
        return new Vec2d(x * cos - y * sin, x * sin + y * cos);
    }
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("x",Util.getRoundedDouble(x,1));
        json.put("y",Util.getRoundedDouble(y,1));
        return json;
    }
    public BlockPos ofFloor(){
        return BlockPos.ofFloor(this);
    }
    public static Vec2d fromJSON(JSONObject json) {
        return new Vec2d(json.getDouble("x"), json.getDouble("y"));
    }

    public Vec2d opposite() {
        return new Vec2d(-x,-y);
    }
}