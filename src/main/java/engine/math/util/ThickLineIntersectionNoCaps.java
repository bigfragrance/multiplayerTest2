package engine.math.util;

import engine.math.Vec2d;

public class ThickLineIntersectionNoCaps {
    public static void main(String[] a){
        Vec2d p1=new Vec2d(0,0);
        Vec2d p2=new Vec2d(1,1);
        Vec2d p3=new Vec2d(1,0);
        Vec2d p4=new Vec2d(0,1);
        System.out.println(isThickLineIntersect(p1,p2,0,p3,p4,0));
    }
    public static boolean isThickLineIntersect(Vec2d p1,Vec2d p2,double w1,Vec2d p3,Vec2d p4,double w2){
        return thickLineIntersect(p1.x,p1.y,p2.x,p2.y,w1,p3.x,p3.y,p4.x,p4.y,w2);
    }
    public static boolean thickLineIntersect(
            double x1, double y1, double x2, double y2, double w1,
            double x3, double y3, double x4, double y4, double w2) {

        double dist = segmentToSegmentDistance(x1, y1, x2, y2, x3, y3, x4, y4);
        return dist <= (w1 + w2);
    }


    private static double segmentToSegmentDistance(
            double x1, double y1, double x2, double y2,
            double x3, double y3, double x4, double y4) {


        double ux = x2 - x1, uy = y2 - y1;
        double vx = x4 - x3, vy = y4 - y3;
        double wx = x1 - x3, wy = y1 - y3;

        double a = ux * ux + uy * uy; // u·u
        double b = ux * vx + uy * vy; // u·v
        double c = vx * vx + vy * vy; // v·v
        double d = ux * wx + uy * wy; // u·w
        double e = vx * wx + vy * wy; // v·w

        double denom = a * c - b * b;
        double sc, sN, sD = denom;
        double tc, tN, tD = denom;

        if (denom < 1e-12) {
            sN = 0.0;
            sD = 1.0;
            tN = e;
            tD = c;
        } else {
            sN = (b * e - c * d);
            tN = (a * e - b * d);
            if (sN < 0) { sN = 0; tN = e; tD = c; }
            else if (sN > sD) { sN = sD; tN = e + b; tD = c; }
        }

        if (tN < 0) {
            tN = 0;
            if (-d < 0) sN = 0;
            else if (-d > a) sN = sD;
            else { sN = -d; sD = a; }
        } else if (tN > tD) {
            tN = tD;
            if ((-d + b) < 0) sN = 0;
            else if ((-d + b) > a) sN = sD;
            else { sN = (-d + b); sD = a; }
        }

        sc = (Math.abs(sN) < 1e-12 ? 0 : sN / sD);
        tc = (Math.abs(tN) < 1e-12 ? 0 : tN / tD);

        double dx = wx + sc * ux - tc * vx;
        double dy = wy + sc * uy - tc * vy;

        return Math.hypot(dx, dy);
    }

}