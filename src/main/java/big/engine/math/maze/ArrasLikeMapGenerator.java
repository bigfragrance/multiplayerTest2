package big.engine.math.maze;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Random;

public class ArrasLikeMapGenerator {
    public static final int WALL = 0;
    public static final int FLOOR = 1;

    public static void main(String[] args) {
        int width  = 61;
        int height = 41;
        boolean preview = true;
        long seed = System.nanoTime();

        int[][] map = generateMap(width, height, seed, preview);

        // 打印控制台简化图
        System.out.println(toAscii(map));
    }

    public static int[][] generateMap(int width, int height, long seed, boolean preview) {
        if (width % 2 == 0) width--;
        if (height % 2 == 0) height--;

        int[][] grid = new int[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                grid[y][x] = WALL;

        MazeFrame frame = null;
        if (preview) {
            frame = new MazeFrame(grid, 12);
            frame.setVisible(true);
        }

        Random rng = new Random(seed);

        // Step1: 迷宫基础框架
        carveMaze(grid, rng, frame);

        // Step2: 添加额外通路（减少迷宫指向性）
        addExtraPassages(grid, rng, (width * height) / 40, frame);

        // Step3: 添加特别地形
        addObstacles(grid, rng, 30, frame);   // 随机孤岛
        addPlazas(grid, rng, 5, frame);       // 广场

        // Step4: 移除边界围墙
        for (int x = 0; x < width; x++) {
            grid[0][x] = FLOOR;
            grid[height - 1][x] = FLOOR;
        }
        for (int y = 0; y < height; y++) {
            grid[y][0] = FLOOR;
            grid[y][width - 1] = FLOOR;
        }

        if (frame != null) frame.repaint();
        return grid;
    }

    /** DFS carve basic maze */
    private static void carveMaze(int[][] grid, Random rng, MazeFrame frame) {
        int h = grid.length, w = grid[0].length;
        int sx = (rng.nextInt(w / 2) * 2) + 1;
        int sy = (rng.nextInt(h / 2) * 2) + 1;
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        grid[sy][sx] = FLOOR;
        stack.push(new int[]{sx, sy});

        int[][] dirs = {{2,0},{-2,0},{0,2},{0,-2}};
        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int cx = cur[0], cy = cur[1];
            boolean carved = false;
            shuffle(dirs, rng);
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx > 0 && nx < w-1 && ny > 0 && ny < h-1 && grid[ny][nx] == WALL) {
                    grid[cy + d[1]/2][cx + d[0]/2] = FLOOR;
                    grid[ny][nx] = FLOOR;
                    stack.push(new int[]{nx, ny});
                    carved = true;
                    if (frame != null) frame.softRepaint();
                    break;
                }
            }
            if (!carved) stack.pop();
        }
    }

    /** Add random passages to reduce "directional" feeling */
    private static void addExtraPassages(int[][] grid, Random rng, int count, MazeFrame frame) {
        int h = grid.length, w = grid[0].length;
        for (int i = 0; i < count; i++) {
            int x = 1 + rng.nextInt(w - 2);
            int y = 1 + rng.nextInt(h - 2);
            if (grid[y][x] == WALL) {
                grid[y][x] = FLOOR;
                if (frame != null && i % 5 == 0) frame.softRepaint();
            }
        }
    }

    /** Add isolated blocks (obstacles) */
    private static void addObstacles(int[][] grid, Random rng, int count, MazeFrame frame) {
        int h = grid.length, w = grid[0].length;
        for (int i = 0; i < count; i++) {
            int x = 2 + rng.nextInt(w - 4);
            int y = 2 + rng.nextInt(h - 4);
            grid[y][x] = WALL;
        }
        if (frame != null) frame.repaint();
    }

    /** Add plazas (cleared open areas) */
    private static void addPlazas(int[][] grid, Random rng, int count, MazeFrame frame) {
        int h = grid.length, w = grid[0].length;
        for (int i = 0; i < count; i++) {
            int px = 2 + rng.nextInt(w - 6);
            int py = 2 + rng.nextInt(h - 6);
            int pw = 3 + rng.nextInt(5);
            int ph = 3 + rng.nextInt(5);
            for (int y = py; y < py + ph && y < h-1; y++)
                for (int x = px; x < px + pw && x < w-1; x++)
                    grid[y][x] = FLOOR;
        }
        if (frame != null) frame.repaint();
    }

    private static void shuffle(int[][] dirs, Random rng) {
        for (int i = dirs.length-1; i > 0; i--) {
            int j = rng.nextInt(i+1);
            int[] tmp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = tmp;
        }
    }

    public static String toAscii(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int cell : row)
                sb.append(cell == FLOOR ? '.' : '#');
            sb.append('\n');
        }
        return sb.toString();
    }

    // -------- UI 部分 --------
    static class MazeFrame extends JFrame {
        MazePanel panel;
        MazeFrame(int[][] grid, int cellSize) {
            this.panel = new MazePanel(grid, cellSize);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setContentPane(panel);
            pack();
            setLocationRelativeTo(null);
        }
        void softRepaint() {
            panel.repaint();
            try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        }
    }
    static class MazePanel extends JPanel {
        int[][] grid; int cell;
        MazePanel(int[][] grid, int cell) {
            this.grid = grid; this.cell = cell;
            setPreferredSize(new Dimension(grid[0].length*cell, grid.length*cell));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int y = 0; y < grid.length; y++)
                for (int x = 0; x < grid[0].length; x++) {
                    g.setColor(grid[y][x]==FLOOR ? Color.WHITE : Color.DARK_GRAY);
                    g.fillRect(x*cell, y*cell, cell, cell);
                }
        }
    }
}
