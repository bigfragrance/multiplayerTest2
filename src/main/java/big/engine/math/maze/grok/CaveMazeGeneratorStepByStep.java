package big.engine.math.maze.grok;

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
                    double walls = countNeighborWalls(x, y);
                    if (walls > 3) newMap[y][x] = '#';
                    else if (walls < 3) newMap[y][x] = ' ';
                    else newMap[y][x] = map[y][x];
                }
            }
        }
        map = newMap;
    }

    private double countNeighborWalls(int x, int y) {
        double c = 0;
        for (int ny = y - 1; ny <= y + 1; ny++) {
            for (int nx = x - 1; nx <= x + 1; nx++) {
                if (nx == x && ny == y) continue;
                if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) {
                    c++; // 越界当墙处理
                } else if (map[ny][nx] == '#') {
                    c+=(nx==x||ny==y)?0.5:1;
                }
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
        // 随机选择一个孤立的点作为源
        Point src = unreachable.get(rand.nextInt(unreachable.size()));

        // 找到最近的已连通点
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
            bestTarget = new Point(plazaX + plazaW / 2, plazaY + plazaH / 2);
        }

        // 使用A*算法生成平滑路径
        List<Point> path = findSmoothPathAStar(src, bestTarget);

        // 将路径压入 pendingCarve
        pendingCarve.clear();
        for (Point p : path) {
            pendingCarve.add(p);
        }
        currentCarving = null;
        tunnelsMade++;
    }

    /** 使用A*算法找到从src到target的路径（允许穿过墙壁，优先路，只用4方向确保4连通） */
    private List<Point> findSmoothPathAStar(Point src, Point target) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Double> gScore = new HashMap<>();
        Map<Point, Double> fScore = new HashMap<>();

        gScore.put(src, 0.0);
        fScore.put(src, heuristic(src, target));
        openSet.add(new Node(src, fScore.get(src)));

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            Point current = currentNode.pos;

            // 跳过过时的节点
            if (fScore.containsKey(current) && currentNode.fScore > fScore.get(current)) {
                continue;
            }

            if (current.equals(target)) {
                return reconstructPath(cameFrom, current);
            }

            int[] dx = {1, -1, 0, 0}; // 只用4方向，避免对角导致不连通
            int[] dy = {0, 0, 1, -1};
            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];
                if (nx >= 0 && ny >= 0 && nx < cols && ny < rows) {
                    Point neighbor = new Point(nx, ny);
                    double cost = 1.0; // 统一成本
                    // 动态成本：墙壁成本略高，增加随机扰动
                    if (map[ny][nx] == '#') cost *= (1.3 + rand.nextDouble() * 0.2); // 1.3~1.5
                    else cost *= (0.8 + rand.nextDouble() * 0.2); // 0.8~1.0

                    double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) + cost;
                    if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                        cameFrom.put(neighbor, current);
                        gScore.put(neighbor, tentativeGScore);
                        double h = heuristic(neighbor, target);
                        fScore.put(neighbor, tentativeGScore + h);
                        openSet.add(new Node(neighbor, fScore.get(neighbor)));
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    /** A*节点 */
    private static class Node {
        Point pos;
        double fScore;

        Node(Point pos, double fScore) {
            this.pos = pos;
            this.fScore = fScore;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return pos.equals(node.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }

    /** 启发式：欧几里德距离 */
    private double heuristic(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    /** 重建A*路径 */
    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        List<Point> path = new ArrayList<>();
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    /** 执行 pendingCarve 的一步挖掘（逐格挖，动态宽度） */
    private void executeCarveOneStep() {
        if (pendingCarve.isEmpty()) {
            currentCarving = null;
            return;
        }
        Point p = pendingCarve.removeFirst();
        // 动态决定挖掘宽度（基于周围墙壁密度和随机性）
        int width = determineCarveWidth(p);
        carveVariableWidth(p, width);
        currentCarving = p;
    }

    /** 根据周围环境决定挖掘宽度（1~3格） */
    private int determineCarveWidth(Point p) {
        // 计算周围3x3区域的墙壁数量
        double walls = countNeighborWalls(p.x, p.y);
        // 墙壁越多，通道越窄；墙壁越少，通道越宽
        double wallRatio = walls / 5.0; // 0~1（不含中心格）
        // 概率分布：窄（1格）、中（2格）、宽（3格）
        double r = rand.nextDouble();
        if (wallRatio > 0.6 || r < 0.3) return 1; // 窄通道（30% 或墙壁密集）
        else if (wallRatio > 0.3 || r < 0.7) return 2; // 中等通道（40% 或中等密度）
        else return 3; // 宽通道（30% 或空旷区域）
    }

    /** 挖掘指定宽度的区域 */
    private void carveVariableWidth(Point p, int width) {
        int radius = width - 1; // width=1 -> 1x1, width=2 -> 2x2, width=3 -> 3x3
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int nx = p.x + dx;
                int ny = p.y + dy;
                if (nx >= 1 && nx < cols - 1 && ny >= 1 && ny < rows - 1) {
                    map[ny][nx] = ' ';
                }
            }
        }
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