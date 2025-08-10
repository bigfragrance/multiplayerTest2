package big.engine.math.util;

import big.engine.math.Vec2d;

public class LineIntersection {
    public static boolean intersects(Vec2d p1, Vec2d p2, Vec2d p3, Vec2d p4) {
        return isIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }
    public static boolean isIntersect(
            double x1, double y1, double x2, double y2,
            double x3, double y3, double x4, double y4) {


        double d1 = direction(x3, y3, x4, y4, x1, y1);
        double d2 = direction(x3, y3, x4, y4, x2, y2);
        double d3 = direction(x1, y1, x2, y2, x3, y3);
        double d4 = direction(x1, y1, x2, y2, x4, y4);


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


    private static double direction(double xi, double yi, double xj, double yj, double xk, double yk) {
        return (xk - xi) * (yj - yi) - (xj - xi) * (yk - yi);
    }


    private static boolean onSegment(double xi, double yi, double xj, double yj, double xk, double yk) {
        return Math.min(xi, xj) <= xk && xk <= Math.max(xi, xj) &&
               Math.min(yi, yj) <= yk && yk <= Math.max(yi, yj);
    }
}