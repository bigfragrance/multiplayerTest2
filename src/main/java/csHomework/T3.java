package csHomework;

import java.util.Scanner;

public class T3 {
    public static void main(String[] a){
        Scanner input=new Scanner(System.in);
        int i=input.nextInt();
        boolean b=false;
        for(int j=3;j<=7;j+=2){
            if(i%j==0){
                b=true;
                System.out.printf("%d ",j);
            }
        }
        if(!b){
            System.out.println("n");
        }
        "ssasf".replace("asf","asdf");
    }
}
