package engine.math;

public class BlockPos2d {
    public int x;
    public int y;
    public BlockPos2d(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public BlockPos2d add(int x, int y){
        return new BlockPos2d(this.x+x,this.y+y);
    }
    public BlockPos2d add(BlockPos2d pos){
        return add(pos.x,pos.y);
    }
    public BlockPos2d subtract(int x, int y){
        return add(-x,-y);
    }
    public Vec2d toCenterPos(){
        return new Vec2d(x+0.5,y+0.5);
    }
    public static BlockPos2d ofFloor(double x,double y){
        return new BlockPos2d((int) Math.floor(x), (int) Math.floor(y));
    }
    public static BlockPos2d ofFloor(Vec2d vec){
        return ofFloor(vec.x,vec.y);
    }
    @Override
    public boolean equals(Object o){
        if(this==o)
            return true;
        if(o instanceof BlockPos2d pos) {
            return this.x == pos.x && this.y == pos.y;
        }
        else{
            return false;
        }
    }
}
