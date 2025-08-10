package big.engine.math.util;

import big.engine.math.Box;
import big.engine.math.Vec2d;

public class SegmentBoxIntersectionChecker {


    public static boolean segmentIntersectsBox(Vec2d segmentStart, Vec2d segmentEnd, Box box) {

        if (box.contains(segmentStart) || box.contains(segmentEnd)) {
            return true;
        }


        Vec2d[] leftEdge = {new Vec2d(box.minX, box.minY), new Vec2d(box.minX, box.maxY)};
        Vec2d[] rightEdge = {new Vec2d(box.maxX, box.minY), new Vec2d(box.maxX, box.maxY)};
        Vec2d[] bottomEdge = {new Vec2d(box.minX, box.minY), new Vec2d(box.maxX, box.minY)};
        Vec2d[] topEdge = {new Vec2d(box.minX, box.maxY), new Vec2d(box.maxX, box.maxY)};


        return segmentsIntersect(segmentStart, segmentEnd, leftEdge[0], leftEdge[1]) ||
               segmentsIntersect(segmentStart, segmentEnd, rightEdge[0], rightEdge[1]) ||
               segmentsIntersect(segmentStart, segmentEnd, bottomEdge[0], bottomEdge[1]) ||
               segmentsIntersect(segmentStart, segmentEnd, topEdge[0], topEdge[1]);
    }


    private static boolean segmentsIntersect(Vec2d a1, Vec2d a2, Vec2d b1, Vec2d b2) {

        Vec2d aDir = a2.subtract(a1);
        Vec2d bDir = b2.subtract(b1);

        double cross1 = crossProduct(aDir, b1.subtract(a1));
        double cross2 = crossProduct(aDir, b2.subtract(a1));
        double cross3 = crossProduct(bDir, a1.subtract(b1));
        double cross4 = crossProduct(bDir, a2.subtract(b1));


        if (cross1 * cross2 > 1e-8) return false;
        if (cross3 * cross4 > 1e-8) return false;


        if (isCollinear(a1, a2, b1, b2)) {
            return checkCollinearOverlap(a1, a2, b1, b2);
        }


        return isPointOnSegment(a1, a2, b1) ||
               isPointOnSegment(a1, a2, b2) ||
               isPointOnSegment(b1, b2, a1) ||
               isPointOnSegment(b1, b2, a2);
    }


    private static double crossProduct(Vec2d v1, Vec2d v2) {
        return v1.x * v2.y - v1.y * v2.x;
    }


    private static boolean isCollinear(Vec2d a, Vec2d b, Vec2d c, Vec2d d) {
        return Math.abs(crossProduct(b.subtract(a), c.subtract(a))) < 1e-8 &&
               Math.abs(crossProduct(b.subtract(a), d.subtract(a))) < 1e-8;
    }


    private static boolean checkCollinearOverlap(Vec2d a1, Vec2d a2, Vec2d b1, Vec2d b2) {
        double aMinX = Math.min(a1.x, a2.x);
        double aMaxX = Math.max(a1.x, a2.x);
        double bMinX = Math.min(b1.x, b2.x);
        double bMaxX = Math.max(b1.x, b2.x);
        if (aMaxX < bMinX - 1e-8 || bMaxX < aMinX - 1e-8) return false;

        double aMinY = Math.min(a1.y, a2.y);
        double aMaxY = Math.max(a1.y, a2.y);
        double bMinY = Math.min(b1.y, b2.y);
        double bMaxY = Math.max(b1.y, b2.y);
        if (aMaxY < bMinY - 1e-8 || bMaxY < aMinY - 1e-8) return false;

        return true;
    }


    private static boolean isPointOnSegment(Vec2d segStart, Vec2d segEnd, Vec2d point) {
        if (point.x < Math.min(segStart.x, segEnd.x) - 1e-8 || 
            point.x > Math.max(segStart.x, segEnd.x) + 1e-8) {
            return false;
        }
        if (point.y < Math.min(segStart.y, segEnd.y) - 1e-8 || 
            point.y > Math.max(segStart.y, segEnd.y) + 1e-8) {
            return false;
        }
        return Math.abs(crossProduct(segEnd.subtract(segStart), point.subtract(segStart))) < 1e-8;
    }
}