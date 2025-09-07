package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class IcibaTranslatorUI extends JFrame {
    private JTextField inputField;
    private JButton submitButton, copyButton;
    private JTextArea recordArea;

    public IcibaTranslatorUI() {
        super("Word Translator (ICIBA)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        inputField = new JTextField(20);
        submitButton = new JButton("提交");
        copyButton = new JButton("复制记录");
        recordArea = new JTextArea();
        recordArea.setEditable(false);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("英文单词:"));
        topPanel.add(inputField);
        topPanel.add(submitButton);
        topPanel.add(copyButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(recordArea), BorderLayout.CENTER);

        submitButton.addActionListener(e -> translateAndRecord());
        copyButton.addActionListener(e -> copyRecords());
    }

    private void translateAndRecord() {
        String word = inputField.getText().trim();
        if (word.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入单词", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String translated = translate(word);
        if (translated != null && !translated.isEmpty()) {
            recordArea.append(word + "\n" + translated + "\n\n");
            inputField.setText("");
        }
    }

    private String translate(String text) {
        try {
            String urlStr = "http://dict-co.iciba.com/api/dictionary.php?w="
                    + URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&type=json";

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);

                    String resp = sb.toString();

                    // 提取音标
                    String phonetic = "";
                    int symStart = resp.indexOf("\"ph_en\":\"");
                    if (symStart >= 0) {
                        int symEnd = resp.indexOf("\"", symStart + 9);
                        if (symEnd > symStart) phonetic = resp.substring(symStart + 9, symEnd);
                    }

                    // 提取 meanings，每个 meaning 单独换行
                    StringBuilder meanings = new StringBuilder();
                    int idx = 0;
                    while ((idx = resp.indexOf("\"means\":[", idx)) >= 0) {
                        int start = idx + 9;
                        int end = resp.indexOf("]", start);
                        if (end > start) {
                            String arr = resp.substring(start, end);
                            arr = arr.replaceAll("[\\[\\]\"]", "");
                            String[] parts = arr.split(",");
                            for (String part : parts) {
                                meanings.append(part.trim()).append("\n");
                            }
                        }
                        idx = end;
                    }

                    if (!phonetic.isEmpty()) {
                        return "[" + phonetic + "]\n" + meanings.toString().trim();
                    } else {
                        return meanings.toString().trim();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "翻译失败，HTTP code：" + conn.getResponseCode(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "翻译出错：" + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void copyRecords() {
        String text = recordArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可复制的内容",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            JOptionPane.showMessageDialog(this, "已复制记录到剪贴板",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "复制失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IcibaTranslatorUI().setVisible(true));
    }
}
