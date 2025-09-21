package big.engine.math;

import big.modules.world.ChunkPos;

public class BlockPos {
    public final int x;
    public final int y;
    public BlockPos(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public BlockPos add(int x, int y){
        return new BlockPos(this.x+x,this.y+y);
    }
    public BlockPos add(BlockPos pos){
        return add(pos.x,pos.y);
    }
    public BlockPos subtract(int x, int y){
        return add(-x,-y);
    }
    public Vec2d toCenterPos(){
        return new Vec2d(x+0.5,y+0.5);
    }
    public Box toBox(){
        return new Box(this);
    }
    public static BlockPos ofFloor(double x, double y){
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y));
    }
    public static BlockPos ofFloor(Vec2d vec){
        return ofFloor(vec.x,vec.y);
    }
    public long toLong(){
        return ChunkPos.toLong(x,y);
    }
    @Override
    public boolean equals(Object o){
        if(this==o)
            return true;
        if(o instanceof BlockPos pos) {
            return this.x == pos.x && this.y == pos.y;
        }
        else{
            return false;
        }
    }
    public int hashCode() {
        int result = 17;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }
    public double distanceTo(BlockPos pos) {
        return Math.sqrt((x-pos.x)*(x-pos.x)+(y-pos.y)*(y-pos.y));
    }
}
