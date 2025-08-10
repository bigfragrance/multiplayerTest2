package big.engine.math.util.pathing;

import big.engine.math.Vec2d;


import java.util.ArrayList;

public class PathNode {
    public Vec2d pos;
    float g;
    float f;
    PathNode parent;
    public boolean startJump = false;
    public boolean jumping = false;
    public Vec2d startJumpPos = null;
    public boolean onGround = true;
    public Calculator calculator;

    public PathNode(Vec2d pos, float g, float f, PathNode parent, Calculator calculator) {
        this.pos = pos;
        this.g = g;
        this.f = f;
        this.parent = parent;
        if (parent != null) {
            this.startJumpPos = parent.startJumpPos;
        }
        this.calculator = calculator;
    }

    public PathNode(PathNode pathNode, Vec2d pos, Calculator calculator) {
        this(pos, (pathNode == null ? 0 : pathNode.g) + calculator.getV((pathNode == null ? calculator.from : pathNode.pos), pos), calculator.getVTo(pos), pathNode, calculator);
    }

    public PathNode(Vec2d pos, Calculator calculator) {
        this(pos, calculator.getVFrom(pos), calculator.getVTo(pos), null, calculator);
    }

    public boolean isBetterThan(PathNode pathNode) {
        if (pathNode == null) return true;
        return this.getTotal() < pathNode.getTotal();
    }

    public static float xyDistance(Vec2d v1, Vec2d v2) {
        return Math.sqrt(Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2));
    }

    public float getTotal() {
        return f + g;
    }

    public Path getPath(Vec2d end) {
        ArrayList<PathNode> result = new ArrayList<>();
        boolean done = false;
        PathNode node = this;
        result.add(node);
        while (node.parent != null) {
            result.add(0, node.parent);
            node = node.parent;
        }
        return new Path(result, end, this.pos, this.calculator,calculator.owner);
    }
}
