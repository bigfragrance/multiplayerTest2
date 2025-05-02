package modules.client;

import com.formdev.flatlaf.FlatDarkLaf;
import engine.math.Vec2d;

import javax.swing.*; 
import java.awt.*; 
import java.awt.event.*; 
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ClientGUI extends JFrame {
    private static final int CELL_SIZE = 16;
    private ClientNetwork network;
    private Color selectedColor = Color.WHITE;
    private CanvasPanel canvasPanel;
    private Vec2d lastMousePos;
 
    public ClientGUI(String serverIP,int port) {
        setupUI();
        setupNetwork(serverIP,port);
        setupInput();
        startRenderThread();
    }
 
    private void setupUI() {
        FlatDarkLaf.setup(); 
        setTitle("Pixel Canvas v2025.05");
        setSize(64*CELL_SIZE + 280, 64*CELL_SIZE + 40);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        canvasPanel = new CanvasPanel();
        add(createSidebar(), BorderLayout.EAST);
        add(new JScrollPane(canvasPanel), BorderLayout.CENTER);
        
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.add(new  JLabel("Position: (0,0)"));
        add(statusBar, BorderLayout.SOUTH);
    }
 
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new  Dimension(250, 0));
        
        JPanel colorGrid = new JPanel(new GridLayout(0, 4, 5, 5));
        Color[] colors = {
            new Color(0xFF3B30), new Color(0x34C759), new Color(0x007AFF),
            new Color(0xFF9500), new Color(0xAF52DE), new Color(0xFF2D55),
            new Color(0x5AC8FA), new Color(0x5856D6), Color.WHITE, Color.BLACK 
        };
        
        for (Color c : colors) {
            JButton btn = new JButton();
            btn.setBackground(c); 
            btn.addActionListener(e  -> selectedColor = c);
            btn.setToolTipText(String.format("#%06X",  c.getRGB()  & 0xFFFFFF));
            colorGrid.add(btn); 
        }
        
        JToolBar tools = new JToolBar();
        tools.add(new  JButton("Eraser"));
        tools.addSeparator(); 
        tools.add(new  JButton("Magnifier"));
        
        sidebar.add(tools,  BorderLayout.NORTH);
        sidebar.add(colorGrid,  BorderLayout.CENTER);
        return sidebar;
    }
 
    private class CanvasPanel extends JPanel {
        private BufferedImage buffer;
        private BasicStroke gridStroke = new BasicStroke(0.8f);
 
        @Override 
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 
            Graphics2D g2 = (Graphics2D) g.create(); 
            
            if (buffer == null || 
                buffer.getWidth()  != getWidth() || 
                buffer.getHeight()  != getHeight()) {
                buffer = new BufferedImage(getWidth(), getHeight(), 
                    BufferedImage.TYPE_INT_ARGB);
            }
            
            Graphics2D bufferG = buffer.createGraphics(); 
            renderCanvas(bufferG);
            g2.drawImage(buffer,  0, 0, null);
            bufferG.dispose(); 
            
            if (lastMousePos != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,  0.5f));
                g2.setColor(selectedColor); 
                g2.fillRect((int) (lastMousePos.x*CELL_SIZE), (int) (lastMousePos.y*CELL_SIZE),
                    CELL_SIZE, CELL_SIZE);
            }
            g2.dispose(); 
        }
 
        private void renderCanvas(Graphics2D g) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  
                RenderingHints.VALUE_ANTIALIAS_ON);
                
            g.setColor(new  Color(100, 100, 100, 80));
            g.setStroke(gridStroke); 
            for (int x = 0; x <= 64; x++) {
                g.drawLine(x*CELL_SIZE,  0, x*CELL_SIZE, 64*CELL_SIZE);
            }
            for (int y = 0; y <= 64; y++) {
                g.drawLine(0,  y*CELL_SIZE, 64*CELL_SIZE, y*CELL_SIZE);
            }
 
            network.canvasLock.readLock().lock(); 
            try {
                for (int x = 0; x < 64; x++) {
                    for (int y = 0; y < 64; y++) {
                        Color c = network.getPixel(x,  y);
                        if (c != null) {
                            g.setColor(c); 
                            g.fillRect(x*CELL_SIZE+1,  y*CELL_SIZE+1, 
                                CELL_SIZE-2, CELL_SIZE-2);
                        }
                    }
                }
            } finally {
                network.canvasLock.readLock().unlock(); 
            }
        }
    }
 
    private void setupNetwork(String serverIP,int port) {
        /*try {
            network = new ClientNetwork();
            network.connect(serverIP,port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,  "Server connection failed", 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1); 
        }*/
    }
 
    private void setupInput() {
        canvasPanel.addMouseMotionListener(new  MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if(lastMousePos!=null){
                    for(double d=0;d<=1;d+=0.1){
                        int x=(int)(lastMousePos.x+((double)e.getX()/CELL_SIZE-lastMousePos.x)*d);
                        int y=(int)(lastMousePos.y+((double)e.getY()/CELL_SIZE-lastMousePos.y)*d);
                        if(x>=0&&x<64&&y>=0&&y<64){
                            network.sendPixelUpdate(x,y,selectedColor);
                        }
                    }
                }
                int x = e.getX()  / CELL_SIZE;
                int y = e.getY()  / CELL_SIZE;
                if (x >= 0 && x < 64 && y >= 0 && y < 64) {
                    network.sendPixelUpdate(x,  y, selectedColor);
                }
                lastMousePos = new Vec2d(
                        (double) e.getX() / CELL_SIZE,
                        (double) e.getY() / CELL_SIZE
                );
            }
            
            public void mouseMoved(MouseEvent e) {
                lastMousePos = new Vec2d(
                    (double) e.getX() / CELL_SIZE,
                    (double) e.getY() / CELL_SIZE
                );
                canvasPanel.repaint();
            }
        });
        canvasPanel.addMouseListener(new  MouseAdapter() {
            public void mousePressed(MouseEvent e) {

            }
            public void mouseReleased(MouseEvent e) {
                lastMousePos = null;
            }
        });
    }
 
    private void startRenderThread() {
        new Timer(16, e -> {

            canvasPanel.repaint();
        }).start();
    }
}