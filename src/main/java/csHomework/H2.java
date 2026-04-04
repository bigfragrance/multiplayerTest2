package csHomework;

import java.util.Scanner;
import java.util.regex.Pattern;

public class H2 {
    public static void main(String[] a){
        Pattern pattern=Pattern.compile("[a-zA-Z0-9]*\\s+[a-zA-Z0-9]+");
        String input0=" "+new Scanner(System.in).nextLine();
        int count0=pattern.matcher(input0).results().toArray().length;
        System.out.println(count0);

        Scanner input=new Scanner(System.in);
        int count=0;
        boolean lastSpace=true;
        String in=input.nextLine();
        for(int i=0;i<in.length();i++){
            if(in.substring(i,i+1).equals(" ")){
                lastSpace=true;
            }else if(lastSpace){
                count++;
                lastSpace=false;
            }
        }
        System.out.println(count);
    }
}
