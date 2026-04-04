package csHomework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class H6 {
    public static void main(String[] args){
        int[] ints=new int[21];
        int len=20;
        for(int i=0;i<20;i++){
            ints[i]=(i+1)*5;
        }
        Scanner input=new Scanner(System.in);
        int v=input.nextInt();
        for(int i=0;i<=20;i++){
            if(ints[i]==v){
                for(int j=i;j<19;j++){
                    ints[j]=ints[j+1];
                }
                ints[19]=0;
                len--;
                break;
            }else if(ints[i]>v||i==20){
                for(int j=20;j>i;j--){
                    ints[j]=ints[j-1];
                }
                ints[i]=v;
                len++;
                break;
            }
        }
        System.out.println(Arrays.toString(Arrays.copyOf(ints,len)));
    }
}
