package big.engine.math.test;

import big.engine.math.Vec2d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CurvePredictorUI extends JFrame {

    private List<Vec2d> history = new ArrayList<>();
    private int predictSteps = 20; // 预测的未来点数

    public CurvePredictorUI() {
        setTitle("曲线预判演示");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DrawingPanel panel = new DrawingPanel();
        JButton clearButton = new JButton("clear");
        clearButton.addActionListener(e -> {
            history.clear();
            panel.repaint();
        });

        JPanel topPanel = new JPanel();
        topPanel.add(clearButton);

        add(topPanel, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
    }

    private class DrawingPanel extends JPanel {
        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    history.add(new Vec2d(e.getX(), e.getY()));
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // 画历史点
            g2.setColor(Color.BLUE);
            for (Vec2d p : history) {
                g2.fillOval((int) p.x - 4, (int) p.y - 4, 8, 8);
            }

            // 画历史连线
            g2.setColor(Color.GRAY);
            for (int i = 1; i < history.size(); i++) {
                Vec2d p1 = history.get(i - 1);
                Vec2d p2 = history.get(i);
                g2.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }

            // 画预测轨迹
            if (history.size() >= 3) {
                g2.setColor(Color.RED);
                Vec2d prev = history.get(history.size() - 1);
                for (double i = 0.2; i <= predictSteps; i+=0.2) {
                    Vec2d next = CurvePredictor.predictCatmullRom(history, i);
                    g2.drawLine((int) prev.x, (int) prev.y, (int) next.x, (int) next.y);
                    prev = next;
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CurvePredictorUI ui = new CurvePredictorUI();
            ui.setVisible(true);
        });
    }
}

// --------------------- Vec2d 类 ---------------------

// --------------------- 曲线预判类 ---------------------
class CurvePredictor {
    public static Vec2d predictCatmullRom(List<Vec2d> history,double t) {
        int n = history.size();
        if (n < 4) {
            // 点太少，退化为线性预测
            Vec2d last = history.get(n - 1);
            Vec2d prev = history.get(n - 2);
            Vec2d vel = new Vec2d(last.x - prev.x, last.y - prev.y);
            return new Vec2d(last.x + vel.x * t, last.y + vel.y * t);
        }

        // 使用最后 4 个点进行 Catmull-Rom 样条
        Vec2d p0 = history.get(n - 4);
        Vec2d p1 = history.get(n - 3);
        Vec2d p2 = history.get(n - 2);
        Vec2d p3 = history.get(n - 1);

        // t参数归一化，假设每步相当于0.1的步长
        double step = 0.1;
        double u = step * t;
        if (u > 1.0) u = 1.0; // 限制在样条区间内

        double u2 = u * u;
        double u3 = u2 * u;

        double x = 0.5 * ((2 * p1.x) +
                (-p0.x + p2.x) * u +
                (2*p0.x - 5*p1.x + 4*p2.x - p3.x) * u2 +
                (-p0.x + 3*p1.x - 3*p2.x + p3.x) * u3);

        double y = 0.5 * ((2 * p1.y) +
                (-p0.y + p2.y) * u +
                (2*p0.y - 5*p1.y + 4*p2.y - p3.y) * u2 +
                (-p0.y + 3*p1.y - 3*p2.y + p3.y) * u3);

        return new Vec2d(x, y);
    }
    public static Vec2d predict(List<Vec2d> history,double t, int degree) {
        int n = history.size();
        if (n < degree + 1) {
            // 点太少时退化为线性预测
            Vec2d last = history.get(n - 1);
            Vec2d prev = history.get(n - 2);
            Vec2d vel = new Vec2d(last.x - prev.x, last.y - prev.y);
            return new Vec2d(last.x + vel.x * t, last.y + vel.y * t);
        }

        // 构造时间序列：0,1,...,n-1
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = history.get(i).x;
            ys[i] = history.get(i).y;
        }

        double[] coefX = polyFit(n, degree, xs);
        double[] coefY = polyFit(n, degree, ys);

        double futureK = n - 1 + t;
        double predX = polyEval(coefX, futureK);
        double predY = polyEval(coefY, futureK);

        return new Vec2d(predX, predY);
    }

    /** 多项式拟合（最小二乘法），返回系数数组 a0..ad */
    private static double[] polyFit(int n, int degree, double[] values) {
        // 正规方程法：X^T X a = X^T y
        double[][] A = new double[degree + 1][degree + 1];
        double[] B = new double[degree + 1];

        for (int row = 0; row <= degree; row++) {
            for (int col = 0; col <= degree; col++) {
                double sum = 0;
                for (int i = 0; i < n; i++) {
                    sum += Math.pow(i, row + col);
                }
                A[row][col] = sum;
            }
            double sumB = 0;
            for (int i = 0; i < n; i++) {
                sumB += values[i] * Math.pow(i, row);
            }
            B[row] = sumB;
        }

        return gaussianSolve(A, B);
    }

    /** 多项式求值 */
    private static double polyEval(double[] coef, double x) {
        double result = 0;
        for (int i = 0; i < coef.length; i++) {
            result += coef[i] * Math.pow(x, i);
        }
        return result;
    }

    /** 高斯消元解方程组 */
    private static double[] gaussianSolve(double[][] A, double[] B) {
        int n = B.length;
        for (int i = 0; i < n; i++) {
            // 主元选择
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(A[k][i]) > Math.abs(A[maxRow][i])) {
                    maxRow = k;
                }
            }
            double[] tmp = A[i]; A[i] = A[maxRow]; A[maxRow] = tmp;
            double t = B[i]; B[i] = B[maxRow]; B[maxRow] = t;

            // 消元
            for (int k = i + 1; k < n; k++) {
                double factor = A[k][i] / A[i][i];
                B[k] -= factor * B[i];
                for (int j = i; j < n; j++) {
                    A[k][j] -= factor * A[i][j];
                }
            }
        }

        // 回代
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = B[i];
            for (int j = i + 1; j < n; j++) {
                sum -= A[i][j] * x[j];
            }
            x[i] = sum / A[i][i];
        }
        return x;
    }
}
