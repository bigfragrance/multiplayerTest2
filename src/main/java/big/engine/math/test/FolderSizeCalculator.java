package big.engine.math.test;

import java.io.File;

public class FolderSizeCalculator {

    public static void main(String[] args) {
        // 示例路径，可以改成你需要的路径
        String path = "C:\\ProgramData";
        File root = new File(path);

        if (!root.exists() || !root.isDirectory()) {
            System.out.println("路径无效或不是文件夹: " + path);
            return;
        }

        // 遍历 root 下的直接子目录
        File[] subDirs = root.listFiles(File::isDirectory);
        if (subDirs == null) {
            System.out.println("没有子目录。");
            return;
        }

        for (File dir : subDirs) {
            long size = calculateFolderSizeWithoutSubfolders(dir);
            System.out.printf("文件夹: %s, 大小: %d 字节 (%.2f MB)%n",
                    dir.getName(), size, size / 1024.0 / 1024.0);
        }
    }

    /**
     * 计算某个文件夹内（仅第一层文件）的大小，不递归子目录
     */
    private static long calculateFolderSizeWithoutSubfolders(File folder) {
        long size = 0;
        File[] files = folder.listFiles(file -> file.isFile()); // 只取文件
        if (files != null) {
            for (File f : files) {
                size += f.length();
            }
        }
        return size;
    }
}