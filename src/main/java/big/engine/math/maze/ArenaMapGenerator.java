package big.engine.math.maze;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ArenaMapGenerator {
    public static final int WALL = 0;
    public static final int FLOOR = 1;

    public static void main(String[] args) {
        int width = 61;
        int height = 41;
        boolean preview = true;
        double density = 0.08;

        int[][] map = generateArena(width, height, density, System.nanoTime(), preview);

        System.out.println(toAscii(map));
    }

    public static int[][] generateArena(int width, int height, double density, long seed, boolean preview) {
        int[][] grid = new int[height][width];
        Random rng = new Random(seed);

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                grid[y][x] = FLOOR;

        // 随机撒点障碍
        int obstacles = (int)(width * height * density);
        for (int i = 0; i < obstacles; i++) {
            int x = rng.nextInt(width-2)+1;
            int y = rng.nextInt(height-2)+1;
            int type = rng.nextInt(4);

            switch (type) {
                case 0: // 单点
                    grid[y][x] = WALL; break;
                case 1: // 方块
                    for (int dy=0; dy<2; dy++)
                        for (int dx=0; dx<2; dx++)
                            if (y+dy<height && x+dx<width)
                                grid[y+dy][x+dx] = WALL;
                    break;
                case 2: // 十字
                    grid[y][x] = WALL;
                    if (x+1<width) grid[y][x+1] = WALL;
                    if (x-1>=0) grid[y][x-1] = WALL;
                    if (y+1<height) grid[y+1][x] = WALL;
                    if (y-1>=0) grid[y-1][x] = WALL;
                    break;
                case 3: // 矩形墙带
                    int w = 3 + rng.nextInt(4);
                    int h = 1 + rng.nextInt(2);
                    for (int dy=0; dy<h; dy++)
                        for (int dx=0; dx<w; dx++)
                            if (y+dy<height && x+dx<width)
                                grid[y+dy][x+dx] = WALL;
                    break;
            }
        }

        // 边界空开，不要围墙
        for (int x = 0; x < width; x++) {
            grid[0][x] = FLOOR;
            grid[height-1][x] = FLOOR;
        }
        for (int y = 0; y < height; y++) {
            grid[y][0] = FLOOR;
            grid[y][width-1] = FLOOR;
        }

        if (preview) {
            JFrame f = new JFrame("Arena Preview");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new ArenaPanel(grid, 12));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        }

        return grid;
    }

    public static String toAscii(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int cell : row)
                sb.append(cell==FLOOR ? '.' : '#');
            sb.append('\n');
        }
        return sb.toString();
    }

    static class ArenaPanel extends JPanel {
        int[][] grid; int cell;
        ArenaPanel(int[][] g, int cellSize) {
            grid = g; cell = cellSize;
            setPreferredSize(new Dimension(g[0].length*cell, g.length*cell));
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
