package big.engine.math.test;

import java.util.*;

public class MazeGenerator {
    private final int width;
    private final int height;
    public final int[][] maze;
    private final Random random = new Random();
    public static final int PATH = 1;
    public static final int WALL = 0;

    public MazeGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.maze = new int[width][height];
        initializeMaze();
        generateMaze(1, 1);
        addExtraPaths(0.3);
        //ensureExit();
    }

    private void initializeMaze() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze[x][y] = WALL;
            }
        }
    }

    private void generateMaze(int cx, int cy) {
        maze[cx][cy] = PATH;
        Direction[] directions = Direction.values();
        shuffleDirections(directions);

        for (Direction dir : directions) {
            int nx = cx + dir.dx * 2;
            int ny = cy + dir.dy * 2;

            if (isValidPath(nx, ny)) {
                maze[cx + dir.dx][cy + dir.dy] = PATH;
                generateMaze(nx, ny);
            }
        }
    }

    private void addExtraPaths(double density) {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (maze[x][y] == WALL && isWallWithTwoPaths(x, y)) {
                    if (random.nextDouble() < density) {
                        maze[x][y] = PATH;
                    }
                }
            }
        }
    }

    private void ensureExit() {
        // 确保入口和出口是通路
        maze[1][0] = PATH; // 入口
        maze[width-2][height-1] = PATH; // 出口
    }

    private boolean isValidPath(int x, int y) {
        return x > 0 && y > 0 && x < width - 1 && y < height - 1
                && maze[x][y] == WALL;
    }

    private boolean isWallWithTwoPaths(int x, int y) {
        int pathCount = 0;
        for (Direction dir : Direction.values()) {
            int nx = x + dir.dx;
            int ny = y + dir.dy;
            if (isInBounds(nx, ny) && maze[nx][ny] == PATH) {
                pathCount++;
            }
        }
        return pathCount == 2;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private void shuffleDirections(Direction[] directions) {
        for (int i = directions.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            Direction temp = directions[index];
            directions[index] = directions[i];
            directions[i] = temp;
        }
    }

    public void printMaze() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(maze[x][y] == PATH ? "  " : "██");
            }
            System.out.println();
        }
    }

    private enum Direction {
        NORTH(0, -1), SOUTH(0, 1), EAST(1, 0), WEST(-1, 0);

        final int dx;
        final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    public static void main(String[] args) {
        int width = 11;
        int height = 11;
        MazeGenerator generator = new MazeGenerator(width, height);
        generator.printMaze();
    }
}    