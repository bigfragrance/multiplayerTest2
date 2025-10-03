package big.engine.math.util;

import big.engine.math.Vec2d;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public class BulletRenderer {

    // 预生成的不同速度下的模糊子弹
    private static BufferedImage[] speedBullets;
    private static int maxSpeed;

    /**
     * 初始化子弹模糊图像
     *
     * @param baseRadius 基础半径
     * @param maxSpeed   最大速度（预生成 0 ~ maxSpeed）
     */
    public static void initBullets(int baseRadius, int maxSpeed) {
        BulletRenderer.maxSpeed = maxSpeed;
        speedBullets = new BufferedImage[maxSpeed + 1];

        for (int s = 0; s <= maxSpeed; s++) {
            // 模糊长度与速度相关（你可以调整比例，比如 s/2）
            int blurLength = Math.max(3, (int)(s * 0.6) + 3);

            int size = baseRadius * 2 + blurLength;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(ColorUtils.darker(Color.WHITE, 0.6));
            g.fillOval(blurLength / 2+baseRadius/2, blurLength / 2+baseRadius/2, baseRadius, baseRadius);
            g.setColor(Color.WHITE);
            g.fillOval(blurLength / 2+baseRadius/2+3, blurLength / 2+baseRadius/2+3, baseRadius-6, baseRadius-6);
            g.dispose();

            // 水平模糊卷积
            float[] kernelData = new float[blurLength];
            for (int i = 0; i < blurLength; i++) kernelData[i] = 1.0f / blurLength;
            java.awt.image.Kernel kernel = new java.awt.image.Kernel(blurLength, 1, kernelData);
            java.awt.image.ConvolveOp op = new java.awt.image.ConvolveOp(kernel, java.awt.image.ConvolveOp.EDGE_NO_OP, null);
            img = op.filter(img, null);

            speedBullets[s] = img;
        }
    }

    /**
     * 绘制子弹
     *
     * @param g      Graphics2D
     * @param pos    中心位置
     * @param vel    速度向量
     * @param radius 半径
     * @param color  颜色
     */
    public static void drawBullet(Graphics2D g, Vec2d pos, Vec2d vel, double radius, Color color) {
        if (speedBullets == null)
            initBullets(50,30);
        radius*=2;

        double speed = Math.sqrt(vel.x * vel.x + vel.y * vel.y);
        int s = (int)Math.round(Math.min(maxSpeed, speed)); // 取最近的速度

        BufferedImage base = speedBullets[s];

        // 上色
        BufferedImage coloredBullet = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        RescaleOp rop = new RescaleOp(
                new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f},
                new float[]{0, 0, 0, 0},
                null
        );
        rop.filter(base, coloredBullet);

        // 构造仿射变换
        double baseWidth = base.getWidth();
        double baseHeight = base.getHeight();

        AffineTransform at = new AffineTransform();
        at.translate(pos.x, pos.y);
        at.rotate(Math.atan2(vel.y, vel.x));
        at.scale(radius / (baseWidth / 2.0), radius / (baseHeight / 2.0)); // 按半径缩放
        at.translate(-baseWidth / 2.0, -baseHeight / 2.0);

        g.drawImage(coloredBullet, at, null);
    }
}
