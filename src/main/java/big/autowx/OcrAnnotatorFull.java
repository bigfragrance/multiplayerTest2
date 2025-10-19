package big.autowx;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class OcrAnnotatorFull {

    public static void main(String[] args) throws Exception {
        String imagePath = "test.png";    // 输入 PNG
        String outputPath = "result.png"; // 输出 PNG
        long start=System.currentTimeMillis();
        // 1️⃣ 上传文件到 Python OCR 服务
        String response = uploadFile("http://127.0.0.1:5000/ocr", imagePath);
        System.out.println("ocr time: "+(System.currentTimeMillis()-start));
        // 2️⃣ 解析 JSON 返回结果
        JSONArray results = new JSONArray(response);

        // 3️⃣ 在图片上绘制结果
        BufferedImage img = ImageIO.read(new File(imagePath));
        Graphics2D g = img.createGraphics();
        g.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        g.setStroke(new BasicStroke(2));

        for (int i = 0; i < results.length(); i++) {
            JSONObject item = results.getJSONObject(i);
            String text = item.getString("text");
            double confidence = item.getDouble("confidence");
            JSONArray bbox = item.getJSONArray("bbox");

            // 计算矩形边界
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (int j = 0; j < 4; j++) {
                JSONArray p = bbox.getJSONArray(j);
                int x = (int) p.getDouble(0);
                int y = (int) p.getDouble(1);
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }

            // 绘制半透明绿色矩形框
            g.setColor(new Color(0, 255, 0, 120));
            g.drawRect(minX, minY, maxX - minX, maxY - minY);

            // 绘制红色文字
            g.setColor(new Color(255, 0, 0, 200));
            g.drawString(text + String.format(" (%.2f)", confidence), minX, minY - 5);
        }

        g.dispose();

        // 4️⃣ 保存结果 PNG
        ImageIO.write(img, "png", new File(outputPath));
        System.out.println("✅ 处理完成，结果已保存到: " + outputPath);
    }
    public static String ocr(String imagePath) throws Exception {
        return uploadFile("http://127.0.0.1:5000/ocr", imagePath);
    }
    public static String uploadFile(String urlString, String filePath) throws IOException {
        String boundary = "----OCRBoundary" + System.currentTimeMillis();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

            File file = new File(filePath);
            String fileName = file.getName();

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"\r\n");
            writer.append("Content-Type: image/png\r\n\r\n");
            writer.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            os.flush();
            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--\r\n").flush();
        }

        // 读取响应
        InputStream in = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        conn.disconnect();
        return sb.toString();
    }
}
