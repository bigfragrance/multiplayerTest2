package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class OcrSwingApp extends JFrame {
    private JLabel imageLabel;
    private BufferedImage currentImage;
    private List<OcrItem> ocrResults = new ArrayList<>();

    public OcrSwingApp() {
        super("OCR");

        JButton openBtn = new JButton("choose image");
        openBtn.addActionListener(this::onOpenImage);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);

        this.setLayout(new BorderLayout());
        this.add(openBtn, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        this.setSize(800, 600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
    }

    private void onOpenImage(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "files", "jpg", "jpeg", "png", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                currentImage = ImageIO.read(file);
                if (currentImage == null) {
                    JOptionPane.showMessageDialog(this, "err");
                    return;
                }

                // 调用 Python OCR
                ocrResults = runPythonOcr(file.getAbsolutePath());

                // 绘制结果
                BufferedImage annotated = drawResults(currentImage, ocrResults);
                imageLabel.setIcon(new ImageIcon(annotated));

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "failed: " + ex.getMessage());
            }
        }
    }

    private List<OcrItem> runPythonOcr(String imagePath) throws IOException, InterruptedException {
        List<OcrItem> items = new ArrayList<>();

        // 调用 python ocr_cli_tsv.py
        ProcessBuilder pb = new ProcessBuilder("python3", "ocr.py", imagePath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\t", -1);
                if (parts.length < 10) continue;

                double[] b = new double[8];
                for (int i = 0; i < 8; i++) {
                    b[i] = Double.parseDouble(parts[i]);
                }
                double prob = Double.parseDouble(parts[8]);
                String text = parts[9];

                items.add(new OcrItem(b, prob, text));
            }
            if(line!=null){
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python err exitCode=" + exitCode);
        }

        return items;
    }

    private BufferedImage drawResults(BufferedImage src, List<OcrItem> items) {
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2f));
        g.setFont(new Font("SansSerif", Font.BOLD, 16));

        for (OcrItem it : items) {
            int[] xs = {(int) it.bbox[0], (int) it.bbox[2], (int) it.bbox[4], (int) it.bbox[6]};
            int[] ys = {(int) it.bbox[1], (int) it.bbox[3], (int) it.bbox[5], (int) it.bbox[7]};
            g.setColor(Color.RED);
            g.drawPolygon(xs, ys, 4);

            g.setColor(new Color(255, 255, 0, 180));
            g.fillRect(xs[0], ys[0] - 20, it.text.length() * 14, 20);

            g.setColor(Color.BLACK);
            g.drawString(it.text, xs[0] + 2, ys[0] - 5);
        }

        g.dispose();
        return copy;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OcrSwingApp().setVisible(true));
    }
}

class OcrItem {
    final double[] bbox; // [x0,y0,x1,y1,x2,y2,x3,y3]
    final double prob;
    final String text;

    OcrItem(double[] bbox, double prob, String text) {
        this.bbox = bbox;
        this.prob = prob;
        this.text = text;
    }
}
