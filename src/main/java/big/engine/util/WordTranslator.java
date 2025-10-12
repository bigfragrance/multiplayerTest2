package big.engine.util;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class WordTranslator extends JFrame {
    private JTextField wordInput;
    private JTextArea historyArea;
    private JTextArea debugArea;
    private List<String> wordList;
    
    // Baidu Translation API information - replace with your own API keys
    private static final String APP_ID = "20250831002443163";
    private static final String SECURITY_KEY = "YVqfwybGwf0Y0tFQJW2_";
    private static final String TRANSLATE_API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    public WordTranslator() {
        wordList = new ArrayList<>();
        initUI();
    }

    private void initUI() {
        setTitle("Word Translator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        wordInput = new JTextField();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new SubmitListener());
        
        inputPanel.add(new JLabel("Enter English word:"), BorderLayout.WEST);
        inputPanel.add(wordInput, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        // History and debug panels in a split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // History area
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Translation History"));
        
        // Debug area for error information
        debugArea = new JTextArea();
        debugArea.setEditable(false);
        JScrollPane debugScroll = new JScrollPane(debugArea);
        debugScroll.setBorder(BorderFactory.createTitledBorder("Debug Information"));
        
        splitPane.setTopComponent(historyScroll);
        splitPane.setBottomComponent(debugScroll);
        splitPane.setDividerLocation(250);

        // Copy button
        JButton copyButton = new JButton("Copy Records");
        copyButton.addActionListener(new CopyListener());

        // Assemble UI
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(copyButton, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private class SubmitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String word = wordInput.getText().trim();
            if (word.isEmpty()) {
                JOptionPane.showMessageDialog(WordTranslator.this, "Please enter a word", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Clear previous debug info
            debugArea.setText("");
            logDebug("Translating: " + word);
            
            // Check API credentials
            if (APP_ID.equals("your_app_id") || SECURITY_KEY.equals("your_security_key")) {
                JOptionPane.showMessageDialog(WordTranslator.this, 
                    "Please replace APP_ID and SECURITY_KEY with your actual credentials", 
                    "Missing Credentials", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call translation API
            String translation = translateWord(word);
            if (translation != null) {
                String record = word + " - " + translation;
                wordList.add(record);
                updateHistoryArea();
                wordInput.setText("");
            } else {
                JOptionPane.showMessageDialog(WordTranslator.this, 
                    "Translation failed. Check debug information below.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class CopyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (wordList.isEmpty()) {
                JOptionPane.showMessageDialog(WordTranslator.this, "No records to copy", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (String record : wordList) {
                sb.append(record).append("\n");
            }

            StringSelection stringSelection = new StringSelection(sb.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            
            JOptionPane.showMessageDialog(WordTranslator.this, "Copied to clipboard", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateHistoryArea() {
        historyArea.setText("");
        for (String record : wordList) {
            historyArea.append(record + "\n");
        }
    }

    private void logDebug(String message) {
        debugArea.append(message + "\n");
        System.out.println(message); // Also print to console
    }

    private String translateWord(String word) {
        try {
            String salt = String.valueOf(System.currentTimeMillis());
            String sign = generateSign(word, salt);
            
            logDebug("Generated salt: " + salt);
            logDebug("Generated sign: " + sign);

            // Build request URL
            StringBuilder urlBuilder = new StringBuilder(TRANSLATE_API_URL);
            urlBuilder.append("?q=").append(URLEncoder.encode(word, StandardCharsets.UTF_8.name()));
            urlBuilder.append("&from=en");
            urlBuilder.append("&to=zh");
            urlBuilder.append("&appid=").append(APP_ID);
            urlBuilder.append("&salt=").append(salt);
            urlBuilder.append("&sign=").append(sign);
            
            String fullUrl = urlBuilder.toString();
            logDebug("Request URL: " + fullUrl);

            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set request headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);

            // Get response code
            int responseCode = connection.getResponseCode();
            logDebug("Response Code: " + responseCode);
            
            if (responseCode != 200) {
                // Read error stream
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
                String errorLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                logDebug("Error Response: " + errorResponse.toString());
                return null;
            }

            // Read successful response
            BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            String jsonResponse = response.toString();
            logDebug("Full Response: " + jsonResponse);
            try{
                JSONObject obj = new JSONObject(jsonResponse);
                String translation = obj.getJSONArray("trans_result").getJSONObject(0).getString("dst");
                logDebug("Translation: " + translation);
                return translation;
            }catch (Exception e){}

            // Check for API errors in response
            if (jsonResponse.contains("\"error_code\"")) {
                int errorCodeStart = jsonResponse.indexOf("\"error_code\":") + 13;
                int errorCodeEnd = jsonResponse.indexOf(",", errorCodeStart);
                String errorCode = jsonResponse.substring(errorCodeStart, errorCodeEnd).trim();
                
                int errorMsgStart = jsonResponse.indexOf("\"error_msg\":\"") + 13;
                int errorMsgEnd = jsonResponse.indexOf("\"", errorMsgStart);
                String errorMsg = jsonResponse.substring(errorMsgStart, errorMsgEnd);
                
                logDebug("API Error: " + errorCode + " - " + errorMsg);
                return null;
            }

            // 改进的JSON解析逻辑 - 专门处理百度翻译API的响应格式
            // 查找trans_result数组的开始
            int transResultStart = jsonResponse.indexOf("\"trans_result\":[") + 15;
            if (transResultStart < 15) {
                logDebug("Could not find trans_result array");
                return null;
            }
            
            // 在trans_result数组中查找dst字段
            int dstStart = jsonResponse.indexOf("\"dst\":\"", transResultStart) + 6;
            if (dstStart < 6) {
                logDebug("Could not find dst field in trans_result");
                return null;
            }
            
            // 找到dst值的结束位置
            int dstEnd = jsonResponse.indexOf("\"", dstStart);
            if (dstEnd <= dstStart) {
                logDebug("Could not determine end of dst value");
                return null;
            }
            
            // 提取并返回翻译结果（Java会自动处理Unicode转义字符）
            String translation = jsonResponse.substring(dstStart, dstEnd);
            logDebug("Extracted translation: " + translation);
            return translation;

        } catch (IOException ex) {
            logDebug("Network error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException ex) {
            logDebug("Encryption error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            logDebug("Unexpected error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    private String generateSign(String word, String salt) throws NoSuchAlgorithmException {
        String str = APP_ID + word + salt + SECURITY_KEY;
        logDebug("Sign string: " + str);
        
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        
        try (Formatter formatter = new Formatter()) {
            for (byte b : digest) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WordTranslator());
    }
}
