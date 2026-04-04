package test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PortScannerWithProgress {

    public static void main(String[] args) {
        // 1. 设置配置
        String targetIp = "play.edgerunners.cn"; // 目标IP
        int timeout = 200;             // 超时时间(ms)
        int poolSize = 200;            // 线程池大小 (适当增大以提高速度)
        int totalPorts = 65535;

        // 2. 初始化并发工具
        // 用于存储发现的开放端口 (线程安全)
        List<Integer> openPorts = Collections.synchronizedList(new ArrayList<>());
        // 用于记录已检查的端口数量 (原子操作，线程安全)
        AtomicInteger checkedCount = new AtomicInteger(0);

        System.out.println("开始扫描 IP: " + targetIp);
        long startTime = System.currentTimeMillis();

        // 3. 启动扫描线程池
        ExecutorService scanExecutor = Executors.newFixedThreadPool(poolSize);

        // 4. 启动进度监控线程 (每秒执行一次)
        ScheduledExecutorService monitorExecutor = Executors.newSingleThreadScheduledExecutor();
        monitorExecutor.scheduleAtFixedRate(() -> {
            int current = checkedCount.get();
            double percent = (double) current / totalPorts * 100;
            // 使用 \r 回车符覆盖当前行，实现动态刷新效果
            System.out.printf("当前进度: %d / %d (%.1f%%)\r", current, totalPorts, percent);
        }, 1, 1, TimeUnit.SECONDS);

        // 5. 提交所有扫描任务
        for (int port = 1; port <= totalPorts; port++) {
            final int currentPort = port;
            scanExecutor.execute(() -> {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(targetIp, currentPort), timeout);
                    socket.close();
                    // 如果连接成功，加入列表
                    openPorts.add(currentPort);
                } catch (Exception ignored) {
                    // 连接失败，端口关闭，忽略
                } finally {
                    // 无论成功失败，计数器+1
                    checkedCount.incrementAndGet();
                }
            });
        }

        // 6. 关闭扫描线程池并等待完成
        scanExecutor.shutdown();
        try {
            // 等待直到所有任务完成（或者等待一个很长的时间）
            boolean finished = scanExecutor.awaitTermination(1, TimeUnit.HOURS);
            if (!finished) {
                System.err.println("扫描超时未完成");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 7. 关闭监控线程
        monitorExecutor.shutdown();
        
        // 打印最终100%进度以确保显示完整
        System.out.printf("当前进度: %d / %d (100.0%%)\n", totalPorts, totalPorts);

        // 8. 处理并输出结果
        long endTime = System.currentTimeMillis();
        System.out.println("\n扫描结束，耗时: " + (endTime - startTime) / 1000 + "秒");

        if (openPorts.isEmpty()) {
            System.out.println("未发现开放端口。");
        } else {
            // 多线程执行顺序不固定，输出前先排序
            Collections.sort(openPorts);
            
            // 使用Stream流将List拼接成字符串
            String result = openPorts.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            
            System.out.println("发现的开放端口: ");
            System.out.println(result);
        }
    }
}