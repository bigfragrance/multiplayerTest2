package big.engine.math.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

public class ChemistryInference extends JFrame {
    private GraphPanel graphPanel;
    private JButton solveButton;

    public ChemistryInference() {
        setTitle("Chemistry Inference");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.CENTER);

        JToolBar toolBar = new JToolBar();
        solveButton = new JButton("Solve");
        toolBar.add(solveButton);
        add(toolBar, BorderLayout.NORTH);

        solveButton.addActionListener(e -> graphPanel.solveInference());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChemistryInference().setVisible(true));
    }
}

class GraphPanel extends JPanel {
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private Node selectedNode;
    private Node dragNode;
    private Point dragStart;
    private int nodeRadius = 20;
    private String currentMode = "NODE";

    public GraphPanel() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Edge edge : edges) {
            edge.draw(g2d);
        }

        for (Node node : nodes) {
            node.draw(g2d);
        }
    }

    public void solveInference() {
        Set<Node> knownNodes = new HashSet<>();
        for (Node node : nodes) {
            if (!node.label.equals("?")) {
                knownNodes.add(node);
            }
        }

        boolean changed;
        do {
            changed = false;
            for (Edge edge : edges) {
                if (edge.type == EdgeType.CONVERSION) {
                    if (knownNodes.contains(edge.start) && !knownNodes.contains(edge.end)) {
                        edge.end.label = "Inferred";
                        knownNodes.add(edge.end);
                        changed = true;
                    }
                    if (edge.bidirectional && knownNodes.contains(edge.end) && !knownNodes.contains(edge.start)) {
                        edge.start.label = "Inferred";
                        knownNodes.add(edge.start);
                        changed = true;
                    }
                }
            }
        } while (changed);

        repaint();
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            for (Node node : nodes) {
                if (node.contains(p)) {
                    dragNode = node;
                    dragStart = node.position;
                    return;
                }
            }
            
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (currentMode.equals("NODE")) {
                    nodes.add(new Node(p, "?"));
                } else {
                    for (Node node : nodes) {
                        if (node.contains(p)) {
                            if (selectedNode == null) {
                                selectedNode = node;
                            } else if (selectedNode != node) {
                                edges.add(new Edge(selectedNode, node, 
                                        currentMode.equals("REACTION") ? EdgeType.REACTION : EdgeType.CONVERSION,
                                        currentMode.equals("BIDIRECTIONAL")));
                                selectedNode = null;
                            }
                        }
                    }
                }
            } else if (SwingUtilities.isRightMouseButton(e)) {
                for (Node node : new ArrayList<>(nodes)) {
                    if (node.contains(p)) {
                        String newLabel = JOptionPane.showInputDialog("Enter label:");
                        if (newLabel != null && !newLabel.trim().isEmpty()) {
                            node.label = newLabel.trim();
                        }
                        repaint();
                        return;
                    }
                }
                
                for (Edge edge : new ArrayList<>(edges)) {
                    if (edge.isNear(p)) {
                        edges.remove(edge);
                        repaint();
                        return;
                    }
                }
            }
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragNode = null;
        }
    }

    private class MouseMotionHandler extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragNode != null) {
                dragNode.position = e.getPoint();
                repaint();
            }
        }
    }

    private class Node {
        Point position;
        String label;

        Node(Point position, String label) {
            this.position = position;
            this.label = label;
        }

        boolean contains(Point p) {
            return position.distance(p) <= nodeRadius;
        }

        void draw(Graphics2D g) {
            g.setColor(label.equals("?") ? Color.LIGHT_GRAY : Color.CYAN);
            g.fillOval(position.x - nodeRadius, position.y - nodeRadius, 
                      nodeRadius * 2, nodeRadius * 2);
            
            g.setColor(Color.BLACK);
            g.drawOval(position.x - nodeRadius, position.y - nodeRadius, 
                      nodeRadius * 2, nodeRadius * 2);
            
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getHeight();
            g.drawString(label, position.x - textWidth/2, position.y + textHeight/4);
        }
    }

    private enum EdgeType { REACTION, CONVERSION }

    private class Edge {
        Node start;
        Node end;
        EdgeType type;
        boolean bidirectional;

        Edge(Node start, Node end, EdgeType type, boolean bidirectional) {
            this.start = start;
            this.end = end;
            this.type = type;
            this.bidirectional = bidirectional;
        }

        void draw(Graphics2D g) {
            if (type == EdgeType.REACTION) {
                g.setColor(Color.RED);
                g.drawLine(start.position.x, start.position.y, end.position.x, end.position.y);
            } else {
                g.setColor(Color.BLUE);
                drawArrow(g, start.position, end.position);
                if (bidirectional) {
                    drawArrow(g, end.position, start.position);
                }
            }
        }

        private void drawArrow(Graphics2D g, Point from, Point to) {
            g.drawLine(from.x, from.y, to.x, to.y);
            
            double angle = Math.atan2(to.y - from.y, to.x - from.x);
            int arrowSize = 10;
            Point arrow1 = new Point(
                (int) (to.x - arrowSize * Math.cos(angle - Math.PI/6)),
                (int) (to.y - arrowSize * Math.sin(angle - Math.PI/6))
            );
            Point arrow2 = new Point(
                (int) (to.x - arrowSize * Math.cos(angle + Math.PI/6)),
                (int) (to.y - arrowSize * Math.sin(angle + Math.PI/6))
            );
            
            g.drawLine(to.x, to.y, arrow1.x, arrow1.y);
            g.drawLine(to.x, to.y, arrow2.x, arrow2.y);
        }

        boolean isNear(Point p) {
            int tolerance = 5;
            double distance = Line2D.ptSegDist(
                start.position.x, start.position.y,
                end.position.x, end.position.y,
                p.x, p.y
            );
            return distance < tolerance;
        }
    }
}