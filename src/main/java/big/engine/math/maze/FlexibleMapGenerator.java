package big.engine.math.maze;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FlexibleMapGenerator {
    public static final int WALL = 0;
    public static final int FLOOR = 1;

    public enum Mode { ARENA, LABYRINTH, MIXED }

    public static void main(String[] args) {
        int width = 61, height = 41;
        Mode mode = Mode.ARENA;   // 切换模式：ARENA, LABYRINTH, MIXED
        boolean preview = true;
        long seed = System.nanoTime();

        int[][] map = generateMap(width, height, mode, seed, preview);

        System.out.println(toAscii(map));
    }

    public static int[][] generateMap(int width, int height, Mode mode, long seed, boolean preview) {
        if (width % 2 == 0) width--;
        if (height % 2 == 0) height--;

        int[][] grid = new int[height][width];
        Random rng = new Random(seed);

        switch (mode) {
            case ARENA -> generateArena(grid, rng, 0.08);
            case LABYRINTH -> generateLabyrinth(grid, rng);
            case MIXED -> {
                generateLabyrinth(grid, rng);
                addExtraPassages(grid, rng, (width * height) / 30);
                addObstacles(grid, rng, 20);
            }
        }

        // 移除边界围墙
        for (int x = 0; x < width; x++) {
            grid[0][x] = FLOOR;
            grid[height-1][x] = FLOOR;
        }
        for (int y = 0; y < height; y++) {
            grid[y][0] = FLOOR;
            grid[y][width-1] = FLOOR;
        }

        // 打通被围死的区域
        connectIsolatedAreas(grid, rng);

        if (preview) {
            JFrame f = new JFrame("Map Preview - " + mode);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new ArenaPanel(grid, 12));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        }

        return grid;
    }

    // -------- 模式生成 --------

    private static void generateArena(int[][] grid, Random rng, double density) {
        int h = grid.length, w = grid[0].length;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                grid[y][x] = FLOOR;

        int obstacles = (int) (w * h * density);
        for (int i = 0; i < obstacles; i++) {
            int x = rng.nextInt(w - 2) + 1;
            int y = rng.nextInt(h - 2) + 1;
            int type = rng.nextInt(4);

            switch (type) {
                case 0 -> grid[y][x] = WALL; // 单点
                case 1 -> { // 方块
                    for (int dy = 0; dy < 2; dy++)
                        for (int dx = 0; dx < 2; dx++)
                            if (y + dy < h && x + dx < w)
                                grid[y + dy][x + dx] = WALL;
                }
                case 2 -> { // 十字
                    grid[y][x] = WALL;
                    if (x + 1 < w) grid[y][x + 1] = WALL;
                    if (x - 1 >= 0) grid[y][x - 1] = WALL;
                    if (y + 1 < h) grid[y + 1][x] = WALL;
                    if (y - 1 >= 0) grid[y - 1][x] = WALL;
                }
                case 3 -> { // 矩形墙带
                    int ww = 3 + rng.nextInt(3);
                    int hh = 1 + rng.nextInt(2);
                    for (int dy = 0; dy < hh; dy++)
                        for (int dx = 0; dx < ww; dx++)
                            if (y + dy < h && x + dx < w)
                                grid[y + dy][x + dx] = WALL;
                }
            }
        }
    }

    private static void generateLabyrinth(int[][] grid, Random rng) {
        int h = grid.length, w = grid[0].length;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                grid[y][x] = WALL;

        int sx = (rng.nextInt(w / 2) * 2) + 1;
        int sy = (rng.nextInt(h / 2) * 2) + 1;
        Deque<int[]> stack = new ArrayDeque<>();
        grid[sy][sx] = FLOOR;
        stack.push(new int[]{sx, sy});

        int[][] dirs = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int cx = cur[0], cy = cur[1];
            boolean carved = false;
            shuffle(dirs, rng);
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx > 0 && nx < w - 1 && ny > 0 && ny < h - 1 && grid[ny][nx] == WALL) {
                    grid[cy + d[1] / 2][cx + d[0] / 2] = FLOOR;
                    grid[ny][nx] = FLOOR;
                    stack.push(new int[]{nx, ny});
                    carved = true;
                    break;
                }
            }
            if (!carved) stack.pop();
        }
    }

    // -------- 改造工具 --------

    private static void addExtraPassages(int[][] grid, Random rng, int count) {
        int h = grid.length, w = grid[0].length;
        for (int i = 0; i < count; i++) {
            int x = 1 + rng.nextInt(w - 2);
            int y = 1 + rng.nextInt(h - 2);
            if (grid[y][x] == WALL) grid[y][x] = FLOOR;
        }
    }

    private static void addObstacles(int[][] grid, Random rng, int count) {
        int h = grid.length, w = grid[0].length;
        for (int i = 0; i < count; i++) {
            int x = 2 + rng.nextInt(w - 4);
            int y = 2 + rng.nextInt(h - 4);
            grid[y][x] = WALL;
        }
    }

    // -------- 死区打通 --------

    private static void connectIsolatedAreas(int[][] grid, Random rng) {
        int h = grid.length, w = grid[0].length;
        int[][] comp = new int[h][w];
        int compId = 0;
        Map<Integer, Integer> compSize = new HashMap<>();

        // flood fill 找连通区域
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (grid[y][x] == FLOOR && comp[y][x] == 0) {
                    compId++;
                    int size = floodFill(grid, comp, x, y, compId);
                    compSize.put(compId, size);
                }
            }
        }

        if (compId <= 1) return; // 已经连通

        // 找最大区域
        int mainComp = compSize.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();

        // 其他区域打通到主区域
        for (int id = 1; id <= compId; id++) {
            if (id == mainComp) continue;

            List<int[]> borderWalls = new ArrayList<>();
            for (int y = 1; y < h - 1; y++) {
                for (int x = 1; x < w - 1; x++) {
                    if (comp[y][x] == id) {
                        // 找到相邻墙 -> 另一边是主区域
                        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                        for (int[] d : dirs) {
                            int nx = x + d[0], ny = y + d[1];
                            if (grid[ny][nx] == WALL) {
                                int nnx = nx + d[0], nny = ny + d[1];
                                if (nnx>=0 && nnx<w && nny>=0 && nny<h && comp[nny][nnx] == mainComp) {
                                    borderWalls.add(new int[]{nx, ny});
                                }
                            }
                        }
                    }
                }
            }
            if (!borderWalls.isEmpty()) {
                int[] wcell = borderWalls.get(rng.nextInt(borderWalls.size()));
                grid[wcell[1]][wcell[0]] = FLOOR;
            }
        }
    }

    private static int floodFill(int[][] grid, int[][] comp, int sx, int sy, int id) {
        int h = grid.length, w = grid[0].length;
        Deque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{sx, sy});
        comp[sy][sx] = id;
        int size = 0;
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int x = cur[0], y = cur[1];
            size++;
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = x + d[0], ny = y + d[1];
                if (nx>=0 && nx<w && ny>=0 && ny<h && grid[ny][nx]==FLOOR && comp[ny][nx]==0) {
                    comp[ny][nx] = id;
                    q.add(new int[]{nx, ny});
                }
            }
        }
        return size;
    }

    // -------- Utils --------

    private static void shuffle(int[][] dirs, Random rng) {
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
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
