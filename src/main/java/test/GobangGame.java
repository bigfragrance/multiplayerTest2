package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class GobangGame extends JFrame {
    // Game Constants
    private static final int BOARD_SIZE = 9;
    private static final int CELL_SIZE = 60;
    private static final int MARGIN = 30;
    private static final int EMPTY = 0;
    private static final int BLACK = 1; // Human
    private static final int WHITE = 2; // AI

    // AI Configuration
    private static final int SEARCH_DEPTH = 3; // Depth of Alpha-Beta search
    private static final int SEARCH_RADIUS = 2; // Only search near existing stones

    // Game State
    private int[][] board;
    private boolean isBlackTurn = true; // Black goes first
    private boolean gameOver = false;
    private GamePanel gamePanel;
    private JLabel statusLabel;

    // Thread Pool for AI
    private final ExecutorService threadPool;

    public GobangGame() {
        setTitle("Gobang - Human vs AI");
        setSize(MARGIN * 2 + CELL_SIZE * (BOARD_SIZE - 1) + 20, MARGIN * 2 + CELL_SIZE * (BOARD_SIZE - 1) + 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Initialize Logic
        board = new int[BOARD_SIZE][BOARD_SIZE];
        // Use threads equal to available cores for maximum parallelism
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // UI Components
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        statusLabel = new JLabel("Your Turn (Black)");
        JButton restartBtn = new JButton("Restart");
        restartBtn.addActionListener(e -> restartGame());
        
        bottomPanel.add(statusLabel);
        bottomPanel.add(restartBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void restartGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        isBlackTurn = true;
        gameOver = false;
        statusLabel.setText("Your Turn (Black)");
        gamePanel.repaint();
    }

    // --- Check Win Logic ---
    private boolean checkWin(int r, int c, int player) {
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] d : directions) {
            int count = 1;
            // Check forward
            for (int i = 1; i < 5; i++) {
                int nr = r + d[0] * i, nc = c + d[1] * i;
                if (isValid(nr, nc) && board[nr][nc] == player) count++;
                else break;
            }
            // Check backward
            for (int i = 1; i < 5; i++) {
                int nr = r - d[0] * i, nc = c - d[1] * i;
                if (isValid(nr, nc) && board[nr][nc] == player) count++;
                else break;
            }
            if (count >= 5) return true;
        }
        return false;
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE;
    }

    // --- Inner Class for Drawing ---
    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(new Color(220, 190, 140)); // Wood color
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (gameOver || !isBlackTurn) return;

                    int c = Math.round((float) (e.getX() - MARGIN) / CELL_SIZE);
                    int r = Math.round((float) (e.getY() - MARGIN) / CELL_SIZE);

                    if (GobangGame.this.isValid(r, c) && board[r][c] == EMPTY) {
                        makeMove(r, c, BLACK);
                        if (!gameOver) {
                            // Trigger AI in a separate thread to avoid freezing UI
                            new Thread(GobangGame.this::aiMove).start();
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw Grid
            g2.setColor(Color.BLACK);
            for (int i = 0; i < BOARD_SIZE; i++) {
                g2.drawLine(MARGIN, MARGIN + i * CELL_SIZE, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, MARGIN + i * CELL_SIZE);
                g2.drawLine(MARGIN + i * CELL_SIZE, MARGIN, MARGIN + i * CELL_SIZE, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE);
            }

            // Draw Pieces
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if (board[i][j] != EMPTY) {
                        g2.setColor(board[i][j] == BLACK ? Color.BLACK : Color.WHITE);
                        int x = MARGIN + j * CELL_SIZE - CELL_SIZE / 2 + 2;
                        int y = MARGIN + i * CELL_SIZE - CELL_SIZE / 2 + 2;
                        g2.fillOval(x, y, CELL_SIZE - 4, CELL_SIZE - 4);
                        
                        // Draw shadow/highlight for 3D effect
                        if (board[i][j] == BLACK) g2.setColor(Color.DARK_GRAY); 
                        else g2.setColor(Color.LIGHT_GRAY);
                        g2.drawOval(x, y, CELL_SIZE - 4, CELL_SIZE - 4);
                    }
                }
            }
        }
    }

    private void makeMove(int r, int c, int player) {
        board[r][c] = player;
        gamePanel.repaint();

        if (checkWin(r, c, player)) {
            gameOver = true;
            String winner = (player == BLACK) ? "You Win!" : "AI Wins!";
            statusLabel.setText(winner);
            JOptionPane.showMessageDialog(this, winner);
        } else {
            isBlackTurn = !isBlackTurn;
            statusLabel.setText(isBlackTurn ? "Your Turn (Black)" : "AI Thinking...");
        }
    }

    // ==========================================================
    // ================= AI Core Logic (Multithreaded) ==========
    // ==========================================================

    private void aiMove() {
        long startTime = System.currentTimeMillis();

        // 1. Get candidate moves (optimization: only near existing stones)
        List<Point> candidates = getCandidateMoves();
        
        // If board is empty, play center
        if (candidates.isEmpty()) {
            SwingUtilities.invokeLater(() -> makeMove(7, 7, WHITE));
            return;
        }

        Point bestMove = null;
        int maxScore = Integer.MIN_VALUE;

        // 2. Multithreading: Submit tasks for each top-level candidate move
        List<Future<Integer>> futures = new ArrayList<>();
        List<Point> activeCandidates = new ArrayList<>();

        for (Point move : candidates) {
            // Create a deep copy of the board is expensive, so we use backtracking.
            // However, for parallel execution, we MUST have separate board states or synchronize heavily.
            // Copying int[15][15] is very fast in Java, so we copy the board for each thread.
            int[][] boardCopy = copyBoard(board);
            boardCopy[move.x][move.y] = WHITE; // Try move

            // Submit task
            Callable<Integer> task = () -> minimax(boardCopy, SEARCH_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            futures.add(threadPool.submit(task));
            activeCandidates.add(move);
        }

        // 3. Collect results
        for (int i = 0; i < futures.size(); i++) {
            try {
                int score = futures.get(i).get();
                if (score > maxScore) {
                    maxScore = score;
                    bestMove = activeCandidates.get(i);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("AI Search Time: " + duration + "ms | Score: " + maxScore);

        // 4. Execute Move on UI Thread
        Point finalMove = bestMove;
        SwingUtilities.invokeLater(() -> makeMove(finalMove.x, finalMove.y, WHITE));
    }

    // Minimax with Alpha-Beta Pruning
    private int minimax(int[][] currentBoard, int depth, int alpha, int beta, boolean isMaximizing) {
        int score = evaluateBoard(currentBoard);
        
        // Base case: check for definitive wins or depth limit
        // Optimization: If the score indicates a win (very high), stop searching.
        if (depth == 0 || Math.abs(score) > 90000) { 
            return score;
        }

        List<Point> candidates = getCandidateMoves(currentBoard);
        if (candidates.isEmpty()) return 0;

        if (isMaximizing) { // AI's turn (White) - internally inside recursion
            int maxEval = Integer.MIN_VALUE;
            for (Point move : candidates) {
                currentBoard[move.x][move.y] = WHITE;
                int eval = minimax(currentBoard, depth - 1, alpha, beta, false);
                currentBoard[move.x][move.y] = EMPTY; // Backtrack
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else { // Human's turn (Black) - simulating opponent
            int minEval = Integer.MAX_VALUE;
            for (Point move : candidates) {
                currentBoard[move.x][move.y] = BLACK;
                int eval = minimax(currentBoard, depth - 1, alpha, beta, true);
                currentBoard[move.x][move.y] = EMPTY; // Backtrack
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    // --- Optimization: Search Radius ---
    // Only return empty cells that are within a certain distance of existing stones
    private List<Point> getCandidateMoves() {
        return getCandidateMoves(this.board);
    }

    private List<Point> getCandidateMoves(int[][] b) {
        List<Point> moves = new ArrayList<>();
        // Using a set to avoid duplicates and improve lookup, but for small board, boolean array is faster
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (b[i][j] != EMPTY) {
                    // Search neighborhood
                    for (int dr = -SEARCH_RADIUS; dr <= SEARCH_RADIUS; dr++) {
                        for (int dc = -SEARCH_RADIUS; dc <= SEARCH_RADIUS; dc++) {
                            int nr = i + dr;
                            int nc = j + dc;
                            if (isValid(nr, nc) && b[nr][nc] == EMPTY && !visited[nr][nc]) {
                                moves.add(new Point(nr, nc));
                                visited[nr][nc] = true;
                            }
                        }
                    }
                }
            }
        }
        return moves;
    }

    // --- Evaluation Function ---
    // Heuristic scoring based on patterns (Live 4, Dead 4, Live 3, etc.)
    private int evaluateBoard(int[][] b) {
        int blackScore = evaluateForColor(b, BLACK);
        int whiteScore = evaluateForColor(b, WHITE);
        // AI wants to maximize (White Score - Black Score)
        // Aggressive weight to encourage AI to win or block immediately
        return whiteScore - blackScore * 2; 
    }

    // Pattern Weights
    private static final int WIN_5 = 100000;
    private static final int LIVE_4 = 10000;
    private static final int DEAD_4 = 1000;
    private static final int LIVE_3 = 1000;
    private static final int DEAD_3 = 100;
    private static final int LIVE_2 = 100;

    private int evaluateForColor(int[][] b, int color) {
        int score = 0;
        // Horizontal, Vertical, Diagonal, Anti-Diagonal
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        // To avoid double counting, we scan lines.
        // Simplified scanner for the demo: check every line segment of length 5
        // (A fully optimized eval would use bitmasks or AC automation, but this is sufficient for Java Logic)
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (b[i][j] != EMPTY) continue; // Start scan from empty spots or edges essentially handled by line logic

                // Actually, standard eval scans all lines. 
                // Let's iterate all rows/cols/diagonals and count patterns.
            }
        }
        
        // Revised Eval: Line Scanning
        score += scanLines(b, color, 1, 0); // Horizontal
        score += scanLines(b, color, 0, 1); // Vertical
        score += scanLines(b, color, 1, 1); // Diagonal
        score += scanLines(b, color, 1, -1); // Anti-Diagonal
        
        return score;
    }

    private int scanLines(int[][] b, int color, int dr, int dc) {
        int totalScore = 0;
        // Temporary logic to iterate the board based on direction is complex.
        // We will do a simpler "Point-based" evaluation looking at consecutive stones.
        // Iterate all cells, if cell == color, look forward.
        
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (b[r][c] == color && !visited[r][c]) {
                    // Trace this line
                    int count = 1;
                    int tr = r + dr, tc = c + dc;
                    while(isValid(tr, tc) && b[tr][tc] == color) {
                        visited[tr][tc] = true; // Mark as part of this line segment
                        count++;
                        tr += dr;
                        tc += dc;
                    }

                    // Check ends
                    boolean openStart = isValid(r - dr, c - dc) && b[r - dr][c - dc] == EMPTY;
                    boolean openEnd = isValid(tr, tc) && b[tr][tc] == EMPTY;

                    if (count >= 5) totalScore += WIN_5;
                    else if (count == 4) {
                        if (openStart && openEnd) totalScore += LIVE_4;
                        else if (openStart || openEnd) totalScore += DEAD_4;
                    } else if (count == 3) {
                        if (openStart && openEnd) totalScore += LIVE_3;
                        else if (openStart || openEnd) totalScore += DEAD_3;
                    } else if (count == 2) {
                        if (openStart && openEnd) totalScore += LIVE_2;
                    }
                    
                    // Reset visited for the next primary loop iteration 
                    // (Actually visited logic above is slightly flawed for diagonals in this structure, 
                    // but doing a full board scan is safer for simple code)
                }
            }
        }
        
        // Correct approach for line scanning without complex visited array logic:
        // Iterate every possible line of 5 cells.
        totalScore = 0;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (b[r][c] == color) {
                     // Only check forward to avoid duplicates
                     totalScore += evaluatePosition(b, r, c, dr, dc, color);
                }
            }
        }
        return totalScore;
    }

    private int evaluatePosition(int[][] b, int r, int c, int dr, int dc, int color) {
        // If previous position was same color, skip (we processed it as a block)
        if (isValid(r - dr, c - dc) && b[r - dr][c - dc] == color) return 0;

        int count = 0;
        int currR = r, currC = c;
        while(isValid(currR, currC) && b[currR][currC] == color) {
            count++;
            currR += dr;
            currC += dc;
        }

        // Check boundaries
        boolean openStart = isValid(r - dr, c - dc) && b[r - dr][c - dc] == EMPTY;
        boolean openEnd = isValid(currR, currC) && b[currR][currC] == EMPTY;

        if (count >= 5) return WIN_5;
        if (count == 4) {
            if (openStart && openEnd) return LIVE_4;
            if (openStart || openEnd) return DEAD_4;
        }
        if (count == 3) {
            if (openStart && openEnd) return LIVE_3;
            if (openStart || openEnd) return DEAD_3;
        }
        if (count == 2) {
            if (openStart && openEnd) return LIVE_2;
        }
        return 0;
    }

    private int[][] copyBoard(int[][] source) {
        int[][] dest = new int[source.length][source.length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, dest[i], 0, source[i].length);
        }
        return dest;
    }

    // Main Entry
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GobangGame::new);
    }
}