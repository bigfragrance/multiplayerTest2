package engine.render;


import engine.math.Box;
import engine.math.Vec2d;
import engine.modules.EngineMain;
import modules.ctrl.InputManager;
import modules.entity.Entity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static engine.math.util.Util.round;
import static engine.modules.EngineMain.cs;


public class Screen extends JPanel implements Runnable,ActionListener, KeyListener{
    public static Screen INSTANCE;
    public static JFrame frame;
    public volatile double camX=0;
    public volatile double camY=0;
    public volatile double zoom=4;
    public int windowWidth=1000;
    public int windowHeight=1000;
    public static int mouseX = 50;
    public static int mouseY = 50;
    public static volatile Vec2d mousePos=new Vec2d(50,50);
    public static volatile HashMap<Character,Boolean> keyPressed=new HashMap<>();
    public static volatile HashMap<Character,Boolean> lastKeyPressed=new HashMap<>();
    public static char MOUSECHAR=(char)60000;
    public static double tickDelta=0;
    public InputManager inputManager=null;
    public static Box SCREEN_BOX=new Box(0, 800,0,800);
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
            SCREEN_BOX=new Box(0, Screen.INSTANCE.windowWidth,0,Screen.INSTANCE.windowHeight);
            long start=System.currentTimeMillis();
            windowWidth=frame.getWidth();
            windowHeight=frame.getHeight();
            tickDelta= EngineMain.getTickDelta();
            Vec2d camPos=cs.getCamPos();
            camX=camPos.x;
            camY=camPos.y;
            repaint();
            try {
                long s= -(System.currentTimeMillis() - start) +1000 / 60;
                if(s>0)Thread.sleep(s); // 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void paintComponent(Graphics g){
        try {

            super.paintComponent(g);
            for(Entity entity:cs.entities.values()){
                entity.render(g);
            }
        }
        catch (Exception e){
            //e.printStackTrace();
        }
    }

}
