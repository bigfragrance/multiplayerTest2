package big.engine.math.maze;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * CaveMazeGeneratorStepByStep.java
 *
 * 单文件程序：生成“洞穴/街区风格”地图，中心保留 9x9 广场，
 * 并逐步显示生成过程与逐格连接孤立区域的过程。
 *
 * 运行：javac CaveMazeGeneratorStepByStep.java
 *      java CaveMazeGeneratorStepByStep
 */
public class CaveMazeGeneratorStepByStep extends JPanel {
    // 地图参数
    private final int cols;
    private final int rows;
    private final int cellSize;

    // 地图数据
    private char[][] map; // '#' 墙, ' ' 路
    private final Random rand = new Random();

    // 中央广场参数（可改）
    private final int plazaW = 5;
    private final int plazaH = 5;
    private int plazaX, plazaY; // 广场左上坐标

    // 元胞规则参数
    private final double initialWallProb = 0.5;
    private final int smoothIterationsTotal = 15;

    // 状态机
    private Stage stage = Stage.RANDOM_FILL;
    private int smoothStep = 0;

    // 可达性
    private boolean[][] reachable; // 从广场 BFS 到达标记

    // 连接（挖隧道）相关
    private Deque<Point> pendingCarve = new ArrayDeque<>(); // 当前要逐格挖的路径
    private Point currentCarving = null; // 当前高亮的正在挖的格子
    private int tunnelsMade = 0;

    // UI 控制
    private final Timer autoTimer;
    private final JLabel statusLabel = new JLabel("prepare");
    private final JButton stepButton = new JButton("step");
    private final JButton autoButton = new JButton("auto");
    private final JButton regenButton = new JButton("regen");
    private final JSlider speedSlider = new JSlider(0, 800, 200); // ms

    // 显示哪部分是广场 (绘制用)
    private boolean[][] isPlaza;

    // 阶段枚举
    private enum Stage {
        RANDOM_FILL, PLAZA, SMOOTHING, CONNECTIVITY_PLAN, CONNECTIVITY_CARVING, DONE
    }

    public CaveMazeGeneratorStepByStep(int cols, int rows, int cellSize) {
        this.cols = cols;
        this.rows = rows;
        this.cellSize = cellSize;
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize + 30));
        setBackground(Color.WHITE);

        // 计算广场位置（居中）
        plazaX = (cols - plazaW) / 2;
        plazaY = (rows - plazaH) / 2;

        initUIControls();

        // 自动模式定时器：每 tick 触发一步
        autoTimer = new javax.swing.Timer(speedSlider.getValue(), e -> doStep());
        speedSlider.addChangeListener(e -> {
            if (!speedSlider.getValueIsAdjusting()) {
                autoTimer.setDelay(speedSlider.getValue());
            }
        });

        // 初次生成
        generateNewMap();
    }

    private void initUIControls() {
        // Step 按钮
        stepButton.addActionListener(e -> doStep());

        // Auto 按钮
        autoButton.addActionListener(e -> {
            if (autoTimer.isRunning()) {
                autoTimer.stop();
                autoButton.setText("auto");
            } else {
                autoTimer.setDelay(speedSlider.getValue());
                autoTimer.start();
                autoButton.setText("stop");
            }
        });

        // Regen 按钮
        regenButton.addActionListener(e -> {
            if (autoTimer.isRunning()) {
                autoTimer.stop();
                autoButton.setText("auto");
            }
            generateNewMap();
        });
    }

    /** 重新生成地图（回到起始阶段） */
    private void generateNewMap() {
        stage = Stage.RANDOM_FILL;
        smoothStep = 0;
        tunnelsMade = 0;
        pendingCarve.clear();
        currentCarving = null;
        isPlaza = new boolean[rows][cols];
        randomFill(initialWallProb);
        reachable = null;
        statusLabel.setText("state:random fill");
        repaint();
    }

    /** 随机填充地图 */
    private void randomFill(double probWall) {
        map = new char[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                // 边界保持墙，内部随机
                if (x == 0 || y == 0 || x == cols - 1 || y == rows - 1) {
                    //map[y][x] = '#';
                } else {
                    map[y][x] = (rand.nextDouble() < probWall) ? '#' : ' ';
                }
            }
        }
        addCentralPlaza();
    }

    /** 做一次平滑（元胞自动机规则） */
    private void smoothOnce() {
        char[][] newMap = new char[rows][cols];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (x == 0 || y == 0 || x == cols - 1 || y == rows - 1) {
                    //newMap[y][x] = '#';
                } else {
                    int walls = countNeighborWalls(x, y);
                    if (walls > 4) newMap[y][x] = '#';
                    else if (walls < 4) newMap[y][x] = ' ';
                    else newMap[y][x] = map[y][x];
                }
            }
        }
        map = newMap;
    }

    private int countNeighborWalls(int x, int y) {
        int c = 0;
        for (int ny = y - 1; ny <= y + 1; ny++) {
            for (int nx = x - 1; nx <= x + 1; nx++) {
                if (nx == x && ny == y) continue;
                if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) {
                    c++; // 越界当墙处理
                } else if (map[ny][nx] == '#') c++;
            }
        }
        return c;
    }

    /** 在中心加入广场（立即挖通） */
    private void addCentralPlaza() {
        isPlaza = new boolean[rows][cols];
        for (int y = 0; y < plazaH; y++) {
            for (int x = 0; x < plazaW; x++) {
                int gx = plazaX + x;
                int gy = plazaY + y;
                if (gx >= 0 && gy >= 0 && gx < cols && gy < rows) {
                    map[gy][gx] = ' ';
                    isPlaza[gy][gx] = true;
                }
            }
        }
    }

    /** 计算从广场出发的可达区域（4连通） */
    private boolean[][] computeReachableFromPlaza() {
        boolean[][] seen = new boolean[rows][cols];
        ArrayDeque<Point> q = new ArrayDeque<>();
        // 把所有广场格子作为初始源
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (isPlaza[y][x] && map[y][x] == ' ') {
                    seen[y][x] = true;
                    q.add(new Point(x, y));
                }
            }
        }
        // 如果广场格子有空(通常会)，BFS 向外扩展
        while (!q.isEmpty()) {
            Point p = q.removeFirst();
            int x = p.x, y = p.y;
            int[] dx = {1, -1, 0, 0};
            int[] dy = {0, 0, 1, -1};
            for (int i = 0; i < 4; i++) {
                int nx = x + dx[i], ny = y + dy[i];
                if (nx >= 0 && ny >= 0 && nx < cols && ny < rows) {
                    if (!seen[ny][nx] && map[ny][nx] == ' ') {
                        seen[ny][nx] = true;
                        q.add(new Point(nx, ny));
                    }
                }
            }
        }
        return seen;
    }

    /** 获取所有未被广场连通的可通行格子 */
    private java.util.List<Point> getUnreachableOpenCells(boolean[][] reachableMap) {
        List<Point> list = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (map[y][x] == ' ' && !reachableMap[y][x]) {
                    list.add(new Point(x, y));
                }
            }
        }
        return list;
    }

    /** 规划一条通道（pendingCarve）把一个孤立点连接到某个已连通点 */
    private void planCarveToConnect(List<Point> unreachable, boolean[][] reachableMap) {
        if (unreachable.isEmpty()) return;
        // 随机选择一个孤立的点作为源（可以优化为选最大的组件，这里随机）
        Point src = unreachable.get(rand.nextInt(unreachable.size()));

        // 在所有已连通的点中找到最近的（曼哈顿距离）
        Point bestTarget = null;
        int bestDist = Integer.MAX_VALUE;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (reachableMap[y][x]) {
                    int dist = Math.abs(x - src.x) + Math.abs(y - src.y);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestTarget = new Point(x, y);
                    }
                }
            }
        }
        if (bestTarget == null) {
            // 如果没有已连通点（理论上不会），直接连到广场中央
            bestTarget = new Point(plazaX + plazaW / 2, plazaY + plazaH / 2);
        }

        // 生成一条曼哈顿路径（带点随机顺序，使路径看起来更自然）
        List<Point> path = new ArrayList<>();
        int cx = src.x, cy = src.y;
        path.add(new Point(cx, cy));
        while (cx != bestTarget.x || cy != bestTarget.y) {
            boolean moveX;
            if (cx == bestTarget.x) moveX = false;
            else if (cy == bestTarget.y) moveX = true;
            else moveX = rand.nextBoolean();

            if (moveX) {
                cx += Integer.signum(bestTarget.x - cx);
            } else {
                cy += Integer.signum(bestTarget.y - cy);
            }
            path.add(new Point(cx, cy));
        }

        // 将路径压入 pendingCarve（从起点到终点）
        pendingCarve.clear();
        for (Point p : path) {
            pendingCarve.add(p);
        }
        currentCarving = null;
        tunnelsMade++;
    }

    /** 执行 pendingCarve 的一步挖掘（逐格挖） */
    private void executeCarveOneStep() {
        if (pendingCarve.isEmpty()) {
            currentCarving = null;
            return;
        }
        Point p = pendingCarve.removeFirst();
        // 挖通此格（变成路）
        if (p.x >= 1 && p.x < cols - 1 && p.y >= 1 && p.y < rows - 1) {
            map[p.y][p.x] = ' ';
            // 可选：为了更自然，周围也可以扩一点宽度（这里不扩）
        }
        currentCarving = p;
    }

    /** 执行“一步”的逻辑（驱动状态机） */
    private void doStep() {
        switch (stage) {
            case RANDOM_FILL -> {
                // 随机填充已经完成 -> 进入平滑阶段
                stage = Stage.SMOOTHING;
                smoothStep = 0;
                statusLabel.setText("state:smoothing 0/" + smoothIterationsTotal);
            }
            case SMOOTHING -> {
                if (smoothStep < smoothIterationsTotal) {
                    smoothOnce();
                    smoothStep++;
                    statusLabel.setText("state:smoothing " + smoothStep + "/" + smoothIterationsTotal);
                }
                if (smoothStep >= smoothIterationsTotal) {
                    stage = Stage.PLAZA;
                    statusLabel.setText("state:plaza");
                }
            }
            case PLAZA -> {
                addCentralPlaza();
                statusLabel.setText("state:connectivity plan");
                // 进入连通规划阶段
                reachable = computeReachableFromPlaza();
                List<Point> unreachable = getUnreachableOpenCells(reachable);
                if (unreachable.isEmpty()) {
                    stage = Stage.DONE;
                    statusLabel.setText("done!");
                } else {
                    stage = Stage.CONNECTIVITY_PLAN;
                    statusLabel.setText("state:connectivity plan: " + unreachable.size() + " unreachable");
                }
            }
            case CONNECTIVITY_PLAN -> {
                // 计算最新可达性并计划一条通道
                reachable = computeReachableFromPlaza();
                List<Point> unreachable = getUnreachableOpenCells(reachable);
                if (unreachable.isEmpty()) {
                    stage = Stage.DONE;
                    statusLabel.setText("done!");
                } else {
                    planCarveToConnect(unreachable, reachable);
                    stage = Stage.CONNECTIVITY_CARVING;
                    statusLabel.setText("state:connectivity carving: " + tunnelsMade + " tunnels made, " + (unreachable.size() - 1) + " unreachable");
                }
            }
            case CONNECTIVITY_CARVING -> {
                // 每一步挖一格，直到 pendingCarve 清空，再回到 PLAN
                if (!pendingCarve.isEmpty()) {
                    executeCarveOneStep();
                    statusLabel.setText("state:connectivity carving: " + tunnelsMade + " tunnels made, " + pendingCarve.size() + " pending");
                } else {
                    // 当前隧道挖完，重新计算连通性
                    reachable = computeReachableFromPlaza();
                    List<Point> unreachable = getUnreachableOpenCells(reachable);
                    if (unreachable.isEmpty()) {
                        stage = Stage.DONE;
                        currentCarving = null;
                        statusLabel.setText("done!");
                    } else {
                        stage = Stage.CONNECTIVITY_PLAN;
                        currentCarving = null;
                        statusLabel.setText("state:connectivity plan: " + unreachable.size() + " unreachable");
                    }
                }
            }
            case DONE -> {
                statusLabel.setText("done!");
                currentCarving = null;
                // done, nothing to do
            }
        }
        repaint();
    }

    /** 绘制地图 */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // 先计算 reachable（如果存在）
        if (reachable == null) {
            reachable = computeReachableFromPlaza();
        }

        // 绘制格子
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int px = x * cellSize;
                int py = y * cellSize;
                if (isPlaza[y][x]) {
                    // 广场：浅蓝
                    g.setColor(new Color(200, 230, 255));
                } else if (map[y][x] == '#') {
                    // 墙
                    g.setColor(new Color(50, 50, 50));
                } else {
                    // 通路：如果已连通，显示亮一点；否则更暗
                    if (reachable[y][x]) g.setColor(new Color(230, 230, 230));
                    else g.setColor(new Color(190, 190, 190));
                }
                g.fillRect(px, py, cellSize, cellSize);

                // 网格线（可选）
                g.setColor(new Color(140, 140, 140));
                g.drawRect(px, py, cellSize, cellSize);
            }
        }

        // 高亮当前正在挖掘的格子
        if (currentCarving != null) {
            int cx = currentCarving.x * cellSize;
            int cy = currentCarving.y * cellSize;
            g.setColor(new Color(255, 100, 100, 200));
            g.fillRect(cx, cy, cellSize, cellSize);
            g.setColor(Color.RED);
            g.drawRect(cx, cy, cellSize, cellSize);
        }

        // 状态文字
        g.setColor(Color.BLACK);
        g.drawString(statusLabel.getText(), 6, rows * cellSize + 16);
    }

    /** 创建并显示窗体 */
    private void createAndShowGUI() {
        JFrame frame = new JFrame("maze gen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 面板
        frame.add(this, BorderLayout.CENTER);

        // 控制条
        JPanel controls = new JPanel();
        controls.setLayout(new FlowLayout(FlowLayout.LEFT));
        controls.add(stepButton);
        controls.add(autoButton);
        controls.add(regenButton);
        controls.add(new JLabel("speed(ms)"));
        controls.add(speedSlider);
        controls.add(new JLabel("  "));
        controls.add(statusLabel);

        frame.add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** main */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 推荐尺寸：列 61、行 41，格子 12px（可以自行调整）
            CaveMazeGeneratorStepByStep panel = new CaveMazeGeneratorStepByStep(131, 131, 4);
            panel.createAndShowGUI();
        });
    }
}
