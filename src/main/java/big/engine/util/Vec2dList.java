package big.engine.util;

import big.engine.math.Vec2d;

import java.util.ArrayList;

public class Vec2dList {
    private final ArrayList<Vec2d> list;
    public Vec2dList(){
        list=new ArrayList<>();
    }
    public void add(Vec2d v){
        list.add(v);
    }
    public Vec2d get(int i){
        return list.get(i);
    }
    public int size(){
        return list.size();
    }
    public void clear(){
        list.clear();
    }
    public ArrayList<Vec2d> getList(){
        return list;
    }
    public void plus(Vec2d vec){
        for(Vec2d v:list){
            v=v.add(vec);
        }
    }
    public void multiply(Vec2d multi,Vec2d center){
        for(Vec2d v:list){
            v.set(v.subtract(center).multiply(multi).add(center));
        }
    }
    public void rotate(double angle,Vec2d center){
        for(Vec2d v:list){
            v.set(v.subtract(center).rotate(angle).add(center));
        }
    }
    public void switchToJFrame(){
        for(Vec2d v:list){
            v.set(v.switchToJFrame());
        }
    }
    public void switchToJFrame(double zoom){
        for(Vec2d v:list){
            v.set(v.switchToJFrame(zoom));
        }
    }
    public void switchToGame(){
        for(Vec2d v:list){
            v.set(v.switchToGame());
        }
    }
     public void switchToGame(double zoom){
        for(Vec2d v:list){
            v.set(v.switchToGame(zoom));
        }
    }
}
