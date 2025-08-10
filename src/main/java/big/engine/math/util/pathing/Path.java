package big.engine.math.util.pathing;

import big.engine.math.Vec2d;
import big.engine.math.util.Util;
import big.modules.entity.Entity;


import java.util.*;
import java.util.concurrent.*;

import static big.engine.modules.EngineMain.cs;

public class Path {
    public static float REMOVENODEDISTANCE = 0.3;
    //public static PathNode SPACE = new PathNode(new Vec2d(0, 114514, 0), 114514, 114514, null);
    public volatile ArrayList<PathNode> path;
    public volatile ArrayList<PathNode> path1;
    public Vec2d end;
    public Vec2d from;
    public Path parent = null;
    public static int FIXSTEP = 20;
    public static int FIXINTERVAL = 10;
    public static int INDEX = 0;
    public static int STOPFIX = 10;

    public boolean started = false;
    public Calculator calculator;
    public Entity owner;
    //public ArrayList<Vec2d> tried;
    public Path(ArrayList<PathNode> path, Vec2d end, Vec2d from, Calculator calculator,Entity owner) {
        this.path = path;
        this.path1 = null;
        this.end = end;
        this.from = from;
        this.calculator = calculator;
        this.owner=owner;
        //tried=(ArrayList<Vec2d>) calculator.TRIED.clone();
        update();
    }

    public Path(ArrayList<PathNode> path, Vec2d end, Vec2d from, Path parent, Calculator calculator,Entity owner) {
        this.path = path;
        this.end = end;
        this.from = from;
        this.parent = parent;
        this.calculator = calculator;
        this.owner=owner;
        //tried= (ArrayList<Vec2d>) calculator.TRIED.clone();
        update();
    }

    public void setParent(Path parent) {
        this.parent = parent;
    }

    public void update() {
        if (path.isEmpty()) return;
        int index = -1;
        for (int i = 0; i < path.size(); i++) {
            Vec2d v = path.get(i).pos;
            Vec2d p = owner.getPos();
            float dx = v.x - p.x;
            float dy = v.y - p.y;
            if ((dx * dx + dy * dy <= REMOVENODEDISTANCE)) index = i;
        }
        if (index >= 0) {
            for (int i = 0; i <= index; i++) {
                remove(0);
                INDEX--;
            }
        }
        if (path1 != null) {
            index = -1;
            for (int i = 0; i < path1.size(); i++) {
                Vec2d v = path1.get(i).pos;
                Vec2d p = owner.getPos();
                float dx = v.x - p.x;
                float dy = v.y - p.y;
                if ((dx * dx + dy * dy <= REMOVENODEDISTANCE)) index = i;
                //if (calculator.isNeighbour(center(p), v)) index = i - 1;
            }
            if (index >= 0) {
                for (int i = 0; i <= index; i++) {
                    path1.remove(0);
                    INDEX--;
                }
            }
        }
        /*if(!started&&trapped()){
            started=true;
            Thread thread=new Thread(()->{
                Path p=calculator.getPath(owner.getPos(),getMoveToNow());
                if(p!=null&&p.path!=null){
                    path.addAll(0,p.path);
                    INDEX+=p.path.size();
                }
                started=false;
            });
            thread.start();
        }*/
    }

    public boolean reached() {
        return owner.getPos().distanceTo(end) < cs.serverController.blockSize * 1.5;
    }

    public void recalculate() {
        if (canUse()) return;
        Path p = calculator.getPath(from, end);
        if (p == null) return;
        this.path = p.path;
    }

    public boolean canUse() {
        for (PathNode node : path) {
            if (!calculator.isValidPos(node.pos)) return false;
        }
        return true;
    }

    public Vec2d getMoveToNow() {
        return getMoveTo() == null ? (owner.getPos().distanceTo(end) < REMOVENODEDISTANCE ? end : owner.getPos()) : getMoveTo();
    }

    public boolean pathEnded() {
        return get(0) == null || owner.getPos().distanceTo(get(0)) < REMOVENODEDISTANCE;
    }

    public Vec2d getMoveTo() {
        if (path == null || path.isEmpty()) return null;
        /*
        for(int i=0;i<=path.size();i++){
            Vec2d vec1=get(i);
            Vec2d vec2=get(i+1);
            if(vec1.equals(vec2)) return vec1;
            if(canMoveTo(vec1)&&!canMoveTo(vec2)){
                return vec1;
            }
        }*/
        update();
        Vec2d from = new Vec2d(owner.getPos().x, owner.getPos().y);
        if (path != null && path.size() > 1 && calculator.isNeighbour(from, get(1))) {
            Vec2d v = get(1);
            if (path1 == null) {
                remove(0);
                INDEX--;
            } else {
                path1.remove(0);
            }
            return v;
        }
        return get(0);
    }

    public Vec2d get(int i) {
        if (path1 != null) {
            if (i < path1.size()) return path1.get(i).pos;
            else return path.get(0).pos;
        }
        if (i < path.size()) return path.get(i).pos;
        else return end;
    }

    public Vec2d getLast() {
        return path.isEmpty()?owner.getPos():path.get(path.size() - 1).pos;
    }

    public void remove(int i) {
        if (i >= path.size()) return;
        path.remove(i);
    }

    public void add(Path p) {
        for (PathNode node : p.path) {
            add(node);
        }
    }
    public boolean isStillValid(){
        Vec2d last=null;
        for(PathNode node:path){
            if(!calculator.isValidPos(node.pos)) return false;
            if(last!=null&&!calculator.isValidPos(Util.lerp(last,node.pos,0.5))) return false;
            last=node.pos;
        }
        return true;
    }

    public float getLength() {
        float length = 0;
        PathNode last = null;
        for (PathNode pathNode : path) {
            if (last != null) length += last.pos.distanceTo(pathNode.pos);
            last = pathNode;
        }
        return length;
    }

    public float getLengthTotal() {
        float length = 0;
        PathNode last = null;
        Path p = this;
        for (PathNode pathNode : this.path) {
            if (last != null) length += last.pos.distanceTo(pathNode.pos);
            last = pathNode;
        }
        while (p.parent != null) {
            last = null;
            for (PathNode pathNode : p.parent.path) {
                if (last != null) length += last.pos.distanceTo(pathNode.pos);
                last = pathNode;
            }
            p = p.parent;
        }
        return length;
    }

    public void add(PathNode vec) {
        this.path.add(vec);
    }

    public Path copy() {
        return new Path(path, end, from, parent, calculator,owner);
    }

    public Path getPath() {
        Stack<Path> parentStack = new Stack<>();
        ArrayList<PathNode> result = new ArrayList<>();
        Path p = this;
        parentStack.add(this);
        while (p.parent != null) {
            parentStack.add(p.parent);
            p = p.parent;
        }
        while (!parentStack.isEmpty()) {
            result.addAll(parentStack.peek().path);
            parentStack.pop();
        }
        return new Path(result, end, from, this.calculator,owner);
    }




}
