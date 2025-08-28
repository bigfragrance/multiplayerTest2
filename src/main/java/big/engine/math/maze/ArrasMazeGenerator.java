package big.engine.math.maze;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Random;

/**
 * Arras-style Maze Generator
 * - Grid: 0 = WALL, 1 = FLOOR
 * - Algorithm: Iterative DFS (backtracking), works best with odd width/height
 * - Optional realtime UI preview controlled by 'preview' boolean
 *
 * How to run:
 *   - Direct run: just run main()
 *   - Change config in main(): width, height, seed, preview
 */
public class ArrasMazeGenerator {

    public static final int WALL = 0;
    public static final int FLOOR = 1;

    public static void main(String[] args) {
        int width  = 101;      // should be odd for best results
        int height = 101;      // should be odd for best results
        long seed  = System.nanoTime();
        boolean preview = true; // <-- toggle UI preview here

        MazeResult res = generateMaze(width, height, seed, preview);

        // Example: print a compact ASCII map to console ('.' floor, '#' wall)
        System.out.println("Seed = " + seed);
        System.out.println(toAscii(res.grid));
    }

    /** Container for result and (optional) UI */
    public static class MazeResult {
        public final int[][] grid;
        public MazeResult(int[][] grid) { this.grid = grid; }
    }

    /**
     * Generate a maze. If preview==true, show a live UI while carving.
     */
    public static MazeResult generateMaze(int width, int height, long seed, boolean preview) {
        // Enforce odd dimensions for proper walls
        if (width % 2 == 0)  width -= 1;
        if (height % 2 == 0) height -= 1;

        int[][] grid = new int[height][width];
        // init all walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = WALL;
            }
        }

        // UI setup (optional)
        MazeFrame frame = null;
        if (preview) {
            frame = new MazeFrame(grid, 12); // cell size px
            frame.setTitle("Maze Preview (seed=" + seed + ")");
            frame.setVisible(true);
        }

        Random rng = new Random(seed);

        // Start at a random odd cell
        int sx = (rng.nextInt(width  / 2) * 2) + 1;
        int sy = (rng.nextInt(height / 2) * 2) + 1;

        carveDFS(grid, sx, sy, rng, frame);

        // Optionally add a few loops to reduce linearity (good for arena feel)
        addRandomLoops(grid, rng, Math.max(width, height) / 2, frame);

        // Ensure outer boundary is walls (safety for game bounds)
        for (int x = 0; x < width; x++) { grid[0][x] = WALL; grid[height-1][x] = WALL; }
        for (int y = 0; y < height; y++) { grid[y][0] = WALL; grid[y][width-1] = WALL; }

        if (preview && frame != null) {
            frame.repaint();
        }
        return new MazeResult(grid);
    }

    /**
     * Iterative DFS carve on odd cells; knock walls by 2-step moves.
     */
    private static void carveDFS(int[][] grid, int sx, int sy, Random rng, MazeFrame frame) {
        int h = grid.length, w = grid[0].length;

        ArrayDeque<int[]> stack = new ArrayDeque<>();
        grid[sy][sx] = FLOOR;
        stack.push(new int[]{sx, sy});

        // Directions (dx,dy) representing two-step moves to neighbor cells
        final int[][] dirs = new int[][]{{2,0},{-2,0},{0,2},{0,-2}};

        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int cx = cur[0], cy = cur[1];

            // Gather unvisited neighbors two cells away
            int[][] order = shuffledDirs(dirs, rng);
            boolean carved = false;
            for (int[] d : order) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx > 0 && nx < w-1 && ny > 0 && ny < h-1 && grid[ny][nx] == WALL) {
                    // Knock wall in between
                    int mx = cx + d[0]/2, my = cy + d[1]/2;
                    grid[my][mx] = FLOOR;
                    grid[ny][nx] = FLOOR;
                    stack.push(new int[]{nx, ny});
                    carved = true;

                    if (frame != null) frame.softRepaint();
                    break;
                }
            }
            if (!carved) {
                stack.pop();
            }
        }
    }

    /**
     * Add a few random openings between adjacent corridors to create loops.
     * This can make gameplay feel less linear (more arena-like).
     */
    private static void addRandomLoops(int[][] grid, Random rng, int attempts, MazeFrame frame) {
        int h = grid.length, w = grid[0].length;
        for (int i = 0; i < attempts; i++) {
            // choose a random wall cell that has two opposite floors around it
            int x = 2 + rng.nextInt(Math.max(1, w - 4));
            int y = 2 + rng.nextInt(Math.max(1, h - 4));
            if (grid[y][x] != WALL) continue;

            boolean horizontal = rng.nextBoolean();
            if (horizontal) {
                if (x-1 >= 0 && x+1 < w && grid[y][x-1] == FLOOR && grid[y][x+1] == FLOOR) {
                    grid[y][x] = FLOOR;
                }
            } else {
                if (y-1 >= 0 && y+1 < h && grid[y-1][x] == FLOOR && grid[y+1][x] == FLOOR) {
                    grid[y][x] = FLOOR;
                }
            }
            if (frame != null && i % 2 == 0) frame.softRepaint();
        }
    }

    private static int[][] shuffledDirs(int[][] dirs, Random rng) {
        int[][] arr = new int[dirs.length][2];
        for (int i = 0; i < dirs.length; i++) {
            arr[i][0] = dirs[i][0];
            arr[i][1] = dirs[i][1];
        }
        for (int i = arr.length-1; i > 0; i--) {
            int j = rng.nextInt(i+1);
            int[] t = arr[i]; arr[i] = arr[j]; arr[j] = t;
        }
        return arr;
    }

    /** Convert grid to ASCII for quick export */
    public static String toAscii(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                sb.append(grid[y][x] == FLOOR ? '.' : '#');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // ---------- Lightweight Swing preview ----------

    static class MazeFrame extends JFrame {
        private final MazePanel panel;

        MazeFrame(int[][] grid, int cellSize) {
            this.panel = new MazePanel(grid, cellSize);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setContentPane(panel);
            pack();
            setLocationRelativeTo(null);
        }

        void softRepaint() {
            panel.repaint();
            // Small pause to visualize progress; tune as you like
            //try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        }
    }

    static class MazePanel extends JPanel {
        private final int[][] grid;
        private final int cell;

        MazePanel(int[][] grid, int cellSize) {
            this.grid = grid;
            this.cell = cellSize;
            int w = grid[0].length * cellSize;
            int h = grid.length * cellSize;
            setPreferredSize(new Dimension(w, h));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Background
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Cells
            for (int y = 0; y < grid.length; y++) {
                for (int x = 0; x < grid[0].length; x++) {
                    if (grid[y][x] == FLOOR) {
                        g.setColor(Color.WHITE);
                    } else {
                        g.setColor(Color.DARK_GRAY);
                    }
                    g.fillRect(x * cell, y * cell, cell, cell);
                }
            }
        }
    }
}
