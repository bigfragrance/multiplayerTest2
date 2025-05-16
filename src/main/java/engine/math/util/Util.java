package engine.math.util;



import engine.math.Box;
import engine.math.Vec2d;
import engine.render.Screen;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Util {
    public static Random random=new Random(114514);
    public static double sin(double d){
        return Math.sin(Math.toRadians(d));
    }
    public static double cos(double d){
        return Math.cos(Math.toRadians(d));
    }

    public static int round(double d){
        return (int)Math.round(d);
    }
    public static double round(double d,int m){
        return (double) Math.round(d * m) /m;
    }
    public static boolean withIn(double min,double max,double d,boolean leftInclusive,boolean rightInclusive){
        return (leftInclusive?d>=min:d>min)&&(rightInclusive?d<=max:d<max);
    }
    public static String getRoundedDouble(double d,int m){
        int i=round(d*Math.pow(10,m));
        String s=String.valueOf(i);
        if(s.length()<m) return "0."+"0".repeat(m-s.length())+s;
        else return s.substring(0,s.length()-m)+"."+s.substring(s.length()-m);
    }

    public static void render( Graphics g,double mx, double my, double xs, double ys){
        g.fillOval(round(mx),round(my),round(xs),round(ys));
    }
    public static void render(Graphics g, Box b){
        render(g,b.minX,b.minY,b.xSize(),b.ySize());
    }
    public static void render(Graphics g, Box b,double i){
        render(g,b.minX+i,b.minY+i,b.xSize()-2*i,b.ySize()-2*i);
    }
    public static void renderCube( Graphics g,double mx, double my, double xs, double ys){
        g.fillRect(round(mx),round(my),round(xs),round(ys));
    }
    public static void renderCube(Graphics g, Box b){
        renderCube(g,b.minX,b.minY,b.xSize(),b.ySize());
    }
    public static void renderCube(Graphics g, Box b,double i){
        renderCube(g,b.minX+i,b.minY+i,b.xSize()-2*i,b.ySize()-2*i);
    }
    public static void renderCubeLine(Graphics g,Box b){
        g.drawLine((int) b.minX, (int) b.minY, (int) b.maxX, (int) b.minY);
        g.drawLine((int) b.minX, (int) b.minY, (int) b.minX, (int) b.maxY);
        g.drawLine((int) b.maxX, (int) b.maxY, (int) b.minX, (int) b.maxY);
        g.drawLine((int) b.maxX, (int) b.maxY, (int) b.maxX, (int) b.minY);
    }
    public static void renderString(Graphics g,String s,Vec2d centerPos,int size){
        double offX=s.length()*size/4d;
        g.setFont(new Font("Arial",Font.BOLD,size));
        g.drawString(s,round(centerPos.x-offX),round(centerPos.y+ (double) size /2));
    }
    public static void renderPolygon(Graphics g,Vec2d center,int nSides,double radius,double rotation,boolean side,boolean fill,boolean sharp,double sharpFactor){
        if(sharp) {
            nSides *= 2;
            int[] xPoints = new int[nSides];
            int[] yPoints = new int[nSides];
            double angleIncrement = 360d / nSides;
            boolean isSharp=false;
            for (int i = 0; i < nSides; i++) {
                double angle = i * angleIncrement + rotation;
                double r=isSharp?radius*sharpFactor:radius;
                Vec2d point = center.add(new Vec2d(cos(angle) * r, sin(angle) * r)).switchToJFrame();
                xPoints[i] = round(point.x);
                yPoints[i] = round(point.y);
                isSharp=!isSharp;
            }
            if(fill)g.fillPolygon(xPoints, yPoints, nSides);
            if(side) g.drawPolygon(xPoints, yPoints, nSides);
        }else{
            int[] xPoints = new int[nSides];
            int[] yPoints = new int[nSides];
            double angleIncrement = 360d / nSides;
            for (int i = 0; i < nSides; i++) {
                double angle = i * angleIncrement + rotation;
                Vec2d point = center.add(new Vec2d(cos(angle) * radius, sin(angle) * radius)).switchToJFrame();
                xPoints[i] = round(point.x);
                yPoints[i] = round(point.y);
            }
            if(fill)g.fillPolygon(xPoints, yPoints, nSides);
            if(side) g.drawPolygon(xPoints, yPoints, nSides);
        }
    }
    public static void renderPolygon(Graphics g,Vec2d center,int nSides,double radius,double rotation,boolean side,boolean fill){
        renderPolygon(g,center,nSides,radius,rotation,side,fill,false,1);
    }
    public static double switchXToJFrame(double x){
        return (x - Screen.INSTANCE.camX)* Screen.INSTANCE.zoom+ (double) Screen.INSTANCE.windowWidth /2;
    }
    public static double switchYToJFrame(double y){
        return -((y-Screen.INSTANCE.camY)*Screen.INSTANCE.zoom)+ (double) Screen.INSTANCE.windowHeight /2;
    }
    public static double switchXToGame(double x){
        return (x- (double) Screen.INSTANCE.windowWidth /2+Screen.INSTANCE.camX)/Screen.INSTANCE.zoom ;
    }
    public static double switchYToGame(double y){
        return  (-y+Screen.INSTANCE.camY+ (double) Screen.INSTANCE.windowHeight /2)/Screen.INSTANCE.zoom;
    }
    public static double[] zoom(double d,Vec2d pos){
        double oz=Screen.INSTANCE.zoom;
        double nz=oz+d;
        if(Screen.INSTANCE.zoom<=0){
            Screen.INSTANCE.zoom=d;
            return null;
        }
        Vec2d newPos=pos.multiply(nz);
        Vec2d s=newPos.subtract(pos.multiply(oz));
        return new double[]{(Screen.INSTANCE.camX+s.x),(Screen.INSTANCE.camY+s.y),nz};
    }
    public static double random(double min,double max){
        return min+random.nextDouble()*(max-min);
    }
    public static Vec2d randomInBox(Box box){
        return new Vec2d(random(box.minX,box.maxX),random(box.minY,box.maxY));
    }
    public static Vec2d randomVec(){
        return new Vec2d(random(-1,1),random(-1,1));
    }
    public static double lerp(double start,double end,double t){
        return start*(1-t)+end*t;
    }
    public static Vec2d lerp(Vec2d v1,Vec2d v2,double t){
        return v1.multiply(1-t).add(v2.multiply(t));
    }
    public static Box lerp(Box b1,Box b2,double t){
        return new Box(lerp(b1.getMinPos(),b2.getMinPos(),t),lerp(b1.getMaxPos(),b2.getMaxPos(),t));
    }
    public static Vec2d getVec2dFromString(String str){
        try {
            List<String> strings = splitStringBySpace(str);
            double d1 = Double.parseDouble(strings.get(0));
            double d2 = Double.parseDouble(strings.get(1));
            return new Vec2d(d1,d2);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static String getStringFromVec2d(Vec2d vec){
        StringBuilder sb=new StringBuilder();
        sb.append(vec.x+"-");
        sb.append(vec.y);
        return sb.toString();
    }
    public static List<String> splitStringBySpace(String input) {
        if (input == null || input.isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(input.split("\\s+"));
    }
}
