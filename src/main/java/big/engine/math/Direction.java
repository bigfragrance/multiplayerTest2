package big.engine.math;

public enum Direction {
    RIGHT("right",1,0,0),
    UP("up",0,1,90),
    LEFT("left",-1,0,180),
    DOWN("down",0,-1,-90);
    private final String name;
    private final int x;
    private final int y;
    private final Vec2i offset;
    private final double angle;
    private Direction(String name,int x,int y,double angle){
        this.name=name;
        this.x=x;
        this.y=y;
        this.offset=new Vec2i(x,y);
        this.angle=angle;
    }
    public String getName(){
        return name;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public Vec2i getOffset(){
        return offset;
    }
    public static Direction fromName(String name){
        return Enum.valueOf(Direction.class,name);
    }
    public static Direction fromID(int id){
        return values()[id];
    }

    public double getAngle() {
        return angle;
    }
}
