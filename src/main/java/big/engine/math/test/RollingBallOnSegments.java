package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class RollingBallOnSegments extends JPanel implements ActionListener {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private final Timer timer;

    // physics
    private final double gravity = 1200; // px/s^2
    private final double damping = 0.999; // global velocity damping
    private final double surfaceFriction = 800.0; // px/s^2 (friction acceleration)
    private final double restitution = 0.6; // bounce factor
    private final double timeStep = 1.0 / 60.0; // fixed timestep

    // ball
    private Point2D.Double ballPos = new Point2D.Double(200, 100);
    private Point2D.Double ballVel = new Point2D.Double(200, 0);
    private final double ballRadius = 14;
    private boolean paused = false;

    // segments
    private final List<Line2D.Double> segments = new ArrayList<>();

    // mouse / drawing
    private Point2D.Double drawStart = null;
    private boolean draggingBall = false;
    private Point lastMouse = null;
    private Point dragOffset = null;

    public RollingBallOnSegments() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(32, 36, 43));

        // sample geometry
        segments.add(new Line2D.Double(50, 600, 950, 600));
        segments.add(new Line2D.Double(300, 450, 600, 520));
        segments.add(new Line2D.Double(700, 350, 900, 450));
        segments.add(new Line2D.Double(120, 520, 250, 420));

        timer = new Timer((int) Math.round(timeStep * 1000), this);
        timer.start();

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (p.distance(ballPos.x, ballPos.y) <= ballRadius + 2) {
                    draggingBall = true;
                    dragOffset = new Point((int) (p.x - ballPos.x), (int) (p.y - ballPos.y));
                    ballVel.setLocation(0, 0);
                } else {
                    drawStart = new Point2D.Double(p.x, p.y);
                }
                lastMouse = p;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                if (draggingBall) {
                    // move ball and set velocity based on mouse movement
                    double newX = p.x - dragOffset.x;
                    double newY = p.y - dragOffset.y;
                    if (lastMouse != null) {
                        // velocity in px/s
                        ballVel.setLocation((p.x - lastMouse.x) / timeStep, (p.y - lastMouse.y) / timeStep);
                    } else {
                        ballVel.setLocation(0, 0);
                    }
                    ballPos.setLocation(newX, newY);
                }
                lastMouse = p;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggingBall) {
                    draggingBall = false;
                } else if (drawStart != null) {
                    Point p = e.getPoint();
                    Line2D.Double seg = new Line2D.Double(drawStart.x, drawStart.y, p.x, p.y);
                    if (seg.getP1().distance(seg.getP2()) > 6) {
                        segments.add(seg);
                    }
                    drawStart = null;
                }
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    Point p = e.getPoint();
                    double bestDist = 30;
                    int bestIndex = -1;
                    for (int i = 0; i < segments.size(); i++) {
                        Line2D.Double s = segments.get(i);
                        double d = ptSegDist(s.x1, s.y1, s.x2, s.y2, p.x, p.y);
                        if (d < bestDist) {
                            bestDist = d;
                            bestIndex = i;
                        }
                    }
                    if (bestIndex >= 0) segments.remove(bestIndex);
                    repaint();
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    paused = !paused;
                } else if (e.getKeyCode() == KeyEvent.VK_C) {
                    segments.clear();
                } else if (e.getKeyCode() == KeyEvent.VK_R) {
                    resetBall();
                }
                repaint();
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }

    private void resetBall() {
        ballPos.setLocation(200, 100);
        ballVel.setLocation(200, 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(200, 200, 200));
        for (Line2D.Double s : segments) {
            g2.draw(s);
        }

        if (drawStart != null) {
            Point p = getMousePosition();
            if (p != null) {
                g2.setColor(new Color(120, 200, 255));
                g2.draw(new Line2D.Double(drawStart.x, drawStart.y, p.x, p.y));
            }
        }

        g2.setColor(new Color(255, 175, 0));
        g2.fillOval((int) Math.round(ballPos.x - ballRadius), (int) Math.round(ballPos.y - ballRadius),
                (int) Math.round(ballRadius * 2), (int) Math.round(ballRadius * 2));

        g2.setColor(Color.WHITE);
        g2.drawString("Drag ball or draw segments. Right-click deletes. Space=Pause, C=Clear, R=Reset", 10, 20);

        g2.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !draggingBall) {
            stepPhysics(timeStep);
        }
        repaint();
    }

    private void stepPhysics(double dt) {
        // integrate gravity and damping
        ballVel.y += gravity * dt;
        ballVel.x *= Math.pow(damping, dt * 60);
        ballVel.y *= Math.pow(damping, dt * 60);

        // compute movement with continuous collision detection
        Point2D.Double move = getMaxMove(new Point2D.Double(ballPos.x, ballPos.y), new Point2D.Double(ballVel.x, ballVel.y), dt);
        ballPos.x += move.x;
        ballPos.y += move.y;
    }

    // continuous collision detection and response for moving circle vs static segments
    // returns the actual movement vector (dx,dy) applied during dt and updates ballVel accordingly
    private Point2D.Double getMaxMove(Point2D.Double startPos, Point2D.Double startVel, double dt) {
        // first, resolve any initial penetration (static separation)
        for (int iter = 0; iter < 4; iter++) {
            boolean any = false;
            for (Line2D.Double s : segments) {
                // closest point on segment
                double[] cp = closestPointOnSegment(s.x1, s.y1, s.x2, s.y2, startPos.x, startPos.y);
                double dx = startPos.x - cp[0];
                double dy = startPos.y - cp[1];
                double dist2 = dx * dx + dy * dy;
                double r2 = ballRadius * ballRadius;
                if (dist2 < r2 - 1e-8) {
                    double dist = Math.sqrt(dist2);
                    double nx, ny, penetration;
                    if (dist < 1e-8) {
                        // pick normal perpendicular to segment
                        double sx = -(s.y2 - s.y1);
                        double sy = (s.x2 - s.x1);
                        double sl = Math.hypot(sx, sy);
                        if (sl < 1e-8) { sx = 1; sy = 0; sl = 1; }
                        nx = sx / sl; ny = sy / sl;
                        penetration = ballRadius;
                    } else {
                        nx = dx / dist; ny = dy / dist;
                        penetration = ballRadius - dist;
                    }
                    startPos.x += nx * penetration;
                    startPos.y += ny * penetration;
                    // remove normal component of velocity
                    double vn = startVel.x * nx + startVel.y * ny;
                    if (vn < 0) {
                        startVel.x -= vn * nx;
                        startVel.y -= vn * ny;
                    }
                    any = true;
                }
            }
            if (!any) break;
        }

        double sx = startPos.x, sy = startPos.y;
        double vx = startVel.x, vy = startVel.y;
        double timeLeft = dt;
        double movedX = 0, movedY = 0;
        int maxIters = 6;

        for (int iter = 0; iter < maxIters && timeLeft > 1e-7; iter++) {
            CollisionCandidate best = null;
            // search earliest collision within timeLeft
            for (Line2D.Double s : segments) {
                CollisionCandidate c = earliestCollisionWithSegment(sx, sy, vx, vy, ballRadius, timeLeft, s);
                if (c != null) {
                    if (best == null || c.t < best.t) best = c;
                }
            }

            if (best == null) {
                // no collision, move all remaining
                movedX += vx * timeLeft;
                movedY += vy * timeLeft;
                sx += vx * timeLeft;
                sy += vy * timeLeft;
                timeLeft = 0;
                break;
            } else {
                // move to impact point
                movedX += vx * best.t;
                movedY += vy * best.t;
                sx += vx * best.t;
                sy += vy * best.t;

                // compute normal at contact (already provided by candidate)
                double nx = best.nx;
                double ny = best.ny;

                // tiny push out to avoid re-penetration
                double tiny = 1e-6;
                sx += nx * tiny;
                sy += ny * tiny;

                // reflect normal component with restitution
                double vn = vx * nx + vy * ny;
                if (vn < 0) {
                    double vnAfter = -vn * restitution;
                    double deltaVn = vnAfter - vn; // positive
                    vx += deltaVn * nx;
                    vy += deltaVn * ny;
                }

                // apply tangential friction over the impact time
                double tx = -ny;
                double ty = nx;
                double vt = vx * tx + vy * ty;
                double frictionDelta = Math.signum(vt) * Math.min(Math.abs(vt), surfaceFriction * best.t);
                vt -= frictionDelta;
                // rebuild velocity from components
                double vnComp = vx * nx + vy * ny;
                vx = vnComp * nx + vt * tx;
                vy = vnComp * ny + vt * ty;

                timeLeft -= best.t;
            }
        }

        // if some time remains after iterations, move remaining time
        if (timeLeft > 1e-7) {
            movedX += vx * timeLeft;
            movedY += vy * timeLeft;
            sx += vx * timeLeft;
            sy += vy * timeLeft;
            timeLeft = 0;
        }

        // update global velocity
        ballVel.x = vx;
        ballVel.y = vy;

        return new Point2D.Double(movedX, movedY);
    }

    private static class CollisionCandidate {
        double t; // seconds from start of interval
        double nx, ny; // normal at contact pointing from segment into circle
    }

    // returns earliest collision candidate within [0, timeLimit], or null
    private CollisionCandidate earliestCollisionWithSegment(double sx, double sy, double vx, double vy, double r, double timeLimit, Line2D.Double s) {
        double ax = s.x1, ay = s.y1, bx = s.x2, by = s.y2;
        double abx = bx - ax, aby = by - ay;
        double abLen2 = abx * abx + aby * aby;

        // candidate times
        double bestT = Double.POSITIVE_INFINITY;
        double bestNx = 0, bestNy = 0;

        // 1) line (infinite) collisions where projection falls within segment
        if (abLen2 > 1e-8) {
            double abLen = Math.sqrt(abLen2);
            double nx = -aby / abLen;
            double ny = abx / abLen;
            // orient normal toward start point
            double toStart = (sx - ax) * nx + (sy - ay) * ny;
            if (toStart < 0) { nx = -nx; ny = -ny; toStart = -toStart; }

            double d0 = toStart; // signed distance from start to line along normal
            double vn = vx * nx + vy * ny;

            // solve for d0 + vn * t = r or -r
            if (Math.abs(vn) > 1e-8) {
                double t1 = (r - d0) / vn;
                double t2 = (-r - d0) / vn;
                double[] cand = {t1, t2};
                for (double t : cand) {
                    if (t >= 0 && t <= timeLimit) {
                        double px = sx + vx * t;
                        double py = sy + vy * t;
                        double u = ((px - ax) * abx + (py - ay) * aby) / abLen2;
                        if (u >= 0 && u <= 1) {
                            if (t < bestT) {
                                // compute precise normal from closest point
                                double cx = ax + abx * u;
                                double cy = ay + aby * u;
                                double dx = px - cx, dy = py - cy;
                                double dist = Math.hypot(dx, dy);
                                if (dist > 1e-8) {
                                    bestT = t;
                                    bestNx = dx / dist;
                                    bestNy = dy / dist;
                                } else {
                                    // degenerate, use line normal
                                    bestT = t;
                                    bestNx = nx;
                                    bestNy = ny;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2) endpoint collisions (A and B) - solve quadratic for |(S + V t) - P|^2 = r^2
        double a = vx * vx + vy * vy;
        if (a > 1e-12) {
            // endpoint A
            double cx = sx - ax, cy = sy - ay;
            double b = 2 * (vx * cx + vy * cy);
            double c = cx * cx + cy * cy - r * r;
            double disc = b * b - 4 * a * c;
            if (disc >= 0) {
                double sd = Math.sqrt(disc);
                double tA1 = (-b - sd) / (2 * a);
                double tA2 = (-b + sd) / (2 * a);
                double[] ts = {tA1, tA2};
                for (double t : ts) {
                    if (t >= 0 && t <= timeLimit) {
                        if (t < bestT) {
                            double px = sx + vx * t;
                            double py = sy + vy * t;
                            double dx = px - ax, dy = py - ay;
                            double dist = Math.hypot(dx, dy);
                            if (dist > 1e-8) {
                                bestT = t;
                                bestNx = dx / dist;
                                bestNy = dy / dist;
                            }
                        }
                    }
                }
            }

            // endpoint B
            cx = sx - bx; cy = sy - by;
            b = 2 * (vx * cx + vy * cy);
            c = cx * cx + cy * cy - r * r;
            disc = b * b - 4 * a * c;
            if (disc >= 0) {
                double sd = Math.sqrt(disc);
                double tB1 = (-b - sd) / (2 * a);
                double tB2 = (-b + sd) / (2 * a);
                double[] ts = {tB1, tB2};
                for (double t : ts) {
                    if (t >= 0 && t <= timeLimit) {
                        if (t < bestT) {
                            double px = sx + vx * t;
                            double py = sy + vy * t;
                            double dx = px - bx, dy = py - by;
                            double dist = Math.hypot(dx, dy);
                            if (dist > 1e-8) {
                                bestT = t;
                                bestNx = dx / dist;
                                bestNy = dy / dist;
                            }
                        }
                    }
                }
            }
        }

        if (bestT == Double.POSITIVE_INFINITY) return null;
        CollisionCandidate c = new CollisionCandidate();
        c.t = Math.max(0, bestT);
        c.nx = bestNx;
        c.ny = bestNy;
        return c;
    }

    // closest point on segment AB to point P, returns [cx,cy]
    private static double[] closestPointOnSegment(double ax, double ay, double bx, double by, double px, double py) {
        double vx = bx - ax, vy = by - ay;
        double wx = px - ax, wy = py - ay;
        double c1 = vx * wx + vy * wy;
        if (c1 <= 0) return new double[]{ax, ay};
        double c2 = vx * vx + vy * vy;
        if (c2 <= c1) return new double[]{bx, by};
        double b = c1 / c2;
        return new double[]{ax + b * vx, ay + b * vy};
    }

    private static double ptSegDist(double x1, double y1, double x2, double y2, double px, double py) {
        double vx = x2 - x1, vy = y2 - y1;
        double wx = px - x1, wy = py - y1;
        double c1 = vx * wx + vy * wy;
        if (c1 <= 0) return Math.hypot(px - x1, py - y1);
        double c2 = vx * vx + vy * vy;
        if (c2 <= c1) return Math.hypot(px - x2, py - y2);
        double b = c1 / c2;
        double bx = x1 + b * vx;
        double by = y1 + b * vy;
        return Math.hypot(px - bx, py - by);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rolling Ball on Segments");
            RollingBallOnSegments panel = new RollingBallOnSegments();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

