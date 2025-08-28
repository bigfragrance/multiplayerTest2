package big.engine.math.maze;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class MazeGeneratorUI extends JPanel {
    private final int width;
    private final int height;
    private char[][] maze;
    private final Random random = new Random();

    private static final char WALL = '#';
    private static final char PATH = ' ';

    public MazeGeneratorUI(int width, int height) {
        this.width = (width % 2 == 0 ? width + 1 : width);
        this.height = (height % 2 == 0 ? height + 1 : height);
        generateNewMaze();
    }

    /** 重新生成迷宫 */
    public void generateNewMaze() {
        maze = new char[height][width];
        generateMaze();
        addRandomExits(4); // 默认加 4 个出口
        addExtraPassages(0.15); // 增加一些随机通道，让迷宫更自由
        addCentralPlaza(9, 9);  // 中心空地
        repaint();
    }

    /** DFS生成基础迷宫 */
    private void generateMaze() {
        for (int y = 0; y < height; y++) {
            Arrays.fill(maze[y], WALL);
        }

        int startX = random.nextInt(width / 2) * 2 + 1;
        int startY = random.nextInt(height / 2) * 2 + 1;
        carve(startX, startY);
    }

    private void carve(int x, int y) {
        maze[y][x] = PATH;
        int[] dirs = {0, 1, 2, 3};
        shuffleArray(dirs);

        for (int dir : dirs) {
            int dx = 0, dy = 0;
            switch (dir) {
                case 0 -> dx = 2; // 右
                case 1 -> dx = -2; // 左
                case 2 -> dy = 2; // 下
                case 3 -> dy = -2; // 上
            }

            int nx = x + dx;
            int ny = y + dy;

            if (nx > 0 && nx < width - 1 && ny > 0 && ny < height - 1 && maze[ny][nx] == WALL) {
                maze[y + dy / 2][x + dx / 2] = PATH;
                carve(nx, ny);
            }
        }
    }

    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    /** 在边界挖出口 */
    private void addRandomExits(int count) {
        int added = 0;
        while (added < count) {
            switch (random.nextInt(4)) {
                case 0 -> { // 上边
                    int x = random.nextInt(width / 2) * 2 + 1;
                    if (maze[1][x] == PATH) {
                        maze[0][x] = PATH;
                        added++;
                    }
                }
                case 1 -> { // 下边
                    int x = random.nextInt(width / 2) * 2 + 1;
                    if (maze[height - 2][x] == PATH) {
                        maze[height - 1][x] = PATH;
                        added++;
                    }
                }
                case 2 -> { // 左边
                    int y = random.nextInt(height / 2) * 2 + 1;
                    if (maze[y][1] == PATH) {
                        maze[y][0] = PATH;
                        added++;
                    }
                }
                case 3 -> { // 右边
                    int y = random.nextInt(height / 2) * 2 + 1;
                    if (maze[y][width - 2] == PATH) {
                        maze[y][width - 1] = PATH;
                        added++;
                    }
                }
            }
        }
    }

    /** 增加一些额外通道，让迷宫更自由 */
    private void addExtraPassages(double probability) {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (maze[y][x] == WALL && random.nextDouble() < probability) {
                    maze[y][x] = PATH;
                }
            }
        }
    }

    /** 在迷宫中心加入一个空地 */
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
        int cellSize = 20; // 每格像素大小
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (maze[y][x] == WALL) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width * 20, height * 20);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("随机迷宫");
            MazeGeneratorUI mazePanel = new MazeGeneratorUI(41, 31);

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
