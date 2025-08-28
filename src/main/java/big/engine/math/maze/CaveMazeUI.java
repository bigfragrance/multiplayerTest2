package big.engine.math.maze;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class CaveMazeUI extends JPanel {
    private final int width;
    private final int height;
    private char[][] maze;
    private final Random random = new Random();

    private static final char WALL = '#';
    private static final char PATH = ' ';

    public CaveMazeUI(int width, int height) {
        this.width = width;
        this.height = height;
        generateNewMaze();
    }

    /** 生成新地图 */
    public void generateNewMaze() {
        maze = new char[height][width];
        randomFill(0.45);        // 初始随机填充 45% 墙
        for (int i = 0; i < 10; i++) {
            smoothMap();
            repaint();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 多次迭代让地图更自然
        }
        addCentralPlaza(9, 9);   // 中央广场
        repaint();
    }

    /** 随机填充地图 */
    private void randomFill(double wallProbability) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (random.nextDouble() < wallProbability || x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    maze[y][x] = WALL;
                } else {
                    maze[y][x] = PATH;
                }
            }
        }
    }

    /** 元胞自动机规则 */
    private void smoothMap() {
        char[][] newMap = new char[height][width];
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int wallCount = getNeighborWallCount(x, y);
                if (wallCount > 4) {
                    newMap[y][x] = WALL;
                } else if (wallCount < 4) {
                    newMap[y][x] = PATH;
                } else {
                    newMap[y][x] = maze[y][x];
                }
            }
        }
        maze = newMap;
    }

    /** 统计邻居墙壁数 */
    private int getNeighborWallCount(int x, int y) {
        int count = 0;
        for (int ny = y - 1; ny <= y + 1; ny++) {
            for (int nx = x - 1; nx <= x + 1; nx++) {
                if (!(nx == x && ny == y)) {
                    if (maze[ny][nx] == WALL) count++;
                }
            }
        }
        return count;
    }

    /** 中央空地 */
    private void addCentralPlaza(int plazaWidth, int plazaHeight) {
        int startX = (width - plazaWidth) / 2;
        int startY = (height - plazaHeight) / 2;
        for (int y = startY; y < startY + plazaHeight; y++) {
            for (int x = startX; x < startX + plazaWidth; x++) {
                maze[y][x] = PATH;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cellSize = 15;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (maze[y][x] == WALL) {
                    g.setColor(Color.DARK_GRAY);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                }
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width * 15, height * 15);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("随机地形迷宫");
            CaveMazeUI mazePanel = new CaveMazeUI(41, 41);

            JButton regenerateButton = new JButton("重新生成");
            regenerateButton.addActionListener(e -> mazePanel.generateNewMaze());

            frame.setLayout(new BorderLayout());
            frame.add(mazePanel, BorderLayout.CENTER);
            frame.add(regenerateButton, BorderLayout.SOUTH);

            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
