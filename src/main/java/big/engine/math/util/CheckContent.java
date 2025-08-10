package big.engine.math.util;


import big.engine.math.Box;

import java.io.*;

public class CheckContent {
    public Box box;
    public boolean left;
    public boolean right;
    public boolean top;
    public boolean bottom;
    public CheckContent(Box box, boolean left, boolean right, boolean top, boolean bottom) {
        this.box = box;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }
}
