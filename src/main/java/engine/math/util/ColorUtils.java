package engine.math.util;

import java.awt.*;

public class ColorUtils {

    public static String toString(Color color) {
        return String.format("rgba(%d,%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Color fromString(String colorString) {

        colorString = colorString.replace("rgba(", "").replace(")", "");


        String[] rgbaValues = colorString.split(",");

        if (rgbaValues.length != 4) {
            throw new IllegalArgumentException("Invalid color string format");
        }

        int red = Integer.parseInt(rgbaValues[0]);
        int green = Integer.parseInt(rgbaValues[1]);
        int blue = Integer.parseInt(rgbaValues[2]);
        int alpha = Integer.parseInt(rgbaValues[3]);


        return new Color(red, green, blue, alpha);
    }
    public static Color darker(Color c,double factor) {
        return new Color(Math.min((int)(c.getRed()  *factor), 255),
                Math.min((int)(c.getGreen()*factor), 255),
                Math.min((int)(c.getBlue() *factor), 255),
                c.getAlpha());
    }
    public static Color brighter(Color c,double factor) {
        return opposite(darker(opposite(c),factor));
    }
    public static Color opposite(Color c){
        return new Color(255-c.getRed(),255-c.getGreen(),255-c.getBlue(),c.getAlpha());
    }
    public static Color setAlpha(Color c,int alpha){
        return new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha);
    }
    public static Color setAlpha(Color c,double alpha){
        alpha=Math.clamp(alpha,0.01,0.99);
        return new Color(c.getRed(),c.getGreen(),c.getBlue(),(int)(alpha*255));
    }
}
