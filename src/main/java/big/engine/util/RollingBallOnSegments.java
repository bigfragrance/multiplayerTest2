package big.engine.util;

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
    private final double surfaceFriction = 6.0; // friction when sliding along surfaces
    private final double timeStep = 1.0 / 60.0; // fixed timestep
    private final int iterations = 5; // collision resolution iterations per frame

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
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                if (draggingBall) {
                    ballPos.setLocation(p.x - dragOffset.x, p.y - dragOffset.y);
                    ballVel.setLocation(0, 0);
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();
                if (draggingBall) {
                    draggingBall = false;
                } else if (drawStart != null) {
                    Line2D.Double seg = new Line2D.Double(drawStart.x, drawStart.y, p.x, p.y);
                    // ignore very short segments
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
                    // right click remove nearest segment within threshold
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

        // draw segments
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(200, 200, 200));
        for (Line2D.Double s : segments) {
            g2.draw(s);
        }

        // draw temporary segment
        if (drawStart != null) {
            Point p = getMousePosition();
            if (p != null) {
                g2.setColor(new Color(120, 200, 255));
                g2.draw(new Line2D.Double(drawStart.x, drawStart.y, p.x, p.y));
            }
        }

        // draw ball
        g2.setColor(new Color(255, 175, 0));
        g2.fillOval((int) Math.round(ballPos.x - ballRadius), (int) Math.round(ballPos.y - ballRadius),
                (int) Math.round(ballRadius * 2), (int) Math.round(ballRadius * 2));

        // draw HUD
        g2.setColor(Color.WHITE);
        g2.drawString("Left-click-drag ball to move. Left-click+drag elsewhere to draw segment. Right-click to delete nearest segment.", 10, 20);
        g2.drawString("Space: pause/play  C: clear segments  R: reset ball", 10, 36);

        g2.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !draggingBall) {
            // integrate physics with fixed timestep
            stepPhysics(timeStep);
        }
        repaint();
    }

    private void stepPhysics(double dt) {
        // apply gravity
        ballVel.y += gravity * dt;

        // global damping
        ballVel.x *= Math.pow(damping, dt * 60);
        ballVel.y *= Math.pow(damping, dt * 60);

        // integrate
        ballPos.x += ballVel.x * dt;
        ballPos.y += ballVel.y * dt;

        // collision resolution (iterative)
        for (int it = 0; it < iterations; it++) {
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
                    double tx = -info.ny; // tangent is normal rotated
                    double ty = info.nx;
                    double vt = ballVel.x * tx + ballVel.y * ty;
                    // apply surface friction only when in contact
                    double frictionDelta = Math.signum(vt) * Math.min(Math.abs(vt), surfaceFriction * dt);
                    vt -= frictionDelta;
                    // rebuild velocity from components
                    ballVel.x = vt * tx + (ballVel.x * info.nx + ballVel.y * info.ny) * info.nx; // normal component is already cleared
                    ballVel.y = vt * ty + (ballVel.x * info.nx + ballVel.y * info.ny) * info.ny;
                }
            }
            if (!collided) break;
        }

        // simple ground/edge containment
        if (ballPos.x < ballRadius) {
            ballPos.x = ballRadius;
            if (ballVel.x < 0) ballVel.x = 0;
        }
        if (ballPos.x > WIDTH - ballRadius) {
            ballPos.x = WIDTH - ballRadius;
            if (ballVel.x > 0) ballVel.x = 0;
        }
        if (ballPos.y < ballRadius) {
            ballPos.y = ballRadius;
            if (ballVel.y < 0) ballVel.y = 0;
        }
        if (ballPos.y > HEIGHT - ballRadius) {
            ballPos.y = HEIGHT - ballRadius;
            if (ballVel.y > 0) ballVel.y = 0;
        }
    }

    private static class CollisionInfo {
        boolean collided;
        double nx, ny; // normalized collision normal pointing from segment into ball
        double penetration;

        CollisionInfo() {
            collided = false;
        }
    }

    private CollisionInfo collideBallSegment(double cx, double cy, double r, Line2D.Double s) {
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

    // distance point to segment
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
