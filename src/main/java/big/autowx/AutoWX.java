package big.autowx;

import big.engine.math.Vec2d;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class AutoWX {
    public static Vec2d wxPos=new Vec2d(941,1057);
    public static String tmpImgPath="tmp.png";
    public static Robot robot;
    public static void main(String[] args) throws Exception {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        moveAndClick(wxPos);
        sleep(50);
        robot.mouseWheel(-10000);
        sleep(50);
        BufferedImage img=getScreenShot();
        ImageIO.write(img,"png",new File(tmpImgPath));
        long startTime=System.currentTimeMillis();
        String result=OcrAnnotatorFull.ocr(tmpImgPath);
        long endTime=System.currentTimeMillis();
        System.out.println("ocr time: "+(endTime-startTime)+"ms");
        JSONArray array=new JSONArray(result);
        Vec2d pos=find(array,"dai"/*target name*/);
        if(pos!=null){
            moveAndClick(pos);
            System.out.println("click "+pos);
            send("test");
            Vec2d pos2=find(array,"File");//move away from target dialog
            if(pos2!=null){
                moveAndClick(pos2);
            }
        }
    }
    public static void send(String msg){
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(msg), null);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        sleep(5);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }
    public static Vec2d find(JSONArray array,String... name){
        for(int i=0;i<array.length();i++){
            JSONObject o=array.getJSONObject(i);
            if(contains(o.getString("text"),name)){
                JSONArray bbox = o.getJSONArray("bbox");

                int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
                for (int j = 0; j < 4; j++) {
                    JSONArray p = bbox.getJSONArray(j);
                    int x = (int) p.getDouble(0);
                    int y = (int) p.getDouble(1);
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
                return new Vec2d(minX+maxX,minY+maxY).multiply(0.5);
            }
        }
        return null;
    }
    public static boolean contains(String o,String... name){
        for(String n:name){
            if(o.contains(n)) return true;
        }
        return false;
    }
    public static void sleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void moveAndClick(Vec2d vec){
        moveMouse(vec);
        clickMouse();
    }
    public static void moveMouse(Vec2d vec){
        moveMouse((int) vec.x,(int) vec.y);
    }
    public static void moveMouse(int x,int y){
        robot.mouseMove(x,y);
    }
    public static void clickMouse(){
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    public static BufferedImage getScreenShot(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        Rectangle screenRect = new Rectangle(0,0,width/5,height);
        return robot.createScreenCapture(screenRect);
    }
}
