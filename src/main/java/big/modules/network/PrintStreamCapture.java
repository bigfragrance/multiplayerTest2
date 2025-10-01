package big.modules.network;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class PrintStreamCapture {

    /**
     * 捕获给定 PrintStream 在 Runnable 中的输出
     *
     * @param printStream 要捕获的 PrintStream
     * @param task        需要执行的输出操作
     * @return 捕获到的输出字符串
     */
    public static String capture(PrintStream printStream, Runnable task) {
        if (printStream == null || task == null) {
            throw new IllegalArgumentException("printStream 和 task 不能为空");
        }

        // 创建一个 ByteArrayOutputStream 用来捕获输出
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream tempStream = new PrintStream(baos, true, StandardCharsets.UTF_8);

        // 保存原来的 PrintStream 引用（如果是 System.out 或 System.err）
        boolean isSystemOut = (printStream == System.out);
        boolean isSystemErr = (printStream == System.err);
        PrintStream originalStream = null;

        if (isSystemOut || isSystemErr) {
            originalStream = printStream;
            if (isSystemOut) {
                System.setOut(tempStream);
            } else {
                System.setErr(tempStream);
            }
        }

        // 执行任务
        task.run();

        // 恢复原始 PrintStream
        if (isSystemOut) {
            System.setOut(originalStream);
        } else if (isSystemErr) {
            System.setErr(originalStream);
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    // 测试
    public static void main(String[] args) {
        String output = capture(System.out, () -> {
            System.out.println("Hello captured world!");
            System.out.println("Another line.");
        });

        System.out.println("捕获结果:");
        System.out.println(output);
    }
}
