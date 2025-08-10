package big.engine.math.test;

import big.engine.math.util.FileUtil;
import big.engine.math.util.Util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringProcessor {
    public static void main(String[] args) {
        String path="word.txt";
        String out="after.txt";
        String input = Util.read(path);
        String result = StringEnclosedRemover.removeEnclosedParts(input,'/');
       //result=result.replaceAll("n"+".","");
       //result=result.replaceAll("adj"+".","");
       //result=result.replaceAll("adv"+".","");
       //result=result.replaceAll("prep"+".","");
       //result=result.replaceAll("v"+".","");
       //result=result.replaceAll("vi"+".","");
       //result=result.replaceAll("vt"+".","");
        Util.write(out,result);
    }


}    