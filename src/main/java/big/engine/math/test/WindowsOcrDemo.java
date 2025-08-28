package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class WindowsOcrDemo extends JFrame {

    private JTextArea resultArea;
    private JButton selectButton;

    public WindowsOcrDemo() {
        super("Windows 11 OCR Demo");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        resultArea = new JTextArea();
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(resultArea);

        selectButton = new JButton("选择图片识别文字");
        selectButton.addActionListener(this::chooseFile);

        add(scrollPane, BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);
    }

    private void chooseFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String text = runOcr(file.getAbsolutePath());
            resultArea.setText(text);
        }
    }

    private String runOcr(String imagePath) {
        try {
            // 生成临时 PowerShell 脚本
            String psScript = "$filePath = \"" + imagePath.replace("\\", "\\\\") + "\"\n" +
                    "$file = [Windows.Storage.StorageFile]::GetFileFromPathAsync($filePath).GetAwaiter().GetResult()\n" +
                    "$stream = $file.OpenAsync([Windows.Storage.FileAccessMode]::Read).GetAwaiter().GetResult()\n" +
                    "$decoder = [Windows.Graphics.Imaging.BitmapDecoder]::CreateAsync($stream).GetAwaiter().GetResult()\n" +
                    "$bitmap = $decoder.GetSoftwareBitmapAsync().GetAwaiter().GetResult()\n" +
                    "$ocrEngine = [Windows.Media.Ocr.OcrEngine]::TryCreateFromUserProfileLanguages()\n" +
                    "$result = $ocrEngine.RecognizeAsync($bitmap).GetAwaiter().GetResult()\n" +
                    "Write-Output $result.Text";

            File tempScript = File.createTempFile("ocr", ".ps1");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempScript))) {
                writer.write(psScript);
            }

            // 调用 PowerShell 执行
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-ExecutionPolicy", "Bypass", "-File", tempScript.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            process.waitFor();
            tempScript.delete();
            return sb.toString().trim();

        } catch (Exception ex) {
            ex.printStackTrace();
            return "OCR 出错: " + ex.getMessage();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WindowsOcrDemo demo = new WindowsOcrDemo();
            demo.setVisible(true);
        });
    }
}
