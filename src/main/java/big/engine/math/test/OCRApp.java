package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class OCRApp {
    private JFrame frame;
    private JLabel imageLabel;
    private File selectedImage;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                OCRApp window = new OCRApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public OCRApp() {
        initialize();
    }

    private void initialize() {
        // 创建窗口
        frame = new JFrame();
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建面板
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(new BorderLayout());

        // 创建选择图片按钮
        JButton btnChooseImage = new JButton("choose image");
        btnChooseImage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseImage();
            }
        });
        panel.add(btnChooseImage, BorderLayout.NORTH);

        // 创建图像显示区域
        imageLabel = new JLabel("");
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);

        // 创建识别按钮
        JButton btnRecognize = new JButton("get");
        btnRecognize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedImage != null) {
                    recognizeText(selectedImage.getAbsolutePath());
                }
            }
        });
        panel.add(btnRecognize, BorderLayout.SOUTH);
    }

    // 选择图片
    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("choose");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("img file", "jpg", "png", "jpeg", "gif"));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImage = fileChooser.getSelectedFile();
            displayImage(selectedImage);
        }
    }

    // 显示选中的图片
    private void displayImage(File imageFile) {
        try {
            ImageIcon imageIcon = new ImageIcon(imageFile.getAbsolutePath());
            imageLabel.setIcon(imageIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 调用 Python 脚本进行文字识别
    private void recognizeText(String imagePath) {
        try {
            // 使用 ProcessBuilder 调用 Python 脚本
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "ocr.py", imagePath);
            System.out.println(processBuilder.command());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();

            // 解析识别结果（JSON 格式）
            System.out.println(output.toString());
            JSONArray resultArray = new JSONArray(output.toString());
            drawTextOnImage(resultArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将识别的文本结果绘制在图片上
    private void drawTextOnImage(JSONArray resultArray) {
        try {
            ImageIcon imageIcon = new ImageIcon(selectedImage.getAbsolutePath());
            Image image = imageIcon.getImage();
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);

            // 设置绘制的字体和颜色
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.setColor(Color.RED);

            // 绘制每个识别出的文本
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject textObj = resultArray.getJSONObject(i);
                String text = textObj.getString("text");
                JSONArray bbox = textObj.getJSONArray("bbox");

                // 获取字符位置
                int x = bbox.getInt(0);
                int y = bbox.getInt(1);
                g2d.drawString(text, x, y);
            }

            // 更新图像显示
            imageLabel.setIcon(new ImageIcon(bufferedImage));
            g2d.dispose();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
