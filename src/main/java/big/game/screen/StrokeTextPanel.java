package big.game.screen;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

public class StrokeTextPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        String text = "描边文字";
        Font font = new Font("Microsoft YaHei", Font.BOLD, 64);
        g2d.setFont(font);

        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, text);
        Shape textShape = gv.getOutline(50, 100);

        // 1️⃣ 描边
        g2d.setStroke(new BasicStroke(6f));
        g2d.setColor(Color.BLACK);
        g2d.draw(textShape);

        // 2️⃣ 填充
        g2d.setColor(Color.WHITE);
        g2d.fill(textShape);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("描边文字");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.add(new StrokeTextPanel());
        frame.setVisible(true);
    }
}
