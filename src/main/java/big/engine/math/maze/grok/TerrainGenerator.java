package big.engine.math.maze.grok;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TerrainGenerator {

    private static final int SIZE = 41; // Odd size for perfect center symmetry
    private static final int CELL_SCALE = 10; // Pixel scale for UI preview

    private static boolean enableUIPreview = true;
    private static boolean enableCenterSymmetry = true;

    public static void main(String[] args) {
        int[][] map = new int[SIZE][SIZE];
        generateTerrain(map);

        if (enableCenterSymmetry) {
            applyCenterSymmetry(map);
        }

        if (enableUIPreview) {
            previewMap(map);
        } else {
            printMap(map);
        }
    }

    private static void generateTerrain(int[][] map) {
        // Initialize map to open space (0)
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                map[i][j] = 0;
            }
        }

        Random rand = new Random();
        recursiveDivision(map, 0, 0, SIZE, SIZE, rand);
    }

    private static void recursiveDivision(int[][] map, int x, int y, int width, int height, Random rand) {
        if (width <= 5 || height <= 5) {
            return; // Stop recursion for larger open spaces
        }

        boolean horizontal = rand.nextBoolean();
        if (width > height) {
            horizontal = false; // Prefer vertical split if wider
        } else if (height > width) {
            horizontal = true; // Prefer horizontal split if taller
        }

        if (horizontal) {
            // Add horizontal wall
            int wallY = y + 1 + rand.nextInt(height - 2);
            for (int i = x; i < x + width; i++) {
                map[wallY][i] = 1;
            }
            // Add gap (door)
            int gapX = x + rand.nextInt(width);
            map[wallY][gapX] = 0;

            // Recurse on sub-regions
            recursiveDivision(map, x, y, width, wallY - y, rand);
            recursiveDivision(map, x, wallY + 1, width, y + height - wallY - 1, rand);
        } else {
            // Add vertical wall
            int wallX = x + 1 + rand.nextInt(width - 2);
            for (int i = y; i < y + height; i++) {
                map[i][wallX] = 1;
            }
            // Add gap (door)
            int gapY = y + rand.nextInt(height);
            map[gapY][wallX] = 0;

            // Recurse on sub-regions
            recursiveDivision(map, x, y, wallX - x, height, rand);
            recursiveDivision(map, wallX + 1, y, x + width - wallX - 1, height, rand);
        }
    }

    private static void applyCenterSymmetry(int[][] map) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE / 2 + 1; col++) {
                int symRow = SIZE - 1 - row;
                int symCol = SIZE - 1 - col;
                boolean isWall = (map[row][col] == 1) || (map[symRow][symCol] == 1);
                map[row][col] = isWall ? 1 : 0;
                map[symRow][symCol] = isWall ? 1 : 0;
            }
        }
    }

    private static void previewMap(int[][] map) {
        JFrame frame = new JFrame("Terrain Preview");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new MapPanel(map));
        frame.pack();
        frame.setVisible(true);
    }

    private static void printMap(int[][] map) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                System.out.print(map[y][x] == 1 ? "#" : ".");
            }
            System.out.println();
        }
    }

    static class MapPanel extends JPanel {
        private final int[][] map;

        MapPanel(int[][] map) {
            this.map = map;
            setPreferredSize(new java.awt.Dimension(SIZE * CELL_SCALE, SIZE * CELL_SCALE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (map[y][x] == 1) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    g.fillRect(x * CELL_SCALE, y * CELL_SCALE, CELL_SCALE, CELL_SCALE);
                }
            }
        }
    }
}