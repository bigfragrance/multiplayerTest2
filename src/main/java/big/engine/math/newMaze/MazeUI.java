package big.engine.math.newMaze;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

class MazeGenerator {
    private int width, height;
    private int[][] maze;
    private Random random = new Random();
    private boolean stepByStep;
    private MazePanel panel;

    public MazeGenerator(int width, int height, boolean stepByStep, MazePanel panel) {
        this.width = width;
        this.height = height;
        this.stepByStep = stepByStep;
        this.panel = panel;
        this.maze = new int[height][width];
    }

    public int[][] getMaze() {
        return maze;
    }

    public void generate() {
        // 1. Initialize with walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                maze[y][x] = 1; // wall
            }
        }

        // 2. Dig tunnels
        int tunnels = (width * height) / 2;
        int x = random.nextInt(width);
        int y = random.nextInt(height);

        for (int i = 0; i < tunnels; i++) {
            maze[y][x] = 0; // empty

            // Random direction with slight persistence
            int dir = random.nextInt(4);
            if (random.nextBoolean()) dir = i % 4; // keep some direction

            switch (dir) {
                case 0: if (x > 1) x--; break;
                case 1: if (x < width - 2) x++; break;
                case 2: if (y > 1) y--; break;
                case 3: if (y < height - 2) y++; break;
            }

            maze[y][x] = 0;

            if (stepByStep) {
                panel.repaint();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {}
            }
        }

        // 3. Add central plaza (9x9)
        int plazaSize = 9;
        int px = width / 2 - plazaSize / 2;
        int py = height / 2 - plazaSize / 2;
        for (int yy = py; yy < py + plazaSize; yy++) {
            for (int xx = px; xx < px + plazaSize; xx++) {
                if (yy >= 0 && yy < height && xx >= 0 && xx < width) {
                    maze[yy][xx] = 0;
                }
            }
        }

        panel.repaint();
    }
}

class MazePanel extends JPanel {
    MazeGenerator generator;

    public MazePanel(MazeGenerator generator) {
        this.generator = generator;
        setPreferredSize(new Dimension(600, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int[][] maze = generator.getMaze();
        int h = maze.length;
        int w = maze[0].length;
        int cellSize = Math.min(getWidth() / w, getHeight() / h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (maze[y][x] == 1) {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
                }
            }
        }
    }
}

public class MazeUI extends JFrame {
    private MazePanel panel;
    private MazeGenerator generator;
    private JButton generateButton;
    private JCheckBox stepByStepCheck;

    public MazeUI(int width, int height) {
        setTitle("Maze Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        generator = new MazeGenerator(width, height, false, null);
        panel = new MazePanel(generator);
        generator = new MazeGenerator(width, height, false, panel);
        panel = new MazePanel(generator);

        add(panel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        stepByStepCheck = new JCheckBox("Step by step");
        generateButton = new JButton("Generate");

        controlPanel.add(stepByStepCheck);
        controlPanel.add(generateButton);
        add(controlPanel, BorderLayout.SOUTH);

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generator = new MazeGenerator(width, height, stepByStepCheck.isSelected(), panel);
                panel.generator = generator;

                new Thread(() -> generator.generate()).start();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeUI(41, 41));
    }
}
