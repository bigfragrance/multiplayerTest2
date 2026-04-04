package test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PortScanner {

    public static void main(String[] args) {
        String targetIp = "play.edgerunners.cn";

        int timeout = 200; 

        int poolSize = 100;

        System.out.println("开始扫描 IP: " + targetIp);
        System.out.println("这可能需要几分钟时间...");

        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<Future<Integer>> futures = new ArrayList<>();


        for (int port = 1; port <= 65535; port++) {
            futures.add(portIsOpen(executor, targetIp, port, timeout));
        }

        executor.shutdown();

        try {

            int openPortsCount = 0;
            for (Future<Integer> f : futures) {
                if (f.get() != 0) {
                    System.out.println("发现开放端口: " + f.get());
                    openPortsCount++;
                }
            }
            System.out.println("扫描完成。共发现 " + openPortsCount + " 个开放端口。");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static Future<Integer> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(() -> {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), timeout);
                socket.close();
                return port;
            } catch (Exception ex) {
                return 0;
            }
        });
    }
}