package engine.math.util;


import engine.math.Vec2d;

public class TriangleIntersectionChecker {

    public static boolean areTrianglesIntersecting(Vec2d[] triA, Vec2d[] triB) {

        Vec2d[] axes = collectSeparationAxes(triA, triB);
        

        for (Vec2d axis : axes) {
            if (!projectionsOverlap(triA, triB, axis)) {
                return false;
            }
        }
        return true;
    }
 

    private static Vec2d[] collectSeparationAxes(Vec2d[] triA, Vec2d[] triB) {
        Vec2d[] axes = new Vec2d[6];
        int index = 0;

        for (int i = 0; i < 3; i++) {
            Vec2d edge = getEdgeNormal(triA[i], triA[(i+1)%3]);
            axes[index++] = edge;
        }
        

        for (int i = 0; i < 3; i++) {
            Vec2d edge = getEdgeNormal(triB[i], triB[(i+1)%3]);
            axes[index++] = edge;
        }
        return axes;
    }
 

    private static Vec2d getEdgeNormal(Vec2d p1, Vec2d p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        Vec2d rawNormal = new Vec2d(-dy, dx);
        

        double length = Math.sqrt(rawNormal.x  * rawNormal.x + rawNormal.y * rawNormal.y);
        return new Vec2d(rawNormal.x / length, rawNormal.y / length);
    }
 

    private static boolean projectionsOverlap(Vec2d[] triA, Vec2d[] triB, Vec2d axis) {
        Projection projA = getProjection(triA, axis);
        Projection projB = getProjection(triB, axis);
        return projA.max  >= projB.min  && projB.max  >= projA.min; 
    }

    private static Projection getProjection(Vec2d[] triangle, Vec2d axis) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        
        for (Vec2d point : triangle) {
            double projection = point.x * axis.x + point.y * axis.y;
            min = Math.min(min,  projection);
            max = Math.max(max,  projection);
        }
        return new Projection(min, max);
    }
 

    private static class Projection {
        final double min;
        final double max;
        
        Projection(double min, double max) {
            this.min  = min;
            this.max  = max;
        }
    }

}