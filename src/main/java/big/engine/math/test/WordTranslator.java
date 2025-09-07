package big.engine.math.test;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// 单词信息数据模型
class WordInfo {
    String word;
    String phonetic;
    String translation;
    String exampleSentence;
    
    public WordInfo(String word, String phonetic, String translation, String exampleSentence) {
        this.word = word;
        this.phonetic = phonetic;
        this.translation = translation;
        this.exampleSentence = exampleSentence;
    }
    
    // 复制用格式
    public String toCopyString() {
        return word + " [" + phonetic + "] - " + translation;
    }
    
    // 显示用格式
    public String toDisplayString() {
        return word + " [" + phonetic + "]\n" + 
               "Translation: " + translation + "\n";
    }
}

public class WordTranslator extends JFrame {
    private JTextField wordInput;
    private JTextArea historyArea;
    private JTextArea debugArea;
    private List<WordInfo> wordList;
    
    // API配置
    private static final String APP_ID = "20250831002443163";
    private static final String SECURITY_KEY = "YVqfwybGwf0Y0tFQJW2_";
    private static final String TRANSLATE_API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";
    private static final String DICTIONARY_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public WordTranslator() {
        wordList = new ArrayList<>();
        initUI();
    }

    private void initUI() {
        setTitle("Word Translator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 输入面板
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        wordInput = new JTextField();
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(new SubmitListener());
        
        inputPanel.add(new JLabel("Enter English word:"), BorderLayout.WEST);
        inputPanel.add(wordInput, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        // 历史记录和调试面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // 历史记录区域
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Word History"));
        
        // 调试区域
        debugArea = new JTextArea();
        debugArea.setEditable(false);
        JScrollPane debugScroll = new JScrollPane(debugArea);
        debugScroll.setBorder(BorderFactory.createTitledBorder("Debug Information"));
        
        splitPane.setTopComponent(historyScroll);
        splitPane.setBottomComponent(debugScroll);
        splitPane.setDividerLocation(250);

        // 复制按钮
        JButton copyButton = new JButton("Copy Records");
        copyButton.addActionListener(new CopyListener());

        // 组装界面
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

            debugArea.setText("");
            logDebug("Processing: " + word);
            
            if (APP_ID.equals("your_app_id") || SECURITY_KEY.equals("your_security_key")) {
                JOptionPane.showMessageDialog(WordTranslator.this, 
                    "Please replace APP_ID and SECURITY_KEY with your actual credentials", 
                    "Missing Credentials", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 获取单词信息
            String phonetic = getPhonetic(word);
            String translation = translateWord(word);
            String example = generateExampleSentence(word);
            
            logDebug("Phonetic: " + phonetic);
            logDebug("Translation: " + translation);
            logDebug("Example: " + example);

            if (translation != null && phonetic != null) {
                WordInfo wordInfo = new WordInfo(word, phonetic, translation, example);
                wordList.add(wordInfo);
                updateHistoryArea();
                wordInput.setText("");
            } else {
                JOptionPane.showMessageDialog(WordTranslator.this, 
                    "Failed to process word. Check debug information.", 
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
            for (WordInfo info : wordList) {
                sb.append(info.toCopyString()).append("\n");
            }

            StringSelection stringSelection = new StringSelection(sb.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            
            JOptionPane.showMessageDialog(WordTranslator.this, "Copied to clipboard", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateHistoryArea() {
        historyArea.setText("");
        for (WordInfo info : wordList) {
            historyArea.append(info.toDisplayString() + "\n");
        }
    }

    private void logDebug(String message) {
        debugArea.append(message + "\n");
        System.out.println(message);
    }

    // 使用JSONObject获取音标
    private String getPhonetic(String word) {
        try {
            String urlString = DICTIONARY_API_URL + URLEncoder.encode(word, StandardCharsets.UTF_8.name());
            logDebug("Dictionary API URL: " + urlString);
            
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            logDebug("Dictionary API Response Code: " + responseCode);
            
            if (responseCode != 200) {
                return "No phonetic available";
            }

            // 读取响应并转换为字符串
            BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            String jsonResponse = response.toString();
            logDebug("Dictionary API Response: " + jsonResponse.substring(0, Math.min(300, jsonResponse.length())) + "...");

            // 使用JSONObject解析
            JSONArray jsonArray = new JSONArray(jsonResponse);
            JSONObject firstEntry = jsonArray.getJSONObject(0);
            
            // 尝试获取音标
            if (firstEntry.has("phonetic")) {
                return firstEntry.getString("phonetic");
            }
            
            // 尝试从发音数组中获取
            if (firstEntry.has("phonetics")) {
                JSONArray phonetics = firstEntry.getJSONArray("phonetics");
                for (int i = 0; i < phonetics.length(); i++) {
                    JSONObject phoneticObj = phonetics.getJSONObject(i);
                    if (phoneticObj.has("text")) {
                        return phoneticObj.getString("text");
                    }
                }
            }
            
            return "No phonetic available";

        } catch (JSONException je) {
            logDebug("JSON parsing error for phonetic: " + je.getMessage());
            return "Phonetic format error";
        } catch (Exception ex) {
            logDebug("Phonetic lookup error: " + ex.getMessage());
            return "No phonetic available";
        }
    }

    // 使用JSONObject翻译单词
    private String translateWord(String word) {
        try {
            String salt = String.valueOf(System.currentTimeMillis());
            String sign = generateSign(word, salt);
            
            logDebug("Generated salt: " + salt);
            logDebug("Generated sign: " + sign);

            // 构建请求URL
            StringBuilder urlBuilder = new StringBuilder(TRANSLATE_API_URL);
            urlBuilder.append("?q=").append(URLEncoder.encode(word, StandardCharsets.UTF_8.name()));
            urlBuilder.append("&from=en");
            urlBuilder.append("&to=zh");
            urlBuilder.append("&appid=").append(APP_ID);
            urlBuilder.append("&salt=").append(salt);
            urlBuilder.append("&sign=").append(sign);
            
            String fullUrl = urlBuilder.toString();
            logDebug("Translation API URL: " + fullUrl);

            URL url = new URL(fullUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            logDebug("Translation API Response Code: " + responseCode);
            
            if (responseCode != 200) {
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

            // 读取响应
            BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            String jsonResponse = response.toString();
            logDebug("Translation Response: " + jsonResponse);

            // 使用JSONObject解析
            JSONObject jsonObject = new JSONObject(jsonResponse);
            
            // 检查错误
            if (jsonObject.has("error_code")) {
                String errorCode = jsonObject.getString("error_code");
                String errorMsg = jsonObject.getString("error_msg");
                logDebug("API Error: " + errorCode + " - " + errorMsg);
                return null;
            }
            
            // 提取翻译结果
            JSONArray transResult = jsonObject.getJSONArray("trans_result");
            JSONObject resultObj = transResult.getJSONObject(0);
            return resultObj.getString("dst");

        } catch (JSONException je) {
            logDebug("Translation JSON parsing error: " + je.getMessage());
            return null;
        } catch (Exception ex) {
            logDebug("Translation error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    // 生成例句
    private String generateExampleSentence(String word) {
        String[] templates = {
            "I like to eat %s.",
            "She bought a %s yesterday.",
            "The %s is very important.",
            "He gave me a %s as a gift.",
            "Can you pass me the %s?",
            "I need to find a %s for this project.",
            "The %s looks beautiful in the garden.",
            "Do you know how to use a %s?"
        };
        
        // 动词处理
        if (word.endsWith("e") && !word.endsWith("ee") && !word.endsWith("oe")) {
            String verbForm = word + "d";
            String[] verbTemplates = {
                "She %s the letter yesterday.",
                "They %s their homework on time.",
                "He %s the task with great care."
            };
            return String.format(verbTemplates[(int)(Math.random() * verbTemplates.length)], verbForm);
        }
        
        // 动名词处理
        if (word.endsWith("ing")) {
            String[] ingTemplates = {
                "I am %s right now.",
                "She enjoys %s in her free time.",
                "%s is good for your health."
            };
            return String.format(ingTemplates[(int)(Math.random() * ingTemplates.length)], word);
        }
        
        // 随机选择模板
        return String.format(templates[(int)(Math.random() * templates.length)], word);
    }

    // 生成签名
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
