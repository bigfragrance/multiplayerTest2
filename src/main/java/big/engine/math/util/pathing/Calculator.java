package big.engine.math.util.pathing;

import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.EntityUtils;
import big.modules.entity.Entity;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static big.engine.modules.EngineMain.cs;


public class Calculator {
    public Vec2d from;
    public Vec2d to;
    public double STEP = 0.5;
    public long TIMEOUT = 1;
    public long start = 0;
    public double VALIDDISTANCE = 0.8;
    public boolean CENTER = false;
    public Entity owner;
    public Calculator(Entity owner) {
        this.owner = owner;
    }

    /*
    public Path getPath(Vec2d f, Vec2d t){
        worldMinY = cs.world.getDimension().minY();
        start = System.currentTimeMillis();
        from = f;
        to = t;
        boolean found = false;
        ArrayList<PathNode> pathNodes = new ArrayList<>();
        ArrayList<Vec2d> pathPoses=new ArrayList<>();
        ArrayList<Vec2d> tried = new ArrayList<>();
        pathNodes.add(new PathNode(from, 0, getVTo(from),null));
        //ArrayList<PathNode> openList=new ArrayList<>();
        while (!found && System.currentTimeMillis() - start <= TIMEOUT&&!pathNodes.isEmpty()) {
            for(PathNode p:pathNodes){
                if(p.pos.distanceTo(to)<=VALIDDISTANCE){
                    return p.getPath();
                }
            }
            PathNode lastBest=getMinTotal(pathNodes);
            if (lastBest == null) break;
            RenderUtils.renderTickingBox(getPlayerBox(lastBest.pos),new Color(255,0,0,50),new Color(255,0,0,255), ShapeMode.Both,0.1);
            tried.add(lastBest.pos);

            Vec2d vec=lastBest.pos;
            for (int i = 0; i <= 2; i++) {
                for (int j = 1; j <= 8; j++) {
                    Vec2d v = offset(vec, j, STEP).add(0, yOffset(i), 0);
                    if (i == 0) {
                        v = toGround(v);
                    } else if (i == 2) {
                        v = toAir(v);
                    }
                    if (v == null || tried.contains(v) || !isValidPos(v)) continue;
                    PathNode pathNode = new PathNode(lastBest, v);
                    if(!tried.contains(pathNode.pos)) continue;
                    PathNode node=getSamePos(pathNodes,pathNode.pos);
                    if(node==null){
                        pathNodes.add(pathNode);
                        pathPoses.add(pathNode.pos);
                    }
                    else{
                        PathNode n=new PathNode(lastBest,pathNode.pos);
                        if(n.f<node.f){
                            node.parent=lastBest;
                        }
                    }
                }
            }
            pathNodes.remove(lastBest);
            /*
            PathNode last = pathNodes.get(pathNodes.size() - 1);
            Vec2d vec = last.pos;
            PathNode best = null;
            for (int i = 0; i <= 2; i++) {
                for (int j = 1; j <= 8; j++) {
                    Vec2d v = offset(vec, j, STEP).add(0, yOffset(i), 0);
                    if (i == 0) {
                        v = toGround(v);
                    } else if (i == 2) {
                        v = toAir(v);
                    }
                    if (v == null || tried.contains(v) || !isValidPos(v)) continue;
                    PathNode pathNode = new PathNode(last, v);
                    if (pathNode.isBetterThan(best)) best = pathNode;
                }
            }
            if (best == null) {
                pathNodes.remove(pathNodes.size() - 1);
            } else {
                RenderUtils.renderTickingBox(getPlayerBox(best.pos),new Color(255,0,0,50),new Color(255,0,0,255), ShapeMode.Both,0.1);
                tried.add(best.pos);
                pathNodes.add(best);
                if (best.pos.distanceTo(to) <= VALIDDISTANCE) {
                    found = true;
                }
                /*try {
                    Thread.sleep(100);
                }
                catch (Exception ignored){}
            }
        }
        log(String.valueOf(System.currentTimeMillis()-start));
        return found ? new Path(pathNodes) : null;
    }*/
    public Path getPath(Vec2d f, Vec2d t, double validDistance, boolean closest, long timeout) {
        if (f == null || t == null) return null;
        start = System.currentTimeMillis();
        from = f;
        to = t;
        ArrayList<PathNode> pathNodes = new ArrayList<>();
        ArrayList<Vec2d> tried = new ArrayList<>();
        pathNodes.add(new PathNode(from, 0, getVTo(from), null, this));
        while (System.currentTimeMillis() - start <= timeout && !pathNodes.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            for (PathNode p : pathNodes) {
                if (p.pos.distanceTo(to) <= validDistance) {
                    return p.getPath(to);
                }
            }
            PathNode lastBest = getMinTotal(pathNodes);
            if (lastBest == null) break;
            tried.add(lastBest.pos);
            Vec2d vec = lastBest.pos;

            for (int j = 1; j <= 8; j++) {
                Vec2d v = offset(vec, j, STEP);
                if (v == null || tried.contains(v) || !isValidPos(v)) continue;
                if (method_114514(j)) {
                    Vec2d v2 = offset(vec, j, STEP / 2);
                    if (v2 == null || !isValidPos(v2)) continue;
                }
                PathNode pathNode = new PathNode(lastBest, v, this);

                PathNode node = getSamePos(pathNodes, pathNode.pos);
                if (node == null) {
                    pathNodes.add(pathNode);
                } else if (pathNode.f < node.f) {
                    node.parent = lastBest;
                }
            }
            pathNodes.remove(lastBest);
        }
        if (closest) {
            PathNode best = null;
            double bestDistance = Double.MAX_VALUE;
            for (PathNode p : pathNodes) {
                if (p.pos.distanceTo(to) <= bestDistance) {
                    best = p;
                    bestDistance = p.pos.distanceTo(to);
                }
            }
            if (best != null) {
                return best.getPath(to);
            }
        }
        //log(String.valueOf(System.currentTimeMillis() - start));
        return null;
    }


    public boolean isNeighbour(Vec2d v1, Vec2d v2) {
        for (int j = 1; j <= 8; j++) {
            Vec2d v = offset(v1, j, STEP);
            if (v == null || !isValidPos(v)) continue;
            if (method_114514(j)) {
                Vec2d vv2 = offset(v1, j, STEP / 2);
                if (vv2 == null || !isValidPos(vv2)) continue;
            }
            if (v.distanceTo(v2) < 0.1) return true;
        }
        return false;
    }


    public PathNode getMinTotal(ArrayList<PathNode> pathNodes) {
        PathNode best = null;
        for (PathNode pathNode : pathNodes) {
            if (pathNode.isBetterThan(best)) {
                best = pathNode;
            }
        }
        return best;
    }

    public PathNode getSamePos(ArrayList<PathNode> pathNodes, Vec2d pos) {
        for (PathNode node : pathNodes) {
            if (node.pos.equals(pos)) return node;
        }
        return null;
    }

    public double getVFrom(Vec2d pos) {
        return pos.distanceTo(from);
    }

    public double getV(Vec2d p1, Vec2d p2) {
        return p1.distanceTo(p2);
    }

    public double getVTo(Vec2d pos) {
        //return Math.abs(pos.x - to.x) + Math.abs(pos.y - to.y) + Math.abs(pos.z - to.z);
        return pos.distanceTo(to);
    }

    public double getVTotal(Vec2d pos) {
        return getVTo(pos) + getVFrom(pos);
    }

    public Box getPlayerBox(Vec2d pos) {
        return new Box(pos,owner.boundingBox.xSize()/2,owner.boundingBox.ySize()/2);
    }


    public boolean isValidPos(Vec2d pos) {
        return !isInside(pos);
    }

    public boolean isInside(Vec2d pos) {
        return inside(getPlayerBox(pos));
    }

    public boolean inside(Box bb) {
        return EntityUtils.isInsideWall(bb);
    }


    public Vec2d offset(Vec2d vec, int type, double s) {
        switch (type) {
            /*case(0)->{
                return vec.add(0,s);
            }
            case(1)->{
                return vec.add(s, 0);
            }
            case(2)->{
                return vec.add(0, -s);
            }
            case(3)->{
                return vec.add(-s, 0);
            }*/
            case (1) -> {
                return vec.add(s, 0);
            }
            case (2) -> {
                return vec.add(s, s);
            }
            case (3) -> {
                return vec.add(0,s);
            }
            case (4) -> {
                return vec.add(-s,s);
            }
            case (5) -> {
                return vec.add(-s,0);
            }
            case (6) -> {
                return vec.add(-s,-s);
            }
            case (7) -> {
                return vec.add(0,-s);
            }
            case (8) -> {
                return vec.add(s, -s);
            }
        }
        return null;
    }

    public boolean method_114514(int i) {
        return i == 2 || i == 4 || i == 6 || i == 8;
    }


    public Path getPath(Vec2d v1, Vec2d v2) {
        return getPath(v1, v2, VALIDDISTANCE, true, TIMEOUT);
    }

    public Path getPath(Vec2d v1, Vec2d v2, double validDistance) {
        return getPath(v1, v2, validDistance, false, TIMEOUT);
    }

    public Path getPath(Vec2d v1, Vec2d v2, boolean closest) {
        return getPath(v1, v2, VALIDDISTANCE, closest, TIMEOUT);
    }

    public Path getPath(Vec2d v1, Vec2d to, double validDistance, boolean closest) {
        return getPath(v1, to, validDistance, closest, TIMEOUT);
    }
}
