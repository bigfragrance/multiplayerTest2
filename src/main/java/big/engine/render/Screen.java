package big.engine.render;


import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.util.AfterCheckTask;
import big.engine.math.util.Util;
import big.engine.math.util.timer.AutoList;
import big.engine.modules.EngineMain;
import big.events.RenderEvent;
import big.modules.ctrl.InputManager;
import big.modules.entity.Entity;
import big.modules.entity.bullet.BulletEntity;
import big.modules.particle.Particle;
import big.modules.screen.GUI;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static big.engine.math.util.Util.round;
import static big.engine.modules.EngineMain.TPS;
import static big.engine.modules.EngineMain.cs;


public class Screen extends JPanel implements Runnable,ActionListener, KeyListener{
    public static Screen sc;
    public static JFrame frame;
    public volatile double camX=0;
    public volatile double camY=0;
    public static double defZoom=12.8/0.02;
    public volatile double zoom=defZoom;
    public volatile double zoom2=0.125;
    private double oldZoom=defZoom;
    public double lineWidth=1;
    public int windowWidth=1000;
    public int windowHeight=1000;
    public static int mouseX = 50;
    public static int mouseY = 50;
    public static volatile Vec2d mousePos=new Vec2d(50,50);
    public static volatile  ConcurrentHashMap<Character,Boolean> keyPressed=new  ConcurrentHashMap<>();
    public static volatile ConcurrentHashMap<Character,Boolean> lastKeyPressed=new  ConcurrentHashMap<>();
    public static char MOUSECHAR=(char)60000;
    public static double tickDeltaAdd=0;
    public static double tickDelta=0;
    public long lastRender=0;
    public InputManager inputManager=null;
    public static Box SCREEN_BOX=new Box(0, 800,0,800);
    public ArrayList<AfterCheckTask<Graphics>> renderTasks=new ArrayList<>();
    public AutoList<AfterCheckTask<Graphics>> renderTasks2=new AutoList<>();
    public String renderString="";
    public GUI currentScreen=null;
    public Screen(){
        sc =this;
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
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if(cs.isServer) {
                    double[] d = Util.zoom(-e.getPreciseWheelRotation() * getRealZoom() / 10, mousePos.switchToGame());
                    if (d != null) {
                        cs.camPos=new Vec2d(d[0],d[1]);
                        cs.prevCamPos=cs.camPos;
                        setRealZoom(d[2]);
                    }
                }
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
    public static boolean isKeyClicked(char c){
        return isKeyPressed(c)&&!isKeyLastPressed(c);
    }
    public void run(){
        setUIFont(new Font("微软雅黑", Font.PLAIN, 14));
        while (true) {
            try {
                SCREEN_BOX=new Box(0, Screen.sc.windowWidth,0,Screen.sc.windowHeight);
                if(cs.ticking&&!cs.isServer) continue;
                long start=System.currentTimeMillis();
                windowWidth=frame.getWidth();
                windowHeight=frame.getHeight();

                Vec2d camPos=cs.getCamPos();
                camX=camPos.x;
                camY=camPos.y;
                //if(System.currentTimeMillis()-EngineMain.lastTick>1000/TPS||System.currentTimeMillis()-EngineMain.lastTick<4) continue;
                tickDeltaAdd=(System.currentTimeMillis()-lastRender)/1000.0d*TPS;
                tickDelta=EngineMain.getTickDelta();
                //cs.fastUpdate(tickDeltaAdd);
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
    private static void setUIFont(Font font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(font));
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

            g2d.setStroke(new BasicStroke((float) (lineWidth/zoom2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2d.translate(centerX, centerY);
            g2d.scale(zoom2,zoom2);
            g2d.translate(-centerX, -centerY);

            for(double i=0;i<40;i++){
                Box b=cs.borderBox.expand(i*i,i*i);
                g.setColor(Color.GRAY);
                Util.renderCubeLine(g,b.switchToJFrame());
            }
            storeAndSetDef();
            Util.renderCubeLine(g,Util.toMiniMap(cs.borderBox).switchToJFrame());
            restoreZoom();
            cs.world.renderBackground(g);

            for(Particle particle:(ArrayList<Particle>)cs.particles.clone()){
                particle.render(g);
            }
            for(Entity particle:(ArrayList<Entity>)cs.entityParticles.clone()){
                particle.render(g);
            }

            ArrayList<Entity> entities=new ArrayList<>(cs.entities.values());
            for(Entity entity:entities){
                if(entity instanceof BulletEntity) {
                    entity.render(g);
                }
            }
            for(Entity entity:entities){
                if(!(entity instanceof BulletEntity)) {
                    entity.render(g);
                }
            }
            cs.world.render(g);
            /*if(!cs.isServer) {
                g2d = (Graphics2D) g.create();

                Point2D center = new Point2D.Float(getWidth() / 2f, getHeight() / 2f);
                float radius = Math.min(getWidth(), getHeight()) * 0.6f;
                float[] dist = {0.0f, 0.7f, 1.0f};
                Color[] colors = {
                        new Color(200, 200, 200, 0),
                        new Color(200, 200, 200, 0),
                        new Color(200, 200, 200, 255)
                };

                RadialGradientPaint paint = new RadialGradientPaint(
                        center, radius, dist, colors,
                        MultipleGradientPaint.CycleMethod.NO_CYCLE
                );

                g2d.setPaint(paint);
                g2d.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
                g2d.dispose();
            }*/
            for(int i=renderTasks.size()-1;i>=0;i--){
                renderTasks.get(i).run(g);
            }
            Set<AfterCheckTask<Graphics>> taskSet=renderTasks2.getSet();
            for(AfterCheckTask<Graphics> task:taskSet){
                task.run(g);
            }
            renderTasks2.update(50);
            renderTasks.clear();
            g.setColor(Color.black);
            Util.renderString(g,renderString,Screen.SCREEN_BOX.getMinPos().add(100,50).subtract(sc.getMiddle()).multiply(1/sc.zoom2).add(sc.getMiddle()),round(10/zoom2));

            if(currentScreen!=null){
                currentScreen.render(g);
            }
            cs.EVENT_BUS.post(RenderEvent.get(g));
        }
        catch (Exception e){
            e.printStackTrace();
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
            Set<AfterCheckTask<Graphics>> taskSet=renderTasks2.getSet();
            for(AfterCheckTask<Graphics> task:taskSet){
                task.run(g);
            }
            renderTasks2.update(50);
            cs.world.render(g);
            //e.printStackTrace();
        }
    }
    public void renderAtLast(AfterCheckTask<Graphics> task){
        renderTasks.add(task);
    }
    public double getRealZoom(){
        return zoom*zoom2;
    }
    public void setRealZoom(double zoom){
        this.zoom=zoom/zoom2;
    }
    public double getZoom(){
        return zoom;
    }
    public double getZoom2(){
        return zoom2;
    }
    public Vec2d getMiddle(){
        return new Vec2d((double) windowWidth /2, (double) windowHeight /2);
    }
    public void tick(){
        if(currentScreen!=null)currentScreen.tick();
    }
    public void setScreen(GUI gui){
        currentScreen=gui;
    }
    public void closeScreen(){
        currentScreen=null;
    }
    public void storeAndSetDef(){
        oldZoom=zoom;
        zoom=defZoom;
    }
    public void restoreZoom(){
        zoom=oldZoom;
    }
}
