package big.game.entity.player;

import big.engine.util.ColorUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GhostBloomRenderer {

    public int x, y;     // 中心点
    public int r;        // 半径

    float t = 0f;        // 动画进度
    float fade = 1f;     // 玩家淡出
    boolean gone = false;

    public static BufferedImage noiseMap=null;
    public GhostBloomRenderer(int x, int y, int r) {
        this.x = x;
        this.y = y;
        this.r = r;
        if(noiseMap==null) noiseMap = generateNoise();
    }

    // ------------------------ 蓝噪声 ------------------------
    private BufferedImage generateNoise() {
        Random ran = new Random();
        int size = 256;
        BufferedImage noise = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int v = 150 + ran.nextInt(80);
                int rgb = (v << 16) | (v << 8) | v;
                noise.setRGB(x, y, rgb);
            }
        }
        return noise;
    }

    // ------------------------ 超分辨率残影 ------------------------
    private BufferedImage renderGhost(int baseRadius, float t) {

        int scale = 2;
        int r = baseRadius * scale;
        int size = r * 4;
        int center = size / 2;

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        for (int iy = 0; iy < size; iy++) {
            for (int ix = 0; ix < size; ix++) {

                int dx = ix - center;
                int dy = iy - center;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist > r) continue;
                dist = (float) (Math.pow(dist / r, 1.5) * r);

                int nx = (ix * noiseMap.getWidth()) / size;
                int ny = (iy * noiseMap.getHeight()) / size;
                float noise = (noiseMap.getRGB(nx, ny) & 0xFF) / 255f;

                float cutoff = t * 1.5f;
                if (noise < cutoff) continue;

                float jitterX = (float) ((Math.random() - 0.5) * 0.7);
                float jitterY = (float) ((Math.random() - 0.5) * 0.7);

                float push = cutoff * 30 * scale;
                int fx = (int) (ix + (dx / dist) * push + jitterX);
                int fy = (int) (iy + (dy / dist) * push + jitterY);

                if (fx < 0 || fy < 0 || fx >= size || fy >= size) continue;

                int alpha = (int) ((1 - t) * 255);
                int color = (alpha << 24) | (255 << 16) | (230 << 8) | 120;
                img.setRGB(fx, fy, color);
            }
        }

        int finalSize = size / scale;
        BufferedImage small = new BufferedImage(finalSize, finalSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = small.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(img, 0, 0, finalSize, finalSize, null);
        g2.dispose();

        return small;
    }

    // ------------------------ Bloom ------------------------
    private BufferedImage bloom(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();

        BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        int[] k = {1, 3, 6, 3, 1};
        int ksum = 14;

        // 横向
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = 0, r = 0, g = 0, b = 0;

                for (int i = -2; i <= 2; i++) {
                    int px = Math.max(0, Math.min(w - 1, x + i));
                    int rgb = src.getRGB(px, y);
                    int kv = k[i + 2];

                    a += ((rgb >> 24) & 255) * kv;
                    r += ((rgb >> 16) & 255) * kv;
                    g += ((rgb >> 8) & 255) * kv;
                    b += ((rgb) & 255) * kv;
                }

                a /= ksum; r /= ksum; g /= ksum; b /= ksum;
                tmp.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        // 纵向
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = 0, r = 0, g = 0, b = 0;

                for (int i = -2; i <= 2; i++) {
                    int py = Math.max(0, Math.min(h - 1, y + i));
                    int rgb = tmp.getRGB(x, py);
                    int kv = k[i + 2];

                    a += ((rgb >> 24) & 255) * kv;
                    r += ((rgb >> 16) & 255) * kv;
                    g += ((rgb >> 8) & 255) * kv;
                    b += ((rgb) & 255) * kv;
                }

                a /= ksum; r /= ksum; g /= ksum; b /= ksum;
                out.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    // ------------------------ 主渲染 ------------------------
    public void render(Graphics2D g,float t,Color c) {
        if (t < 0.25f) {
            fade = 1 - t * 4;
        } else {
            fade = 0;
            gone = true;
        }
        BufferedImage ghost = renderGhost(r, t);
        BufferedImage glow  = bloom(ghost);

        int gx = x - ghost.getWidth() / 2;
        int gy = y - ghost.getHeight() / 2;

        g.drawImage(glow, gx, gy, null);
        g.drawImage(ghost, gx, gy, null);

        if (!gone) {
            int a = (int) (fade * 255);
            g.setColor(ColorUtils.setAlpha(c,a));
            g.fillOval(x - r, y - r, r * 2, r * 2);
        }
    }

    // ------------------------ 动画更新 ------------------------
    public void update() {
        t += 0.1f;
        if (t < 0.25f) {
            fade = 1 - t * 4;
        } else {
            fade = 0;
            gone = true;
        }
    }

    // 重新开始 dash
    public void trigger() {
        t = 0;
        fade = 1;
        gone = false;
    }
}
