package big.engine.math.maze;

import java.util.*;

public class MazeGenerator {
    private final int width;
    private final int height;
    private final char[][] maze;
    private final Random random = new Random();

    // 墙壁
    private static final char WALL = '#';
    // 通道
    private static final char PATH = ' ';

    public MazeGenerator(int width, int height) {
        // 为了保证奇数大小（方便墙壁和通道交替）
        this.width = (width % 2 == 0 ? width + 1 : width);
        this.height = (height % 2 == 0 ? height + 1 : height);
        this.maze = new char[this.height][this.width];
        generate();
    }

    private void generate() {
        // 初始化全部为墙
        for (int y = 0; y < height; y++) {
            Arrays.fill(maze[y], WALL);
        }

        // 从随机起点生成迷宫
        int startX = random.nextInt(width / 2) * 2 + 1;
        int startY = random.nextInt(height / 2) * 2 + 1;
        carve(startX, startY);
    }

    private void carve(int x, int y) {
        maze[y][x] = PATH;

        int[] dirs = {0, 1, 2, 3};
        shuffleArray(dirs); // 打乱方向，保证随机性

        for (int dir : dirs) {
            int dx = 0, dy = 0;
            switch (dir) {
                case 0: dx = 2; break; // 右
                case 1: dx = -2; break; // 左
                case 2: dy = 2; break; // 下
                case 3: dy = -2; break; // 上
            }

            int nx = x + dx;
            int ny = y + dy;

            if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1 && maze[ny][nx] == WALL) {
                // 打通两格之间的墙
                maze[y + dy / 2][x + dx / 2] = PATH;
                carve(nx, ny);
            }
        }
    }

    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    public void printMaze() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(maze[y][x]);
            }
            System.out.println();
        }
    }

    public char[][] getMaze() {
        return maze;
    }

    public static void main(String[] args) {
        MazeGenerator mg = new MazeGenerator(31, 21);
        mg.printMaze();
    }
}
