package big.engine.math.util;

import big.engine.math.Vec2d;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BulletRenderer {
    private static final Map<Integer,BufferedImage[]> speedBullets=new ConcurrentHashMap<>();
    private static int maxSpeed;
    public static void initBullets(int baseRadius, int maxSpeed,Color color) {
        BulletRenderer.maxSpeed = maxSpeed;
        BufferedImage[] speedBullets = new BufferedImage[maxSpeed + 1];
        for (int s = 0; s <= maxSpeed; s++) {
            int blurLength = Math.max(3, (int)(s * 0.6) + 3);

            int size = baseRadius * 2 + blurLength;
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(ColorUtils.darker(color, 0.6));
            g.fillOval(blurLength / 2+baseRadius/2, blurLength / 2+baseRadius/2, baseRadius, baseRadius);
            g.setColor(color);
            g.fillOval(blurLength / 2+baseRadius/2+3, blurLength / 2+baseRadius/2+3, baseRadius-6, baseRadius-6);
            g.dispose();

            float[] kernelData = new float[blurLength];
            for (int i = 0; i < blurLength; i++) kernelData[i] = 1.0f / blurLength;
            java.awt.image.Kernel kernel = new java.awt.image.Kernel(blurLength, 1, kernelData);
            java.awt.image.ConvolveOp op = new java.awt.image.ConvolveOp(kernel, java.awt.image.ConvolveOp.EDGE_NO_OP, null);
            img = op.filter(img, null);

            speedBullets[s] = img;
        }
        BulletRenderer.speedBullets.put(color.getRGB(),speedBullets);
    }

    public static void drawBullet(Graphics2D g, Vec2d pos, Vec2d vel, double radius, Color color) {
        if(!speedBullets.containsKey(color.getRGB()))
            initBullets(50,50,color);
        radius*=2;

        double speed = Math.sqrt(vel.x * vel.x + vel.y * vel.y);
        int s = (int)Math.round(Math.min(maxSpeed, speed));

        BufferedImage base = speedBullets.get(color.getRGB())[s];

        /*BufferedImage coloredBullet = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        RescaleOp rop = new RescaleOp(
                new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha()/255f},
                new float[]{0, 0, 0, 0},
                null
        );
        rop.filter(base, coloredBullet);*/

        double baseWidth = base.getWidth();
        double baseHeight = base.getHeight();

        AffineTransform at = new AffineTransform();
        at.translate(pos.x, pos.y);
        at.rotate(Math.atan2(vel.y, vel.x));
        at.scale(radius*(1+speed*0.01) / (baseWidth / 2.0), radius / (baseHeight / 2.0));
        at.translate(-baseWidth / 2.0, -baseHeight / 2.0);

        g.drawImage(base, at, null);
    }
}
