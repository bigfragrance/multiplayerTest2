package big.game.entity.player;

import javax.swing.*;
import java.awt.*;

public class DashEffectDemo extends JPanel implements Runnable {

    // 使用封装后的渲染类（x=300, y=300, 半径=50）
    private final GhostBloomRenderer renderer =
            new GhostBloomRenderer(300, 300, 50);

    private boolean isDashing = false;

    public DashEffectDemo() {
        setPreferredSize(new Dimension(600, 600));
        new Thread(this).start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());


    }

    @Override
    public void run() {
        while (true) {
            if (isDashing) {
                renderer.update();
            }

            repaint();
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {}
        }
    }

    /** 触发一次 dash 动画 */
    public void triggerDash() {
        isDashing = true;
        renderer.trigger();
    }

    // ------------------------
    // ★ Main 直接合并到这里
    // ------------------------
    public static void main(String[] args) {

        JFrame frame = new JFrame("Dash Effect Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DashEffectDemo panel = new DashEffectDemo();
        frame.add(panel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // 每 1 秒触发一次 dash
        new Timer(1000, e -> panel.triggerDash()).start();
    }
}
