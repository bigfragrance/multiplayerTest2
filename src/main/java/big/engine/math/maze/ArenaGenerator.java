package big.engine.math.maze;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ArenaGenerator {
    private static final int WALL = 1;
    private static final int FLOOR = 0;

    public enum Mode {
        MAZE, OPEN, ARENA, MIXED
    }

    private int width, height;
    private int[][] map;
    private Random rand = new Random();
    private boolean preview;

    public ArenaGenerator(int width, int height, boolean preview) {
        this.width = width;
        this.height = height;
        this.preview = preview;
        this.map = new int[height][width];
    }

    public int[][] generate(Mode mode) {
        for (int y = 0; y < height; y++) {
            Arrays.fill(map[y], FLOOR);
        }

        switch (mode) {
            case MAZE:
                generateMaze();
                break;
            case OPEN:
                generateOpen();
                break;
            case ARENA:
                generateArena();
                break;
            case MIXED:
                generateMixed();
                break;
        }

        ensureConnectivity();
        if (preview) showUI();
        return map;
    }


    private void generateMaze() {
        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                map[y][x] = WALL;
            }
        }
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(1, 1));
        while (!stack.isEmpty()) {
            Point p = stack.peek();
            List<Point> neighbors = new ArrayList<>();
            int[][] dirs = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
            for (int[] d : dirs) {
                int nx = p.x + d[0], ny = p.y + d[1];
                if (nx > 0 && ny > 0 && nx < width - 1 && ny < height - 1 && map[ny][nx] == WALL) {
                    neighbors.add(new Point(nx, ny));
                }
            }
            if (!neighbors.isEmpty()) {
                Point n = neighbors.get(rand.nextInt(neighbors.size()));
                map[(p.y + n.y) / 2][(p.x + n.x) / 2] = FLOOR;
                map[n.y][n.x] = FLOOR;
                stack.push(n);
            } else {
                stack.pop();
            }
        }
    }


    private void generateOpen() {
        for (int i = 0; i < (width * height) / 12; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            map[y][x] = WALL;
        }
    }


    private void generateArena() {
        // 房间
        for (int i = 0; i < 5; i++) {
            int rx = rand.nextInt(width - 10);
            int ry = rand.nextInt(height - 10);
            int rw = rand.nextInt(6) + 5;
            int rh = rand.nextInt(6) + 5;
            for (int y = ry; y < ry + rh && y < height; y++) {
                for (int x = rx; x < rx + rw && x < width; x++) {
                    map[y][x] = FLOOR;
                }
            }
        }


        for (int i = 0; i < 3; i++) {
            if (rand.nextBoolean()) {
                int y = rand.nextInt(height);
                for (int x = 0; x < width; x++) map[y][x] = FLOOR;
            } else {
                int x = rand.nextInt(width);
                for (int y = 0; y < height; y++) map[y][x] = FLOOR;
            }
        }


        for (int i = 0; i < 15; i++) {
            int cx = rand.nextInt(width);
            int cy = rand.nextInt(height);
            int size = rand.nextInt(3) + 2;
            for (int y = cy; y < cy + size && y < height; y++) {
                for (int x = cx; x < cx + size && x < width; x++) {
                    map[y][x] = WALL;
                }
            }
        }


        for (int i = 0; i < 10; i++) {
            int x = rand.nextInt(width / 2);
            int y = rand.nextInt(height / 2);
            map[y][x] = WALL;
            map[height - y - 1][x] = WALL;
            map[y][width - x - 1] = WALL;
            map[height - y - 1][width - x - 1] = WALL;
        }
    }


    private void generateMixed() {
        generateArena();
        generateOpen();
    }


    private void ensureConnectivity() {
        boolean[][] visited = new boolean[height][width];
        Queue<Point> q = new LinkedList<>();
        q.add(new Point(0, 0));
        visited[0][0] = true;

        while (!q.isEmpty()) {
            Point p = q.poll();
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = p.x + d[0], ny = p.y + d[1];
                if (nx>=0 && ny>=0 && nx<width && ny<height && !visited[ny][nx] && map[ny][nx]==FLOOR) {
                    visited[ny][nx] = true;
                    q.add(new Point(nx, ny));
                }
            }
        }


        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (map[y][x] == FLOOR && !visited[y][x]) {
                    map[y][x] = FLOOR;
                    map[y][x+1] = FLOOR;
                }
            }
        }
    }


    private void showUI() {
        JFrame frame = new JFrame("Arena Preview");
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

    public static void main(String[] args) {
        ArenaGenerator ag = new ArenaGenerator(60, 40, true);
        ag.generate(Mode.MAZE);
    }
}
