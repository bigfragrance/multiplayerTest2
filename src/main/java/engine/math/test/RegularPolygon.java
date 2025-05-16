package engine.math.test;

import javax.swing.*;
import java.awt.*; 

public class RegularPolygon extends JFrame {
    private final int n;
    private final int size;

    public RegularPolygon(int n) {
        this.n = n;
        this.size  = 150;
        setTitle("" + n + "");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new PolygonPanel());
        setVisible(true);
    }

    class PolygonPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                              RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
                        int[] xPoints = new int[n];
            int[] yPoints = new int[n];
            
            double angleStep = 2 * Math.PI / n;
            double initialAngle = -Math.PI/2;
            
            for (int i = 0; i < n; i++) {
                double angle = initialAngle + i * angleStep;
                xPoints[i] = (int) (centerX + size * Math.cos(angle)); 
                yPoints[i] = (int) (centerY + size * Math.sin(angle)); 
            }
            

            GradientPaint gp = new GradientPaint(
                centerX - size, centerY - size, Color.BLUE,
                centerX + size, centerY + size, Color.CYAN);
            g2d.setPaint(gp); 
                        g2d.fillPolygon(xPoints,  yPoints, n);
            

            g2d.setColor(Color.BLACK); 
            g2d.drawPolygon(xPoints,  yPoints, n);
        }
    }

    public static void main(String[] args) {
        int sides = 3; // 修改这个数字可以生成不同边数的多边形
        new RegularPolygon(sides);
    }
}