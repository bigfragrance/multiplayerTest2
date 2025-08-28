package big.engine.math.maze;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class ArenaMap {
    private static final int WALL = 1;
    private static final int FLOOR = 0;
    private final int width, height;
    private final int[][] map;
    private final Random rand = new Random();

    public enum Mode {
        MAZE, ARENA
    }

    public ArenaMap(int width, int height, Mode mode) {
        this.width = width;
        this.height = height;
        this.map = new int[height][width];
        if (mode == Mode.MAZE) {
            generateMaze();
        } else {
            generateArena();
        }
        removeDeadEnds();
    }

    private void generateMaze() {
        for (int y = 0; y < height; y++) {
            Arrays.fill(map[y], WALL);
        }
        carve(1, 1);
    }

    private void carve(int x, int y) {
        map[y][x] = FLOOR;
        int[] dirs = {0, 1, 2, 3};
        shuffleArray(dirs);
        for (int dir : dirs) {
            int nx = x, ny = y;
            if (dir == 0) nx += 2;
            if (dir == 1) nx -= 2;
            if (dir == 2) ny += 2;
            if (dir == 3) ny -= 2;
            if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1 && map[ny][nx] == WALL) {
                map[(y + ny) / 2][(x + nx) / 2] = FLOOR;
                carve(nx, ny);
            }
        }
    }

    private void generateArena() {

        for (int y = 0; y < height; y++) {
            Arrays.fill(map[y], FLOOR);
        }

        int structures = (width * height) / 200;
        for (int i = 0; i < structures; i++) {
            int cx = rand.nextInt(width - 10) + 5;
            int cy = rand.nextInt(height - 10) + 5;
            int type = rand.nextInt(4);

            switch (type) {
                case 0 -> generateBlock(cx, cy);
                case 1 -> generateCross(cx, cy);
                case 2 -> generateRing(cx, cy);
                case 3 -> generateCluster(cx, cy);
            }
        }
    }

    private void generateBlock(int cx, int cy) {
        int w = rand.nextInt(4, 8);
        int h = rand.nextInt(4, 8);
        for (int y = cy - h / 2; y <= cy + h / 2; y++) {
            for (int x = cx - w / 2; x <= cx + w / 2; x++) {
                if (inBounds(x, y)) map[y][x] = WALL;
            }
        }
    }

    private void generateCross(int cx, int cy) {
        int size = rand.nextInt(3, 6);
        for (int d = -size; d <= size; d++) {
            if (inBounds(cx + d, cy)) map[cy][cx + d] = WALL;
            if (inBounds(cx, cy + d)) map[cy + d][cx] = WALL;
        }
    }

    private void generateRing(int cx, int cy) {
        int r = rand.nextInt(4, 7);
        for (int y = -r; y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                int dist2 = x * x + y * y;
                if (dist2 >= (r - 1) * (r - 1) && dist2 <= r * r) {
                    if (inBounds(cx + x, cy + y)) map[cy + y][cx + x] = WALL;
                }
            }
        }
    }

    private void generateCluster(int cx, int cy) {
        int parts = rand.nextInt(3, 6);
        for (int i = 0; i < parts; i++) {
            int dx = rand.nextInt(-3, 4);
            int dy = rand.nextInt(-3, 4);
            if (inBounds(cx + dx, cy + dy)) map[cy + dy][cx + dx] = WALL;
        }
    }

    private void removeDeadEnds() {
        boolean changed;
        do {
            changed = false;
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (map[y][x] == FLOOR) {
                        int walls = 0;
                        if (map[y + 1][x] == WALL) walls++;
                        if (map[y - 1][x] == WALL) walls++;
                        if (map[y][x + 1] == WALL) walls++;
                        if (map[y][x - 1] == WALL) walls++;
                        if (walls >= 3) {
                            map[y][x] = WALL;
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
    }

    private boolean inBounds(int x, int y) {
        return x > 0 && y > 0 && x < width - 1 && y < height - 1;
    }

    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    public int[][] getMap() {
        return map;
    }

    // --- UI 预览 ---
    public void showUI() {
        JFrame frame = new JFrame("Arena Map Preview");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width * 10, height * 10);
        frame.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        g.setColor(map[y][x] == WALL ? Color.BLACK : Color.WHITE);
                        g.fillRect(x * 10, y * 10, 10, 10);
                    }
                }
            }
        });
        frame.setVisible(true);
    }

    // 测试入口
    public static void main(String[] args) {
        boolean showUI = true;
        ArenaMap arena = new ArenaMap(91, 91, Mode.ARENA);
        if (showUI) arena.showUI();
    }
}
