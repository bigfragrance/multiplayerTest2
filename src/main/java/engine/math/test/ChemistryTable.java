package engine.math.test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class ChemistryTable extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton addRowButton, addColButton, inferButton;
    private JPanel controlPanel;
    private Point dragStart;
    private boolean resizing;
    private final int resizeArea = 5;
    private Map<String, Map<String, String>> relations = new ConcurrentHashMap<>();
    private List<String> knowns = new ArrayList<>();

    public ChemistryTable() {
        setTitle("infer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
        setupListeners();
    }

    private void initUI() {
        model = new DefaultTableModel(new Object[]{"name", "known", "relationship"}, 0);
        table = new JTable(model);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(400);

        addRowButton = new JButton("add row");
        addColButton = new JButton("add column");
        inferButton = new JButton("start infer");

        controlPanel = new JPanel();
        controlPanel.add(addRowButton);
        controlPanel.add(addColButton);
        controlPanel.add(inferButton);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTableSize();
            }
        });
    }

    private void setupListeners() {
        addRowButton.addActionListener(e -> addRow());
        addColButton.addActionListener(e -> addColumn());
        inferButton.addActionListener(this::inferChemicals);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                resizing = isResizeArea(dragStart);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (resizing) {
                    updateTableSize();
                    resizing = false;
                }
            }
        });

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (resizing && dragStart != null) {
                    Point current = e.getPoint();
                    int widthChange = current.x - dragStart.x;
                    int heightChange = current.y - dragStart.y;
                    
                    if (Math.abs(widthChange) > resizeArea || Math.abs(heightChange) > resizeArea) {
                        Dimension size = table.getSize();
                        table.setSize(size.width + widthChange, size.height + heightChange);
                        dragStart = current;
                    }
                }
            }
        });
    }

    private boolean isResizeArea(Point p) {
        int w = table.getWidth();
        int h = table.getHeight();
        return p.x > w - resizeArea && p.y > h - resizeArea;
    }

    private void addRow() {
        model.addRow(new Object[]{"new substances", false, ""});
    }

    private void addColumn() {
        model.addColumn("new relationship column");
    }

    private void updateTableSize() {
        Dimension size = table.getParent().getSize();
        table.setSize(size.width - 20, size.height - 20);
    }

    private void inferChemicals(ActionEvent e) {
        relations.clear();
        knowns.clear();
        
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = model.getValueAt(i, 0).toString();
            boolean isKnown = Boolean.parseBoolean(model.getValueAt(i, 1).toString());
            if (isKnown) knowns.add(name);
            
            Map<String, String> relMap = new  ConcurrentHashMap<>();
            for (int j = 2; j < model.getColumnCount(); j++) {
                String colName = model.getColumnName(j);
                String value = model.getValueAt(i, j) != null ? model.getValueAt(i, j).toString() : "";
                relMap.put(colName, value);
            }
            relations.put(name, relMap);
        }
        
        List<String> results = performInference();
        JOptionPane.showMessageDialog(this, 
            "result:\n" + String.join("\n", results),
            "done", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<String> performInference() {
        Map<String, String> chemicals = new  ConcurrentHashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        for (String known : knowns) {
            chemicals.put(known, "已知");
            queue.add(known);
        }
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            Map<String, String> rels = relations.get(current);
            
            for (Map.Entry<String, String> entry : rels.entrySet()) {
                String target = entry.getKey();
                String relation = entry.getValue();
                
                if (relation.contains("->") && !chemicals.containsKey(target)) {
                    chemicals.put(target, "来自: " + current);
                    queue.add(target);
                }
            }
        }
        
        List<String> result = new ArrayList<>();
        for (String chem : relations.keySet()) {
            String status = chemicals.getOrDefault(chem, "未知");
            result.add(chem + ": " + status);
        }
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChemistryTable().setVisible(true));
    }
}