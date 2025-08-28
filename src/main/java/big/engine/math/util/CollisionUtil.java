package big.engine.math.util;

import big.engine.math.Box;
import big.engine.math.Vec2d;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class CollisionUtil {
    public static List<Line2D.Double> getSegments(List<CheckContent> boxes){
        List<Line2D.Double> segments=new ArrayList<>();
        for(CheckContent c:boxes){
            Box box=c.box;
            if(c.right)segments.add(new Line2D.Double(box.maxX,box.minY,box.maxX,box.maxY));
            if(c.top)segments.add(new Line2D.Double(box.minX,box.maxY,box.maxX,box.maxY));
            if(c.left)segments.add(new Line2D.Double(box.minX,box.minY,box.minX,box.maxY));
            if(c.bottom)segments.add(new Line2D.Double(box.maxX,box.minY,box.minX,box.minY));
        }
        return segments;
    }
    public static Vec2d getMaxMove(Vec2d position,double rad,Vec2d velocity,List<CheckContent> boxes){
        return (getMaxMove1(position,rad,velocity,getSegments(boxes)));
    }
    public static Vec2d getMaxMove0(Vec2d position,double rad,Vec2d velocity,List<Line2D.Double> segments){
        return new Vec2d(getMaxMove(position.x,position.y,rad,velocity.x,velocity.y,segments));
    }
    /**
     * Compute maximum safe movement of a circle against a set of line segments.
     * Keeps the "push out" effect if the circle starts overlapping.
     *
     * @param x      circle center x
     * @param y      circle center y
     * @param radius circle radius
     * @param xVel   desired movement in x
     * @param yVel   desired movement in y
     * @param segments list of line segments
     * @return double[]{dx, dy} the maximal allowed movement this step
     */
    public static double[] getMaxMove(double x, double y, double radius,
                                      double xVel, double yVel,
                                      List<Line2D.Double> segments) {
        double moveX = xVel;
        double moveY = yVel;

        // Iterate multiple times for resolving multiple constraints
        for (int iter = 0; iter < 5; iter++) {
            boolean corrected = false;

            double newX = x + moveX;
            double newY = y + moveY;

            for (Line2D.Double seg : segments) {
                // Find closest point on segment to new circle center
                Point2D closest = closestPointOnSegment(seg, newX, newY);
                double dx = newX - closest.getX();
                double dy = newY - closest.getY();
                double dist2 = dx * dx + dy * dy;

                if (dist2 < radius * radius - 1e-9) {
                    double dist = Math.sqrt(dist2);
                    double overlap = radius - dist;
                    if (dist < 1e-6) {
                        // Circle center exactly on line point: pick arbitrary normal
                        dx = seg.y2 - seg.y1;
                        dy = -(seg.x2 - seg.x1);
                        dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist < 1e-6) {
                            continue;
                        }
                        dx /= dist;
                        dy /= dist;
                    } else {
                        dx /= dist;
                        dy /= dist;
                    }

                    // Push out
                    moveX += dx * overlap;
                    moveY += dy * overlap;
                    corrected = true;
                }
            }

            if (!corrected) break;
        }

        return new double[]{moveX, moveY};
    }

    private static Point2D closestPointOnSegment(Line2D seg, double px, double py) {
        double x1 = seg.getX1(), y1 = seg.getY1();
        double x2 = seg.getX2(), y2 = seg.getY2();
        double dx = x2 - x1, dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            return new Point2D.Double(x1, y1);
        }

        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        if (t < 0) t = 0;
        else if (t > 1) t = 1;
        return new Point2D.Double(x1 + t * dx, y1 + t * dy);
    }
    public static Vec2d getMaxMove1(Vec2d ballPos, double ballRadius, Vec2d ballVel, List<Line2D.Double> segments) {
        Vec2d initialPos = ballPos.copy();
        ballPos = ballPos.copy();
        ballVel = ballVel.copy();

        double totalVel = Math.hypot(ballVel.x, ballVel.y);
        int steps = (int)Math.ceil(totalVel / (ballRadius * 0.5));
        steps = Math.max(1, steps);

        Vec2d stepVel = new Vec2d(ballVel.x / steps, ballVel.y / steps);

        for (int s = 0; s < steps; s++) {
            ballPos.x += stepVel.x;
            ballPos.y += stepVel.y;

            for (int it = 0; it < 5; it++) {
                boolean collided = false;
                for (Line2D.Double seg : segments) {
                    CollisionInfo info = collideBallSegment(ballPos.x, ballPos.y, ballRadius, seg);
                    if (info.collided) {
                        collided = true;
                        ballPos.x += info.nx * info.penetration;
                        ballPos.y += info.ny * info.penetration;

                        double vn = ballVel.x * info.nx + ballVel.y * info.ny;
                        if (vn < 0) {
                            ballVel.x -= vn * info.nx;
                            ballVel.y -= vn * info.ny;
                            stepVel.x -= vn * info.nx / steps;
                            stepVel.y -= vn * info.ny / steps;
                        }
                    }
                }
                if (!collided) break;
            }
        }

        return new Vec2d(ballPos.x - initialPos.x, ballPos.y - initialPos.y);
    }
    public static Vec2d getMaxMove2(Vec2d ballPos,double ballRadius,Vec2d ballVel,List<Line2D.Double> segments) {
        Vec2d initialPos=ballPos;
        ballPos=ballPos.copy();
        ballVel=ballVel.copy();
        ballPos.x += ballVel.x;
        ballPos.y += ballVel.y;
        for (int it = 0; it < 5; it++) {
            boolean collided = false;
            for (Line2D.Double s : segments) {
                CollisionInfo info = collideBallSegment(ballPos.x, ballPos.y, ballRadius, s);
                if (info.collided) {
                    collided = true;
                    // push ball out of penetration
                    ballPos.x += info.nx * info.penetration;
                    ballPos.y += info.ny * info.penetration;

                    // remove velocity component along normal (no bounce)
                    double vn = ballVel.x * info.nx + ballVel.y * info.ny;
                    if (vn < 0) {
                        ballVel.x -= vn * info.nx;
                        ballVel.y -= vn * info.ny;
                    }

                    // apply friction along tangent
                    /*double tx = -info.ny; // tangent is normal rotated
                    double ty = info.nx;
                    double vt = ballVel.x * tx + ballVel.y * ty;
                    // apply surface friction only when in contact
                    double frictionDelta = Math.signum(vt) * Math.min(Math.abs(vt), surfaceFriction * dt);
                    vt -= frictionDelta;
                    // rebuild velocity from components
                    ballVel.x = vt * tx + (ballVel.x * info.nx + ballVel.y * info.ny) * info.nx; // normal component is already cleared
                    ballVel.y = vt * ty + (ballVel.x * info.nx + ballVel.y * info.ny) * info.ny;*/
                }
            }
            if (!collided) break;
        }
        return new Vec2d(ballPos.x-initialPos.x,ballPos.y-initialPos.y);
    }

    private static class CollisionInfo {
        boolean collided;
        double nx, ny; // normalized collision normal pointing from segment into ball
        double penetration;

        CollisionInfo() {
            collided = false;
        }
    }

    private static CollisionInfo collideBallSegment(double cx, double cy, double r, Line2D.Double s) {
        CollisionInfo info = new CollisionInfo();
        double x1 = s.x1, y1 = s.y1, x2 = s.x2, y2 = s.y2;
        double vx = x2 - x1, vy = y2 - y1;
        double wx = cx - x1, wy = cy - y1;
        double len2 = vx * vx + vy * vy;
        double t = 0;
        if (len2 > 1e-8) t = (wx * vx + wy * vy) / len2;
        t = Math.max(0, Math.min(1, t));
        double px = x1 + t * vx;
        double py = y1 + t * vy;
        double dx = cx - px;
        double dy = cy - py;
        double dist2 = dx * dx + dy * dy;
        double r2 = r * r;
        if (dist2 < r2 - 1e-8) {
            double dist = Math.sqrt(dist2);
            if (dist < 1e-8) {
                // center exactly on segment point; pick normal from segment direction
                double sx = -vy;
                double sy = vx;
                double sl = Math.hypot(sx, sy);
                if (sl < 1e-8) {
                    sx = 1; sy = 0; sl = 1;
                }
                info.nx = sx / sl;
                info.ny = sy / sl;
                info.penetration = r;
            } else {
                info.nx = dx / dist;
                info.ny = dy / dist;
                info.penetration = r - dist;
            }
            info.collided = true;
        }
        return info;
    }
}
