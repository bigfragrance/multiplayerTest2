package engine.math.util;



import engine.math.BlockPos;
import engine.math.Box;
import engine.math.Vec2d;
import engine.modules.EngineMain;
import org.json.JSONArray;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static engine.modules.EngineMain.cs;
import static engine.render.Screen.sc;

public class Util {
    public static Random random=new Random(System.nanoTime());
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
    public static double[] createDoubles(double def,int c){
        double[] d=new double[c];
        Arrays.fill(d,def);
        return d;
    }
    public static String getRoundedDouble(double d,int n){
        return  String.valueOf(d);
        /*int i=round(d*Math.pow(10,m));
        String s=String.valueOf(i);
        if(s.length()<m) return "0."+"0".repeat(m-s.length())+s;
        else return s.substring(0,s.length()-m)+"."+s.substring(s.length()-m);*/
    }
    public static String formatDouble(double d){
        return String.format("%."+3+"f",d);
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
    public static void renderLine(Graphics g,Vec2d start,Vec2d end){
        g.drawLine(round(start.x),round(start.y),round(end.x),round(end.y));
    }
    public static void renderString(Graphics g,String s,Vec2d centerPos,int size){
        double offX=s.length()*size/4d;
        g.setFont(new Font("微软雅黑",Font.BOLD,size));
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
    public static void render(Graphics g,boolean fill,Vec2d... points){
        int[] xPoints = new int[points.length];
        int[] yPoints = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            Vec2d point = points[i].switchToJFrame();
            xPoints[i] = round(point.x);
            yPoints[i] = round(point.y);
        }
        if(fill)g.fillPolygon(xPoints, yPoints, points.length);
        g.drawPolygon(xPoints, yPoints, points.length);
    }

    public static void renderPolygon(Graphics g,Vec2d center,int nSides,double radius,double rotation,boolean side,boolean fill){
        renderPolygon(g,center,nSides,radius,rotation,side,fill,false,1);
    }
    public static double switchXToJFrame(double x){
        return (x - sc.camX)* sc.zoom+ (double) sc.windowWidth /2;
    }
    public static double switchYToJFrame(double y){
        return -((y- sc.camY)* sc.zoom)+ (double) sc.windowHeight /2;
    }
    public static double switchXToJFrame(double x,double zoom){
        return (x - sc.camX)* zoom+ (double) sc.windowWidth /2;
    }
    public static double switchYToJFrame(double y,double zoom){
        return -((y- sc.camY)*zoom)+ (double) sc.windowHeight /2;
    }
    public static double switchXToGame(double x){
        return (x- (double) sc.windowWidth /2+ sc.camX)/ sc.zoom ;
    }
    public static double switchYToGame(double y){
        return  (-y+ sc.camY+ (double) sc.windowHeight /2)/ sc.zoom;
    }
    public static double switchXToGame(double x,double zoom){
        return (x- (double) sc.windowWidth /2+ sc.camX)/ zoom ;
    }
    public static double switchYToGame(double y,double zoom){
        return  (-y+ sc.camY+ (double) sc.windowHeight /2)/ zoom;
    }
    public static double switchXToJFrameOld(double x,double zoom){
        return x*zoom - sc.camX+ (double) sc.windowWidth /2;
    }
    public static double switchYToJFrameOld(double y,double zoom){
        return -(y*zoom-sc.camY)+ (double) sc.windowHeight /2;
    }
    public static double[] getDoubles(JSONArray array){
        double[] doubles=new double[array.length()];
        for(int i=0;i<array.length();i++){
            doubles[i]=array.getDouble(i);
        }
        return doubles;
    }
    public static Box toMiniMap(Box b){
        Vec2d center=b.getCenter();
        double sx=b.xSize()/60;
        double sy=b.ySize()/60;
        return new Box(toMiniMap(center),sx,sy);
    }
    public static Vec2d toMiniMap(Vec2d v){
        return v.multiply(1/30d).add(cs.getCamPos()).add(4,4);
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
    public static BlockPos getChunkPos(Vec2d vec){
        return new BlockPos((int) Math.floor(vec.x/ EngineMain.chunkSize), (int) Math.floor(vec.y/EngineMain.chunkSize));
    }
    public static double[] zoom(double d,Vec2d pos){
        double oz=sc.getRealZoom();
        double nz=oz+d;
        if(sc.getRealZoom()<=0){
            sc.setRealZoom(d);
            return null;
        }
        pos=pos.switchToJFrameOld(sc.getRealZoom());
        pos=pos.switchToGame(sc.getRealZoom());
        Vec2d newPos=pos.multiply(nz);
        Vec2d s=newPos.subtract(pos.multiply(oz));
        return new double[]{(sc.camX+s.x),(sc.camY+s.y),nz};
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
    public static void write(String path,String data){
        File setting=new File(path);
        if(!setting.exists()){
            try {
                setting.createNewFile();
                Files.write(setting.toPath(),data.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,   // 文件不存在时创建
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                Files.write(setting.toPath(),data.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,   // 文件不存在时创建
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static String read(String path){
        File setting=new File(path);
        String settingData=null;
        if(!setting.exists()){
            try {
                setting.createNewFile();
                settingData=Setting.create();
                Files.write(setting.toPath(),settingData.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,   // 文件不存在时创建
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            try {
                settingData=Files.readString(setting.toPath(),  StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return settingData;
    }

    public static int floor(double d) {
        return (int) Math.floor(d);
    }
}
