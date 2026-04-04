package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class ForgeUI extends JFrame {

    // ====== 输入控件 ======
    private final JTextArea promptArea = new JTextArea(3, 40);
    private final JTextArea negativeArea = new JTextArea(2, 40);

    private final JTextField seedField = new JTextField("-1", 6);
    private final JComboBox<String> samplerBox =
            new JComboBox<>(new String[]{"Euler a", "Euler", "DPM++ 2M", "DDIM"});
    private final JSpinner cfgSpinner =
            new JSpinner(new SpinnerNumberModel(5.0, 1.0, 30.0, 0.5));
    private final JSpinner stepsSpinner =
            new JSpinner(new SpinnerNumberModel(30, 1, 150, 1));

    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final ImagePanel imagePanel = new ImagePanel();

    private volatile boolean generating = false;

    public ForgeUI() {
        setTitle("SD Forge Java Client");
        setSize(1100, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        promptArea.setLineWrap(true);
        negativeArea.setLineWrap(true);
        negativeArea.setText("EasyNegative,(worst quality:2),(low quality:2),(normal quality:2),lowres,bad anatomy,bad hands,((monochrome)),((grayscale)),((watermark)),EasyNegativeV2,negative hands,negative_hand-neg,ng_deepnegative_v1_75t,badhandv4");

        add(buildTopPanel(), BorderLayout.NORTH);
        add(new JScrollPane(imagePanel), BorderLayout.CENTER);
    }

    private JPanel buildTopPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        p.add(labeled("Prompt", new JScrollPane(promptArea)));
        p.add(labeled("Negative", new JScrollPane(negativeArea)));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("Seed"));
        row.add(seedField);
        row.add(new JLabel("Sampler"));
        row.add(samplerBox);
        row.add(new JLabel("CFG"));
        row.add(cfgSpinner);
        row.add(new JLabel("Steps"));
        row.add(stepsSpinner);

        JButton gen = new JButton("Generate");
        gen.addActionListener(e -> startGenerate());
        row.add(gen);

        p.add(row);

        progressBar.setStringPainted(true);
        p.add(progressBar);

        return p;
    }

    private Component labeled(String name, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(name), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    // ================= 生成逻辑 =================

    private void startGenerate() {
        if (generating) return;
        generating = true;
        progressBar.setValue(0);

        new Thread(this::pollProgress).start();
        new Thread(this::generateImage).start();
    }

    private void generateImage() {
        try {
            String json = """
            {
              "prompt": "%s",
              "negative_prompt": "%s",
              "seed": %s,
              "sampler_name": "%s",
              "cfg_scale": %.2f,
              "steps": %d,
              "width": 1024,
              "height": 1024
            }
            """.formatted(
                    esc(promptArea.getText()),
                    esc(negativeArea.getText()),
                    seedField.getText(),
                    samplerBox.getSelectedItem(),
                    ((Number) cfgSpinner.getValue()).doubleValue(),
                    ((Number) stepsSpinner.getValue()).intValue()
            );

            HttpURLConnection conn = post(
                    "http://127.0.0.1:7860/sdapi/v1/txt2img",
                    json
            );

            String response = read(conn.getInputStream());

            String base64 = response
                    .split("\"images\":\\[\"")[1]
                    .split("\"")[0];

            byte[] bytes = Base64.getDecoder().decode(base64);
            BufferedImage img =
                    javax.imageio.ImageIO.read(new ByteArrayInputStream(bytes));

            saveImage(img, json);

            SwingUtilities.invokeLater(() -> imagePanel.setImage(img));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            generating = false;
        }
    }

    private void pollProgress() {
        while (generating) {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(
                                "http://127.0.0.1:7860/sdapi/v1/progress")
                                .openConnection();

                String json = read(conn.getInputStream());
                float p = Float.parseFloat(
                        json.split("\"progress\":")[1].split(",")[0]);

                SwingUtilities.invokeLater(() ->
                        progressBar.setValue((int) (p * 100)));

                Thread.sleep(300);
            } catch (Exception ignored) {}
        }
        SwingUtilities.invokeLater(() -> progressBar.setValue(100));
    }

    // ================= 图片保存 =================

    private void saveImage(BufferedImage img, String params) throws IOException {
        File dir = new File("outputs");
        dir.mkdirs();

        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(dir, name + ".png");

        javax.imageio.ImageIO.write(img, "png", file);

        // 写 PNG info（SD WebUI 可读）
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(raf.length());
            raf.writeBytes("\nParameters:\n" + params);
        }

        System.out.println("Saved: " + file.getAbsolutePath());
    }

    // ================= 工具 =================

    private static String esc(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private static HttpURLConnection post(String url, String json)
            throws Exception {
        HttpURLConnection c =
                (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setRequestProperty("Content-Type", "application/json");
        c.setDoOutput(true);
        c.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        return c;
    }

    private static String read(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // ================= 图片面板 =================

    static class ImagePanel extends JPanel {

        private BufferedImage image;
        private double scale = 1.0;
        private double ox = 0, oy = 0;
        private Point last;

        ImagePanel() {
            addMouseWheelListener(this::zoom);
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    last = e.getPoint();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    Point p = e.getPoint();
                    ox += p.x - last.x;
                    oy += p.y - last.y;
                    last = p;
                    repaint();
                }
            });
        }

        void setImage(BufferedImage img) {
            image = img;
            scale = 1.0;
            ox = oy = 0;
            repaint();
        }

        void zoom(MouseWheelEvent e) {
            if (image == null) return;
            double old = scale;
            scale *= e.getWheelRotation() < 0 ? 1.1 : 0.9;
            scale = Math.max(0.1, Math.min(scale, 10));

            double mx = e.getX(), my = e.getY();
            ox = mx - (mx - ox) * (scale / old);
            oy = my - (my - oy) * (scale / old);
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) return;
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform at = new AffineTransform();
            at.translate(ox, oy);
            at.scale(scale, scale);
            g2.drawImage(image, at, null);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ForgeUI().setVisible(true));
    }
}
