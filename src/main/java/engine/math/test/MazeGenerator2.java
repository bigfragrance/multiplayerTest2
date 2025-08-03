package engine.math.test;

import java.util.Random;

public class MazeGenerator2 {
    private final int width;
    private final int height;
    public final int[][] maze;
    private final Random random = new Random();
    private static final int FLOOR = 1;
    private static final int WALL = 0;
    private static final int DOOR = 2;

    public MazeGenerator2(int width, int height) {
        this.width = width;
        this.height = height;
        this.maze = new int[width][height];
        generateGameMap();
    }

    private void generateGameMap() {
        // 初始化全部为地板
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze[x][y] = FLOOR;
            }
        }


        placeRandomWalls(0.5);


        //createRooms(100, 1, 20);


        //addDoors();


        ensureConnectivity();

        createRoom((width-1)/2-4,(height-1)/2-4,9,9);
    }

    private void placeRandomWalls(double density) {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (random.nextDouble() < density) {
                    maze[x][y] = WALL;
                }
            }
        }
    }

    private void createRooms(int attempts, int minSize, int maxSize) {
        for (int i = 0; i < attempts; i++) {
            int roomWidth = random.nextInt(maxSize - minSize + 1) + minSize;
            int roomHeight = random.nextInt(maxSize - minSize + 1) + minSize;
            int x = random.nextInt(width - roomWidth - 2) + 1;
            int y = random.nextInt(height - roomHeight - 2) + 1;

            if (canPlaceRoom(x, y, roomWidth, roomHeight)) {
                createRoom(x, y, roomWidth, roomHeight);
            }
        }
    }

    private boolean canPlaceRoom(int x, int y, int width, int height) {
        for (int dy = -1; dy <= height; dy++) {
            for (int dx = -1; dx <= width; dx++) {
                if (x + dx < 0 || y + dy < 0 || x + dx >= this.width || y + dy >= this.height) {
                    return false;
                }
                if (maze[x + dx][y + dy] != WALL) {
                    return false;
                }
            }
        }
        return true;
    }

    private void createRoom(int x, int y, int width, int height) {
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < width; dx++) {
                maze[x + dx][y + dy] = FLOOR;
            }
        }
    }

    private void addDoors() {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (maze[x][y] == WALL) {

                    boolean isHorizontal = maze[x-1][y] == FLOOR && maze[x+1][y] == FLOOR;
                    boolean isVertical = maze[x][y-1] == FLOOR && maze[x][y+1] == FLOOR;

                    if ((isHorizontal || isVertical) && random.nextDouble() < 0.3) {
                        maze[x][y] = FLOOR;
                    }
                }
            }
        }
    }

    private void ensureConnectivity() {

        boolean[][] visited = new boolean[width][height];
        int startX = -1, startY = -1;


        for (int y = 1; y < height - 1 && startX == -1; y++) {
            for (int x = 1; x < width - 1 && startX == -1; x++) {
                if (maze[x][y] == FLOOR) {
                    startX = x;
                    startY = y;
                }
            }
        }

        if (startX != -1) {
            floodFill(startX, startY, visited);


            connectRegions(visited);
        }
    }

    private void floodFill(int x, int y, boolean[][] visited) {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        if (visited[x][y]) return;
        if (maze[x][y] != FLOOR) return;

        visited[x][y] = true;
        floodFill(x+1, y, visited);
        floodFill(x-1, y, visited);
        floodFill(x, y+1, visited);
        floodFill(x, y-1, visited);
    }

    private void connectRegions(boolean[][] visited) {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (maze[x][y] == WALL) {

                    boolean hasVisitedNeighbor = false;
                    boolean hasUnvisitedNeighbor = false;

                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            int nx = x + dx;
                            int ny = y + dy;
                            if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                            if (maze[nx][ny] != FLOOR) continue;

                            if (visited[nx][ny]) {
                                hasVisitedNeighbor = true;
                            } else {
                                hasUnvisitedNeighbor = true;
                            }
                        }
                    }

                    if (hasVisitedNeighbor && hasUnvisitedNeighbor) {

                        maze[x][y] = FLOOR;

                        floodFill(x, y, visited);
                    }
                }
            }
        }
    }

    public void printMaze() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                switch (maze[x][y]) {
                    case FLOOR:
                        System.out.print("  ");
                        break;
                    case WALL:
                        System.out.print("██");
                        break;
                    case DOOR:
                        System.out.print("[]");
                        break;
                }
            }
            System.out.println();
        }
    }

    public int[][] getMaze() {
        return maze;
    }

    public static void main(String[] args) {
        int width = 25;
        int height = 25;
        MazeGenerator2 generator = new MazeGenerator2(width, height);
        generator.printMaze();
    }
}    