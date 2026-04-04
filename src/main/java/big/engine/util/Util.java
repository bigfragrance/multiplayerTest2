package big.engine.util;



import big.engine.math.Direction;
import big.engine.math.Vec2i;
import big.engine.math.Box;
import big.engine.math.Vec2d;
import big.engine.math.interfaces.FInt2Int;
import big.engine.modules.EngineMain;

import big.game.world.World;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.function.Predicate;

import static big.engine.modules.EngineMain.cs;
import static big.engine.render.Screen.sc;

public class Util {
    public static Random random=new Random(System.nanoTime());
    public static double[] sinTable=createDoubles(360*100,i->Math.sin(Math.toRadians(i*360.0/100)));

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
    public static double log(double d,double base){
        return Math.log(d)/Math.log(base);
    }
    public static double[] createDoubles(double def,int c){
        double[] d=new double[c];
        Arrays.fill(d,def);
        return d;
    }
    public static double[] createDoubles(int len, IntToDoubleFunction f){
        double[] d=new double[len];
        for(int i=0;i<len;i++){
            d[i]=f.applyAsDouble(i);
        }
        return d;
    }
    public static int[] createInts(int len, FInt2Int f){
        int[] d=new int[len];
        for(int i=0;i<len;i++){
            d[i]=f.apply(i);
        }
        return d;
    }
    public static double[] multiply(double[] d,double m){
        double[] d2=new double[d.length];
        for(int i=0;i<d.length;i++){
            d2[i]=d[i]*m;
        }
        return d2;
    }
    public static double[] multiply(double[] d,double[] m){
        if(d.length!=m.length) throw new IllegalArgumentException("d and m must have the same length");
        double[] d2=new double[d.length];
        for(int i=0;i<d.length;i++){
            d2[i]=d[i]*m[i];
        }
        return d2;
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

    public static void render( Graphics g,double mx, double my, double xs, double ys,boolean fill){
        if(fill)g.fillOval(round(mx),round(my),round(xs),round(ys));
        else g.drawOval(round(mx),round(my),round(xs),round(ys));
    }
    public static void render(Graphics g, Box b){
        render(g,b.minX,b.minY,b.xSize(),b.ySize(),true);
    }
    public static void render(Graphics g, Box b,double i){
        render(g,b.minX+i,b.minY+i,b.xSize()-2*i,b.ySize()-2*i,true);
    }
    public static void renderArc(Graphics g,Box b,double start,double end){
        g.drawArc(round(b.minX),round(b.minY),round(b.xSize()),round(b.ySize()),round(start),round(end-start));
    }
    public static void renderCLine(Graphics g, Box b){
        render(g,b.minX,b.minY,b.xSize(),b.ySize(),false);
    }
    public static void renderCLine(Graphics g, Box b,double i){
        render(g,b.minX+i,b.minY+i,b.xSize()-2*i,b.ySize()-2*i,false);
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
        renderString(g,s,centerPos,size,true);
    }
    public static void renderString(Graphics g,String s,Vec2d centerPos,int size,boolean center){
        double offX=center?s.length()*size/4d:0;
        g.setFont(new Font("Microsoft JhengHei",Font.BOLD,size));
        g.drawString(s,round(centerPos.x-offX),round(centerPos.y+ (double) size /2));
    }
    public static void renderString(
            Graphics g,
            String s,
            Vec2d centerPos,
            int size,
            boolean center,
            Color insideColor,
            Color outlineColor,
            float width
    ) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Font font = new Font("Microsoft JhengHei", Font.BOLD, size);
        g2d.setFont(font);

        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, s);


        Rectangle bounds = gv.getPixelBounds(frc, 0, 0);

        double x = centerPos.x;
        double y = centerPos.y;

        if (center) {
            x -= bounds.getWidth() / 2.0;
            y -= bounds.getHeight() / 2.0;
        }

        Shape textShape = gv.getOutline(
                (float) x,
                (float) y
        );

        if (outlineColor != null && width > 0) {
            g2d.setColor(outlineColor);
            g2d.setStroke(new BasicStroke(
                    width,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            g2d.draw(textShape);
        }

        if (insideColor != null) {
            g2d.setColor(insideColor);
            g2d.fill(textShape);
        }
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
    public static void renderPolygon(Graphics g,Vec2d center,int nSides,double radius,double rotation,boolean side,boolean fill,boolean sharp,double[] sharpFactors){
        if(sharp) {
            nSides *= sharpFactors.length;
            int[] xPoints = new int[nSides];
            int[] yPoints = new int[nSides];
            double angleIncrement = 360d / nSides;
            for (int i = 0; i < nSides; i++) {
                double angle = i * angleIncrement + rotation;
                double r=radius*sharpFactors[i%sharpFactors.length];
                Vec2d point = center.add(new Vec2d(cos(angle) * r, sin(angle) * r)).switchToJFrame();
                xPoints[i] = round(point.x);
                yPoints[i] = round(point.y);
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
        render(g,true,fill,points);
    }
    public static void render(Graphics g,boolean outline,boolean fill,Vec2d... points){
        int[] xPoints = new int[points.length];
        int[] yPoints = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            Vec2d point = points[i].switchToJFrame();
            xPoints[i] = round(point.x);
            yPoints[i] = round(point.y);
        }
        if(fill)g.fillPolygon(xPoints, yPoints, points.length);
        if(outline)g.drawPolygon(xPoints, yPoints, points.length);
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
    public static double lerpRotation(double start,double end,double t){
        double angle=end-start;
        if(angle>180){
            angle-=360;
        }
        else if(angle<-180){
            angle+=360;
        }
        return start+angle*t;
    }
    public static Vec2d lerp(Vec2d v1,Vec2d v2,double t){
        return v1.multiply(1-t).add(v2.multiply(t));
    }
    public static Box lerp(Box b1,Box b2,double t){
        return new Box(lerp(b1.getMinPos(),b2.getMinPos(),t),lerp(b1.getMaxPos(),b2.getMaxPos(),t));
    }
    public static <T> T secondIfNull(T t,T t2){
        return t==null?t2:t;
    }
    public static Vec2i getChunkPos(Vec2d vec){
        return new Vec2i((int) Math.floor(vec.x/ EngineMain.chunkSize), (int) Math.floor(vec.y/EngineMain.chunkSize));
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
            return null;
        }else{
            try {
                settingData=Files.readString(setting.toPath(),  StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return settingData;
    }
    public static void putAll(JSONObject obj,JSONObject toAdd){
        for(String key:toAdd.keySet()){
            obj.put(key,toAdd.get(key));
        }
    }
    public static int floor(double d) {
        return (int) Math.floor(d);
    }
    public static int ceil(double d) {
        return (int) Math.ceil(d);
    }
    public static String timeString(){
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }
    public static void memory() {

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();


        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();

        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

        System.out.println(":");
        printMemoryUsage(heapUsage);

        System.out.println(":");
        printMemoryUsage(nonHeapUsage);
    }


    private static void printMemoryUsage(MemoryUsage usage) {
        System.out.printf("start size: %d bytes (%.2f MB)%n", usage.getInit(), bytesToMB(usage.getInit()));
        System.out.printf("current used: %d bytes (%.2f MB)%n", usage.getUsed(), bytesToMB(usage.getUsed()));
        System.out.printf("current committed: %d bytes (%.2f MB)%n", usage.getCommitted(), bytesToMB(usage.getCommitted()));
        System.out.printf("max size: %s%n",
                usage.getMax() == Long.MAX_VALUE ? "unlimited" : String.format("%d bytes (%.2f MB)", usage.getMax(), bytesToMB(usage.getMax())));
    }
    public static void fixSettings(JSONObject data,JSONObject standard){
        for(String key:standard.keySet()){
            if(!data.has(key)){
                data.put(key,standard.get(key));
            }
        }
    }
    private static double bytesToMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
    public static void loadImagesRecursively(File dir, Map<String, BufferedImage> map) {
        if (dir == null || !dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadImagesRecursively(file, map);
            } else if (isImageFile(file)) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        map.put(file.getName(), img);
                    }
                } catch (IOException e) {
                    System.err.println("read image failed: " + file.getAbsolutePath());
                }
            }
        }
    }

    public static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") ||
                name.endsWith(".jpg") ||
                name.endsWith(".jpeg") ||
                name.endsWith(".bmp") ||
                name.endsWith(".gif");
    }
    public static String imageToString(BufferedImage image, String format) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("error transform image to string", e);
        }
    }

    public static BufferedImage stringToImage(String base64) {
        try (ByteArrayInputStream bais =
                     new ByteArrayInputStream(Base64.getDecoder().decode(base64))) {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException("error transform string to image", e);
        }
    }
    public static List<String> splitString(String input, int maxChunkSize) {
        List<String> parts = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return parts;
        }

        int length = input.length();
        for (int i = 0; i < length; i += maxChunkSize) {
            int end = Math.min(length, i + maxChunkSize);
            parts.add(input.substring(i, end));
        }

        return parts;
    }
    public static Object parseString(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {}
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {}
        return value;
    }
    public static <T> T tryGet(Getter<T> getter){
        return tryGet(getter,null,null);
    }
    public static <T> T tryGet(Getter<T> getter,T defaultValue,Runnable onError){
        try{
            return getter.get();
        }catch(Exception e){
            if(onError!=null){
                onError.run();
            }
            return defaultValue;
        }
    }
    public static <T> T tryGet(Class<T> clazz, Getter<T> getter){
        try{
            return clazz.cast(getter.get());
        }catch(Exception e){
            return null;
        }
    }
    public static HitResult raycast(World world, Vec2d start, Vec2d end) {
        double x0 = start.x;
        double y0 = start.y;
        double x1 = end.x;
        double y1 = end.y;


        int x = (int) Math.floor(x0);
        int y = (int) Math.floor(y0);

        double dx = x1 - x0;
        double dy = y1 - y0;

        int stepX = (dx > 0) ? 1 : (dx < 0 ? -1 : 0);
        int stepY = (dy > 0) ? 1 : (dy < 0 ? -1 : 0);

        double tMaxX, tMaxY;
        double tDeltaX, tDeltaY;

        if (dx != 0) {
            double nextGridX = x + (stepX > 0 ? 1 : 0);
            tMaxX = (nextGridX - x0) / dx;
            tDeltaX = stepX / dx;
        } else {
            tMaxX = Double.POSITIVE_INFINITY;
            tDeltaX = Double.POSITIVE_INFINITY;
        }

        if (dy != 0) {
            double nextGridY = y + (stepY > 0 ? 1 : 0);
            tMaxY = (nextGridY - y0) / dy;
            tDeltaY = stepY / dy;
        } else {
            tMaxY = Double.POSITIVE_INFINITY;
            tDeltaY = Double.POSITIVE_INFINITY;
        }

        Direction lastFace = null;

        while (true) {
            if (world.getBlock(x, y).solid) {
                Vec2d hitPos;

                if (lastFace == null) {
                    hitPos = start;
                } else {
                    double tHit;
                    if (lastFace == Direction.LEFT || lastFace == Direction.RIGHT) {
                        tHit = tMaxX - tDeltaX;
                    } else {
                        tHit = tMaxY - tDeltaY;
                    }

                    tHit = Math.max(0, Math.min(1, tHit));

                    double hitX = x0 + dx * tHit;
                    double hitY = y0 + dy * tHit;
                    hitPos = new Vec2d(hitX, hitY);
                }

                return new HitResult(true,hitPos, new Vec2i(x, y), lastFace);
            }

            if (x == (int)Math.floor(x1) && y == (int)Math.floor(y1)) break;

            if (tMaxX < tMaxY) {
                x += stepX;
                tMaxX += tDeltaX;
                lastFace = (stepX > 0) ? Direction.LEFT : Direction.RIGHT;
            } else {
                y += stepY;
                tMaxY += tDeltaY;
                lastFace = (stepY > 0) ? Direction.DOWN : Direction.UP;
            }
        }

        return new HitResult(false,null, null, null);
    }
    public record HitResult(boolean hit,Vec2d pos, Vec2i blockPos, Direction direction){

    }
    public static Vec2d[] movingCircleCollision(
            Vec2d p1, Vec2d v1, double r1,
            Vec2d p2, Vec2d v2, double r2,double maxT) {
        Vec2d dp = p1.subtract(p2);
        Vec2d dv = v1.subtract(v2);
        double a = dv.dot(dv);
        double b = 2 * dp.dot(dv);
        double c = dp.dot(dp) - (r1 + r2) * (r1 + r2);
        if (Math.abs(a) < 1e-12) {
            return null;
        }
        double disc = b * b - 4 * a * c;
        if (disc < 0) {
            return null;
        }
        double sqrtDisc = Math.sqrt(disc);
        double t1 = (-b - sqrtDisc) / (2 * a);
        double t2 = (-b + sqrtDisc) / (2 * a);
        double t = (t1 >= 0) ? t1 : ((t2 >= 0) ? t2 : Double.NaN);
        if (Double.isNaN(t)||t>=maxT) {
            return null;
        }
        Vec2d posA = p1.add(v1.multiply(t));
        Vec2d posB = p2.add(v2.multiply(t));
        return new Vec2d[]{posA, posB};
    }

}
