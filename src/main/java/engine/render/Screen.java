package engine.render;


import engine.math.Box;
import engine.math.Vec2d;
import engine.math.util.AfterCheckTask;
import engine.math.util.Util;
import engine.modules.EngineMain;
import modules.ctrl.InputManager;
import modules.entity.Entity;
import modules.entity.bullet.EntityParticle;
import modules.particle.Particle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

import static engine.math.util.Util.round;
import static engine.modules.EngineMain.TPS;
import static engine.modules.EngineMain.cs;


public class Screen extends JPanel implements Runnable,ActionListener, KeyListener{
    public static Screen INSTANCE;
    public static JFrame frame;
    public volatile double camX=0;
    public volatile double camY=0;
    public volatile double zoom=1.6;
    public int windowWidth=1000;
    public int windowHeight=1000;
    public static int mouseX = 50;
    public static int mouseY = 50;
    public static volatile Vec2d mousePos=new Vec2d(50,50);
    public static volatile HashMap<Character,Boolean> keyPressed=new HashMap<>();
    public static volatile HashMap<Character,Boolean> lastKeyPressed=new HashMap<>();
    public static char MOUSECHAR=(char)60000;
    public static double tickDeltaAdd=0;
    public static double tickDelta=0;
    private static long lastRender=0;
    public InputManager inputManager=null;
    public static Box SCREEN_BOX=new Box(0, 800,0,800);
    private ArrayList<AfterCheckTask<Graphics>> renderTasks=new ArrayList<>();
    public Screen(){
        INSTANCE=this;
        frame.addKeyListener(this);
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e){

            }
            @Override
            public void mousePressed(MouseEvent e) {
                keyPressed.put(MOUSECHAR,true);
                //use this
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                keyPressed.put(MOUSECHAR,false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = round((e.getX()));
                mouseY = round((e.getY()));
                mousePos.set(mouseX,mouseY);
            }

        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                mouseX = round((e.getX()));
                mouseY = round((e.getY()));
                mousePos.set(mouseX,mouseY);
            }
        });
        this.inputManager=new InputManager();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyPressed.put(e.getKeyChar(),true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyPressed.put(e.getKeyChar(),false);
    }
    public static boolean isKeyPressed(char c){
        return keyPressed.getOrDefault(c,false);
    }
    public static boolean isKeyLastPressed(char c){
        return lastKeyPressed.getOrDefault(c,false);
    }
    public static boolean isKeyClinked(char c){
        return isKeyPressed(c)&&!isKeyLastPressed(c);
    }
    public void run(){
        while (true) {
            try {
                SCREEN_BOX=new Box(0, Screen.INSTANCE.windowWidth,0,Screen.INSTANCE.windowHeight);
                long start=System.currentTimeMillis();
                windowWidth=frame.getWidth();
                windowHeight=frame.getHeight();

                Vec2d camPos=cs.getCamPos();
                camX=camPos.x;
                camY=camPos.y;
                //if(System.currentTimeMillis()-EngineMain.lastTick>1000/TPS||System.currentTimeMillis()-EngineMain.lastTick<4) continue;
                tickDeltaAdd=(System.currentTimeMillis()-lastRender)/1000.0d*TPS;
                tickDelta=EngineMain.getTickDelta();
                cs.fastUpdate(tickDeltaAdd);
                try {
                    repaint();
                }catch ( Exception e){
                    e.printStackTrace();
                }
                lastRender=System.currentTimeMillis();
                long s= -(System.currentTimeMillis() - start) +1000/60;
                if(s>0)Thread.sleep(s); // 60 FPS
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void paintComponent(Graphics g){
        try {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,  RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,  RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            for(double i=0;i<40;i++){
                Box b=cs.borderBox.expand(i*i,i*i);
                g.setColor(Color.GRAY);
                Util.renderCubeLine(g,b.switchToJFrame());
            }
            ArrayList<Entity> entities=new ArrayList<>(cs.entities.values());
            for(Entity entity:entities){
                entity.render(g);
            }
            for(Particle particle:(ArrayList<Particle>)cs.particles.clone()){
                particle.render(g);
            }
            for(Entity particle:(ArrayList<Entity>)cs.entityParticles.clone()){
                particle.render(g);
            }
            for(int i=renderTasks.size()-1;i>=0;i--){
                renderTasks.get(i).run(g);
            }
            renderTasks.clear();
        }
        catch (Exception e){
            super.paintComponent(g);
            ArrayList<Entity> entities= (ArrayList<Entity>) new ArrayList<>(cs.entities.values().stream().toList()).clone();
            for(Entity entity:entities){
                entity.render(g);
            }
            ArrayList<Particle> particles=new ArrayList<>((ArrayList<Particle>)cs.particles.clone());
            for(Particle particle:particles){
                particle.render(g);
            }
            ArrayList<Entity> bulletParticles=new ArrayList<>((ArrayList<Entity>)cs.entityParticles.clone());
            for(Entity particle:bulletParticles){
                particle.render(g);
            }
            for(double i=0;i<40;i++){
                Box b=cs.borderBox.expand(i*i,i*i);
                g.setColor(Color.GRAY);
                Util.renderCubeLine(g,b.switchToJFrame());
            }
            //e.printStackTrace();
        }
    }
    public void renderAtLast(AfterCheckTask<Graphics> task){
        renderTasks.add(task);
    }
}
