package big.engine.math.util;

import big.engine.math.Vec2d;

public class LineIntersection {
    public static boolean intersects(Vec2d p1, Vec2d p2, Vec2d p3, Vec2d p4) {
        return isIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }
    public static boolean isIntersect(
            float x1, float y1, float x2, float y2,
            float x3, float y3, float x4, float y4) {


        float d1 = direction(x3, y3, x4, y4, x1, y1);
        float d2 = direction(x3, y3, x4, y4, x2, y2);
        float d3 = direction(x1, y1, x2, y2, x3, y3);
        float d4 = direction(x1, y1, x2, y2, x4, y4);


        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }


        if (d1 == 0 && onSegment(x3, y3, x4, y4, x1, y1)) return true;
        if (d2 == 0 && onSegment(x3, y3, x4, y4, x2, y2)) return true;
        if (d3 == 0 && onSegment(x1, y1, x2, y2, x3, y3)) return true;
        if (d4 == 0 && onSegment(x1, y1, x2, y2, x4, y4)) return true;

        return false;
    }


    private static float direction(float xi, float yi, float xj, float yj, float xk, float yk) {
        return (xk - xi) * (yj - yi) - (xj - xi) * (yk - yi);
    }


    private static boolean onSegment(float xi, float yi, float xj, float yj, float xk, float yk) {
        return Math.min(xi, xj) <= xk && xk <= Math.max(xi, xj) &&
               Math.min(yi, yj) <= yk && yk <= Math.max(yi, yj);
    }
}