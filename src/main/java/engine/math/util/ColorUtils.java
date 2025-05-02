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
}
