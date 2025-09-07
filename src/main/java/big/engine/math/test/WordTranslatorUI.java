package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class WordTranslatorUI extends JFrame {
    private JTextField inputField;
    private JButton submitButton, copyButton;
    private JTextArea recordArea;

    public WordTranslatorUI() {
        super("trans&record");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        inputField = new JTextField(20);
        submitButton = new JButton("submit");
        copyButton = new JButton("copy");
        recordArea = new JTextArea();
        recordArea.setEditable(false);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("word:"));
        topPanel.add(inputField);
        topPanel.add(submitButton);
        topPanel.add(copyButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(recordArea), BorderLayout.CENTER);

        submitButton.addActionListener(e -> translateAndRecord());
        copyButton.addActionListener(e -> copyRecordToClipboard());
    }

    private void translateAndRecord() {
        String word = inputField.getText().trim();
        if (word.isEmpty()) {
            JOptionPane.showMessageDialog(this, "input", "warn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String translated = translate(word);
        if (translated != null) {
            recordArea.append(word + " " + translated + "\n");
            inputField.setText("");
        }
    }

    private String translate(String text) {
        try {
            String apiUrl = "https://libretranslate.com/translate";
            String postData = "q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                    "&source=en&target=zh&format=text";

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                try (InputStream is = conn.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    // 简单解析 JSON: {"translatedText":"..."}
                    String resp = sb.toString();
                    int idx = resp.indexOf("\"translatedText\":\"");
                    if (idx >= 0) {
                        String rest = resp.substring(idx + 17);
                        int end = rest.indexOf("\"");
                        if (end > 0) {
                            return rest.substring(0, end);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "fail,HTTP code：" + code, "err", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "err：" + ex.getMessage(), "err", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void copyRecordToClipboard() {
        String text = recordArea.getText();
        StringSelection sel = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
        JOptionPane.showMessageDialog(this, "copied", "tip", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new WordTranslatorUI().setVisible(true);
        });
    }
}
