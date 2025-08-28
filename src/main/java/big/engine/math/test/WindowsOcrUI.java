package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

class OcrLine {
    String text;
    double x, y, w, h;
    OcrLine(String text, double x, double y, double w, double h) {
        this.text = text; this.x = x; this.y = y; this.w = w; this.h = h;
    }
}

public class WindowsOcrUI extends JFrame {
    private BufferedImage image;
    private java.util.List<OcrLine> ocrLines = new ArrayList<>();

    public WindowsOcrUI() {
        super("Windows 11 OCR Demo");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton btn = new JButton("Select Image and OCR");
        btn.addActionListener(e -> selectAndOcr());
        add(btn, BorderLayout.NORTH);
    }

    private void selectAndOcr() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                image = ImageIO.read(file);
                runOcr(file.getAbsolutePath());
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void runOcr(String path) throws Exception {
        ocrLines.clear();
        // PowerShell 7 路径
        String pwsh = "C:\\Program Files\\PowerShell\\7\\pwsh.exe";
        ProcessBuilder pb = new ProcessBuilder(
            pwsh,
            "-ExecutionPolicy", "Bypass",
            "-File", "test.ps1",
            path
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[PS OUTPUT] " + line);
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    try {
                        ocrLines.add(new OcrLine(
                            parts[0],
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]),
                            Double.parseDouble(parts[4])
                        ));
                    } catch (NumberFormatException ex) {
                        System.err.println("Parse error: " + line);
                    }
                }
            }
        }
        int exitCode = process.waitFor();
        System.out.println("[PS EXIT CODE] " + exitCode);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (image != null) {
            int imgX = 50, imgY = 100;
            g.drawImage(image, imgX, imgY, this);
            g.setColor(new Color(255, 0, 0, 120));
            for (OcrLine l : ocrLines) {
                int x = imgX + (int) l.x;
                int y = imgY + (int) l.y;
                int w = (int) l.w;
                int h = (int) l.h;
                g.drawRect(x, y, w, h);
                g.drawString(l.text, x, y - 2);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WindowsOcrUI().setVisible(true));
    }
}
