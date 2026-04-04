package big.engine.math;

import big.game.world.ChunkPos;

import java.util.HashSet;

public class Vec2i {
    public final int x;
    public final int y;
    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Vec2i add(int x, int y){
        return new Vec2i(this.x+x,this.y+y);
    }
    public Vec2i add(Vec2i pos){
        return add(pos.x,pos.y);
    }
    public Vec2i down() {
        return add(0,-1);
    }
    public Vec2i up() {
        return add(0,1);
    }
    public Vec2i left() {
        return add(-1,0);
    }
    public Vec2i right() {
        return add(1,0);
    }
    public Vec2i subtract(int x, int y){
        return add(-x,-y);
    }
    public Vec2d toCenterPos(){
        return new Vec2d(x+0.5,y+0.5);
    }
    public Vec2i offset(Direction dir) {
        return add(dir.getOffset());
    }
    public Box toBox(){
        return new Box(this);
    }
    public static Vec2i ofFloor(double x, double y){
        return new Vec2i((int) Math.floor(x), (int) Math.floor(y));
    }
    public static Vec2i ofFloor(Vec2d vec){
        return ofFloor(vec.x,vec.y);
    }
    public long toLong(){
        return ChunkPos.toLong(x,y);
    }
    @Override
    public boolean equals(Object o){
        if(this==o)
            return true;
        if(o instanceof Vec2i pos) {
            return this.x == pos.x && this.y == pos.y;
        }
        else{
            return false;
        }
    }
    public int hashCode() {
        int result = 31;
        result = 65535 * result + x;
        result = 65535 * result + y;
        return result;
    }
    public double distanceTo(Vec2i pos) {
        return Math.sqrt((x-pos.x)*(x-pos.x)+(y-pos.y)*(y-pos.y));
    }

    public Vec2d toVec2d() {
        return new Vec2d(x,y);
    }



}
