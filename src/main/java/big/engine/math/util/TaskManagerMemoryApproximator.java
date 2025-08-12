package big.engine.math.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.lang.management.BufferPoolMXBean;
import java.util.List;
import com.sun.management.OperatingSystemMXBean;

public class TaskManagerMemoryApproximator {

    public static long getMemoryUsed() {

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();

        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();

        long directMemoryUsed = getDirectMemoryUsed();


        long threadStackSize = estimateThreadStackSize();
        int threadCount = Thread.getAllStackTraces().keySet().size();
        long threadStackTotal = threadCount * threadStackSize;


        long jvmPrivateMemory = heapUsed + nonHeapUsed + directMemoryUsed + threadStackTotal;

        long osProcessMemory = getOSProcessMemory();

        return osProcessMemory>0?osProcessMemory:jvmPrivateMemory;
    }

    private static long getDirectMemoryUsed() {
        List<BufferPoolMXBean> bufferPools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (BufferPoolMXBean pool : bufferPools) {
            if ("direct".equals(pool.getName())) {
                return pool.getMemoryUsed();
            }
        }
        return 0;
    }


    private static long estimateThreadStackSize() {
        // 尝试通过 JVM 参数获取 -Xss 值（如未显式设置，默认 1MB）
        String xssParam = System.getProperty("java.vm.args");
        if (xssParam != null && xssParam.contains("-Xss")) {
            String[] parts = xssParam.split("-Xss");
            if (parts.length > 1) {
                String xssValue = parts[1].split(" ")[0];
                try {
                    return parseMemorySize(xssValue);
                } catch (NumberFormatException e) {
                    // 忽略解析失败
                }
            }
        }
        return 1024 * 1024; // 默认 1MB
    }


    private static long parseMemorySize(String sizeStr) {
        sizeStr = sizeStr.toLowerCase().replaceAll("\\s+", "");
        if (sizeStr.endsWith("k")) {
            return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 1)) * 1024;
        } else if (sizeStr.endsWith("m")) {
            return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 1)) * 1024 * 1024;
        } else if (sizeStr.endsWith("g")) {
            return Long.parseLong(sizeStr.substring(0, sizeStr.length() - 1)) * 1024 * 1024 * 1024;
        } else {
            return Long.parseLong(sizeStr); // 字节
        }
    }

    /**
     * （可选）通过 JVM 内部 API 获取进程私有内存（需模块化配置）
     * 仅在 HotSpot JVM 且添加 --add-exports 后可用
     */
    private static long getOSProcessMemory() {
        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            Class<?> sunOsBeanClass = Class.forName("com.sun.management.internal.OperatingSystemImpl");
            if (sunOsBeanClass.isInstance(osBean)) {
                java.lang.reflect.Field processMemoryField = sunOsBeanClass.getDeclaredField("processMemory");
                processMemoryField.setAccessible(true);
                return (long) processMemoryField.get(osBean);
            }
        } catch (Exception e) {

        }
        return 0;
    }


    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), unit);
    }
}