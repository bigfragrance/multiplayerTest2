package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class GoogleTranslatorUI extends JFrame {
    private JTextField inputField;
    private JButton submitButton, copyButton;
    private JTextArea recordArea;

    public GoogleTranslatorUI() {
        super("Word Translator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        // Force proxy settings (edit host/port for your proxy)
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "7890");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "7890");

        inputField = new JTextField(20);
        submitButton = new JButton("Submit");
        copyButton = new JButton("Copy Records");
        recordArea = new JTextArea();
        recordArea.setEditable(false);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Word:"));
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
            JOptionPane.showMessageDialog(this, "Please enter a word", "Warning", JOptionPane.WARNING_MESSAGE);
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
            String urlStr =
                    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=zh-CN&dt=t&q="
                            + URLEncoder.encode(text, StandardCharsets.UTF_8);

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);

                    String resp = sb.toString();
                    int start = resp.indexOf("\"");
                    if (start >= 0) {
                        int end = resp.indexOf("\"", start + 1);
                        if (end > start) {
                            return resp.substring(start + 1, end);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "HTTP error: " + conn.getResponseCode(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void copyRecords() {
        String text = recordArea.getText();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(text), null);
        JOptionPane.showMessageDialog(this, "Records copied to clipboard",
                "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GoogleTranslatorUI().setVisible(true));
    }
}
