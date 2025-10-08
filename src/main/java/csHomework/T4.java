package csHomework;

import java.util.Scanner;

public class T4 {
    public static void main(String[] a){
        Scanner input=new Scanner(System.in);
        int i=input.nextInt();
        int j=input.nextInt();
        System.out.println(i<60^j<60?1:0);
    }
}
