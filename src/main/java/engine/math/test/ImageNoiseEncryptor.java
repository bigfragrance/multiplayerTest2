package engine.math.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

public class ImageNoiseEncryptor {

    public static BufferedImage addNoise(BufferedImage image, long seed, double strength) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage noisyImage = new BufferedImage(width, height, image.getType());
        Random rand = new Random(seed);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                

                double noiseR = (rand.nextDouble() * 2 - 1) * strength;
                double noiseG = (rand.nextDouble() * 2 - 1) * strength;
                double noiseB = (rand.nextDouble() * 2 - 1) * strength;
                

                r = clamp(r + (int) noiseR/*+(strength>0?-50:50)*/);
                g = clamp(g + (int) noiseG/*+(strength>0?-50:50)*/);
                b = clamp(b + (int) noiseB/*+(strength>0?-50:50)*/);
                

                noisyImage.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return noisyImage;
    }


    public static BufferedImage removeNoise(BufferedImage noisyImage, long seed, double strength) {

        return addNoise(noisyImage, seed, -strength);
    }


    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static void main(String[] args) throws Exception {

        long seed = 12345L;
        double strength = 100;
        String inputPath = "E:\\sd-webui-aki\\sd-webui-aki-v4.11.1-cu128\\outputs\\txt2img-images\\2025-07-16\\00114-986655363.png";
        String encryptedPath = "encrypted.png";
        String decryptedPath = "decrypted.png";


        BufferedImage original = ImageIO.read(new File(inputPath));
        

        BufferedImage encrypted = addNoise(original, seed, strength);
        ImageIO.write(encrypted, "png", new File(encryptedPath));
        

        BufferedImage decrypted = removeNoise(encrypted, seed, strength);
        ImageIO.write(decrypted, "png", new File(decryptedPath));
        
        System.out.println("done!: " + seed);
    }
}