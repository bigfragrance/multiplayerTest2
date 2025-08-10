package big.engine.math.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

public class ImageNoiseProcessor {
    private static final long RANDOM_SEED = 123456789L;
    private static final Random random = new Random(RANDOM_SEED);

    public static void main(String[] args) {
        try {
            // 参数设置
            String inputPath = "noisy.jpg";//"E:\\sd-webui-aki\\sd-webui-aki-v4.11.1-cu128\\outputs\\txt2img-images\\2025-07-16\\00114-986655363.png";
            String noisyOutputPath = "noisy1.jpg";
            String denoisedOutputPath = "denoised.jpg";
            double noiseIntensity = 0.1;
            boolean addNoise = false;

            // 读取原始图片
            BufferedImage image = ImageIO.read(new File(inputPath));

            if (addNoise) {
                // 添加椒盐噪声
                BufferedImage noisyImage = addSaltPepperNoise(image, noiseIntensity);
                ImageIO.write(noisyImage, "jpg", new File(noisyOutputPath));
                System.out.println("已添加噪声并保存至: " + noisyOutputPath);
            } else {
                BufferedImage denoisedImage = medianFilter(image, 2);
                ImageIO.write(denoisedImage, "jpg", new File(denoisedOutputPath));
                System.out.println("已去除噪声并保存至: " + denoisedOutputPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 添加椒盐噪声方法
    private static BufferedImage addSaltPepperNoise(BufferedImage image, double noiseIntensity) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage noisyImage = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                
                // 按概率添加噪声
                if (random.nextDouble() < noiseIntensity) {
                    // 随机选择盐噪声(白色)或胡椒噪声(黑色)
                    rgb = random.nextBoolean() ? 0xFFFFFFFF : 0xFF000000;
                }
                noisyImage.setRGB(x, y, rgb);
            }
        }
        return noisyImage;
    }

    // 中值滤波去噪方法
    private static BufferedImage medianFilter(BufferedImage image, int windowSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, image.getType());
        int margin = windowSize / 2;

        for (int y = margin; y < height - margin; y++) {
            for (int x = margin; x < width - margin; x++) {
                int[] window = new int[windowSize * windowSize];
                int index = 0;
                
                // 收集窗口内像素
                for (int dy = -margin; dy <= margin; dy++) {
                    for (int dx = -margin; dx <= margin; dx++) {
                        window[index++] = image.getRGB(x + dx, y + dy);
                    }
                }
                
                // 排序并取中值
                java.util.Arrays.sort(window);
                int median = window[window.length / 2];
                result.setRGB(x, y, median);
            }
        }
        return result;
    }
}