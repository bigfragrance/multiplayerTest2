package csHomework;

import big.engine.render.Screen;

import java.util.Scanner;

public class H1 {
    public static void main(String[] a){
        Scanner input=new Scanner(System.in);
        int num=input.nextInt();
        if(num<=1){
            System.out.println("NO");
            return;
        }
        if(num<=3){
            System.out.println("YES");
            return;
        }
        if(num%2==0||num%3==0){
            System.out.println("NO");
            return;
        }
        for(int i=5;i*i<=num;i+=6){
            if(num%i==0||num%(i+2)==0){
                System.out.println("NO");
                return;
            }
        }
        System.out.println("YES");
    }
}
