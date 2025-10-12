package big.engine.util;

import big.engine.math.Box;
import big.engine.math.Vec2d;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class CollisionUtil {
    private static final double S = 0.001;
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
    // ---------- 辅助：点比较 ----------
    private static boolean pointsClose(double x1, double y1, double x2, double y2, double eps) {
        double dx = x1 - x2, dy = y1 - y2;
        return dx*dx + dy*dy <= eps*eps;
    }

    // ---------- 合并首尾共线线段（返回新列表） ----------
    private static List<Line2D.Double> mergeColinearSegments(List<Line2D.Double> segments) {
        final double EPS = 1e-6;
        List<Line2D.Double> segs = new ArrayList<>(segments);
        boolean changed = true;
        while (changed) {
            changed = false;
            outer:
            for (int i = 0; i < segs.size(); i++) {
                Line2D.Double a = segs.get(i);
                for (int j = i + 1; j < segs.size(); j++) {
                    Line2D.Double b = segs.get(j);
                    // 检查是否有共享端点
                    double ax1 = a.x1, ay1 = a.y1, ax2 = a.x2, ay2 = a.y2;
                    double bx1 = b.x1, by1 = b.y1, bx2 = b.x2, by2 = b.y2;
                    double sx = Double.NaN, sy = Double.NaN;
                    double aOtherX = 0, aOtherY = 0, bOtherX = 0, bOtherY = 0;
                    if (pointsClose(ax2, ay2, bx1, by1, EPS)) { sx = ax2; sy = ay2; aOtherX = ax1; aOtherY = ay1; bOtherX = bx2; bOtherY = by2; }
                    else if (pointsClose(ax1, ay1, bx2, by2, EPS)) { sx = ax1; sy = ay1; aOtherX = ax2; aOtherY = ay2; bOtherX = bx1; bOtherY = by1; }
                    else if (pointsClose(ax2, ay2, bx2, by2, EPS)) { sx = ax2; sy = ay2; aOtherX = ax1; aOtherY = ay1; bOtherX = bx1; bOtherY = by1; }
                    else if (pointsClose(ax1, ay1, bx1, by1, EPS)) { sx = ax1; sy = ay1; aOtherX = ax2; aOtherY = ay2; bOtherX = bx2; bOtherY = by2; }
                    else continue;

                    // 方向向量
                    double vax = sx - aOtherX, vay = sy - aOtherY;
                    double vbx = bOtherX - sx, vby = bOtherY - sy;
                    double la = Math.hypot(vax, vay), lb = Math.hypot(vbx, vby);
                    if (la < EPS || lb < EPS) continue;
                    double dax = vax / la, day = vay / la;
                    double dbx = vbx / lb, dby = vby / lb;
                    double cross = Math.abs(dax * dby - day * dbx);
                    double dot = dax * dbx + day * dby;
                    // 共线且方向一致或相反（允许小角度误差）
                    if (cross < 1e-3 && Math.abs(dot) > 0.999) {
                        Line2D.Double merged = new Line2D.Double(aOtherX, aOtherY, bOtherX, bOtherY);
                        segs.remove(j);
                        segs.remove(i);
                        segs.add(merged);
                        changed = true;
                        break outer;
                    }
                }
            }
        }
        return segs;
    }

    // ---------- 碰撞信息 ----------
    private static class CollisionInfo {
        boolean collided;
        double nx, ny;            // 若可得，为接触点指向球心的单位法线
        double penetration;       // r - dist
        boolean isEndpoint;       // 最近点是否端点
        double contactX, contactY;// 最近点坐标（端点或段内点）
        double tangentX, tangentY;// 段方向单位向量（总是保存）
        CollisionInfo() {
            collided = false; nx = ny = penetration = 0;
            isEndpoint = false; contactX = contactY = 0;
            tangentX = tangentY = 0;
        }
    }

    // ---------- 圆与线段最近点碰撞检测 ----------
    private static CollisionInfo collideBallSegment(double cx, double cy, double r, Line2D.Double s) {
        final double EPS = 1e-8;
        CollisionInfo info = new CollisionInfo();
        double x1 = s.x1, y1 = s.y1, x2 = s.x2, y2 = s.y2;
        double vx = x2 - x1, vy = y2 - y1;
        double wx = cx - x1, wy = cy - y1;
        double len2 = vx * vx + vy * vy;

        double tRaw = 0.0;
        if (len2 > EPS) tRaw = (wx * vx + wy * vy) / len2;
        double t = Math.max(0.0, Math.min(1.0, tRaw));

        double px = x1 + t * vx;
        double py = y1 + t * vy;
        info.contactX = px; info.contactY = py;

        double dx = cx - px, dy = cy - py;
        double dist2 = dx * dx + dy * dy;
        double r2 = r * r;

        if (dist2 < r2 - 1e-8) {
            info.collided = true;
            double dist = Math.sqrt(dist2);
            boolean atEndpoint = (t <= 1e-9) || (t >= 1.0 - 1e-9);

            if (dist > 1e-8) {
                info.nx = dx / dist;
                info.ny = dy / dist;
                info.penetration = r - dist;
                info.isEndpoint = atEndpoint;
            } else {
                // 球心几乎在接触点：法线不明确，标记为端点特殊处理
                info.nx = 0.0;
                info.ny = 0.0;
                info.penetration = r;
                info.isEndpoint = true;
            }

            double segLen = Math.hypot(vx, vy);
            if (segLen > EPS) {
                info.tangentX = vx / segLen;
                info.tangentY = vy / segLen;
            } else {
                info.tangentX = 0.0; info.tangentY = 0.0;
            }
        }
        return info;
    }

    // ---------- 主函数：计算最大移动（替换用） ----------
    public static Vec2d getMaxMove1(Vec2d ballPos, double ballRadius, Vec2d ballVel, List<Line2D.Double> segments) {
        final double EPS = 1e-8;
        final double GROUP_EPS = Math.max(1e-6, ballRadius * 1e-3);

        // 先合并共线相连的段，避免重复碰撞
        List<Line2D.Double> segs = mergeColinearSegments(segments);

        Vec2d initialPos = ballPos.copy();
        ballPos = ballPos.copy();
        ballVel = ballVel.copy();

        double totalVel = Math.hypot(ballVel.x, ballVel.y);
        int steps = (int) Math.ceil(totalVel / (ballRadius * 0.5));
        steps = Math.max(1, steps);

        Vec2d stepVel = new Vec2d(ballVel.x / steps, ballVel.y / steps);

        for (int s = 0; s < steps; s++) {
            ballPos.x += stepVel.x;
            ballPos.y += stepVel.y;

            for (int it = 0; it < 8; it++) {
                List<CollisionInfo> collisions = new ArrayList<>();
                for (Line2D.Double seg : segs) {
                    CollisionInfo info = collideBallSegment(ballPos.x, ballPos.y, ballRadius, seg);
                    if (info.collided) collisions.add(info);
                }
                if (collisions.isEmpty()) break;

                // 按接触点分组
                List<List<CollisionInfo>> groups = new ArrayList<>();
                for (CollisionInfo c : collisions) {
                    boolean placed = false;
                    for (List<CollisionInfo> g : groups) {
                        CollisionInfo rep = g.get(0);
                        double dx = rep.contactX - c.contactX;
                        double dy = rep.contactY - c.contactY;
                        if (Math.hypot(dx, dy) < GROUP_EPS) { g.add(c); placed = true; break; }
                    }
                    if (!placed) { List<CollisionInfo> gg = new ArrayList<>(); gg.add(c); groups.add(gg); }
                }

                double totalCorrX = 0.0, totalCorrY = 0.0;

                // 处理每个组
                for (List<CollisionInfo> g : groups) {
                    double gx = g.get(0).contactX, gy = g.get(0).contactY;
                    double sumNx = 0.0, sumNy = 0.0;
                    boolean hasN = false;
                    double sumTx = 0.0, sumTy = 0.0;
                    double maxPen = 0.0;

                    for (CollisionInfo c : g) {
                        maxPen = Math.max(maxPen, c.penetration);
                        if (Math.abs(c.nx) > EPS || Math.abs(c.ny) > EPS) {
                            sumNx += c.nx * c.penetration;
                            sumNy += c.ny * c.penetration;
                            hasN = true;
                        } else if (Math.abs(c.tangentX) > EPS || Math.abs(c.tangentY) > EPS) {
                            sumTx += c.tangentX * c.penetration;
                            sumTy += c.tangentY * c.penetration;
                        }
                    }

                    double groupNX = 0.0, groupNY = 0.0;
                    if (hasN && Math.hypot(sumNx, sumNy) > EPS) {
                        double gl = Math.hypot(sumNx, sumNy);
                        groupNX = sumNx / gl; groupNY = sumNy / gl;
                    } else if (Math.hypot(sumTx, sumTy) > EPS) {
                        // 端点只有切线信息：把切线平均后取垂直方向为法线
                        double tl = Math.hypot(sumTx, sumTy);
                        double avgTx = sumTx / tl, avgTy = sumTy / tl;
                        groupNX = -avgTy; groupNY = avgTx;
                        double gl = Math.hypot(groupNX, groupNY);
                        if (gl > EPS) { groupNX /= gl; groupNY /= gl; }
                        // 确保法线朝向球心
                        double dirx = ballPos.x - gx, diry = ballPos.y - gy;
                        if (dirx * groupNX + diry * groupNY < 0) { groupNX = -groupNX; groupNY = -groupNY; }
                    } else {
                        // 兜底：使用退回方向
                        groupNX = -stepVel.x; groupNY = -stepVel.y;
                        double fl = Math.hypot(groupNX, groupNY);
                        if (fl < EPS) { groupNX = -ballVel.x; groupNY = -ballVel.y; fl = Math.hypot(groupNX, groupNY); }
                        if (fl < EPS) { groupNX = 1.0; groupNY = 0.0; fl = 1.0; }
                        groupNX /= fl; groupNY /= fl;
                    }

                    // 计算该组沿 groupN 需要的最小位移：对组内每个碰撞用投影求取 a_i = penetration / dot(...)
                    double reqA = 0.0;
                    for (CollisionInfo c : g) {
                        double dot = 0.0;
                        if (Math.abs(c.nx) > EPS || Math.abs(c.ny) > EPS) {
                            dot = c.nx * groupNX + c.ny * groupNY;
                        } else {
                            // 端点没有法线：用球心到接触点方向作为近似
                            double ux = ballPos.x - c.contactX, uy = ballPos.y - c.contactY;
                            double ud = Math.hypot(ux, uy);
                            if (ud < EPS) {
                                ux = -stepVel.x; uy = -stepVel.y; ud = Math.hypot(ux, uy);
                                if (ud < EPS) { ux = 1.0; uy = 0.0; ud = 1.0; }
                            }
                            ux /= ud; uy /= ud;
                            dot = ux * groupNX + uy * groupNY;
                        }
                        if (dot < 1e-6) dot = 1e-6; // 防止除零或接近平行导致巨大值
                        double ai = c.penetration / dot;
                        if (ai > reqA) reqA = ai;
                    }

                    totalCorrX += groupNX * reqA;
                    totalCorrY += groupNY * reqA;
                } // end groups

                // 一次性应用合成修正
                ballPos.x += totalCorrX;
                ballPos.y += totalCorrY;

                // 用合成修正方向移除速度法向分量（一次性）
                double nlen = Math.hypot(totalCorrX, totalCorrY);
                if (nlen < EPS) break;
                double nvx = totalCorrX / nlen, nvy = totalCorrY / nlen;
                double vn = ballVel.x * nvx + ballVel.y * nvy;
                if (vn < 0) {
                    ballVel.x -= vn * nvx;
                    ballVel.y -= vn * nvy;
                    stepVel.x -= vn * nvx / steps;
                    stepVel.y -= vn * nvy / steps;
                }
                // 继续下一次迭代以处理残余穿透
            } // end iter
        } // end steps

        return new Vec2d(ballPos.x - initialPos.x, ballPos.y - initialPos.y);
    }



}
