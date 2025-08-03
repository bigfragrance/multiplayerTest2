package engine.math.test;

import java.util.Stack;
import java.util.ArrayList;

public class StringEnclosedRemover {

    public static String removeEnclosedParts(String str, char ch) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        int n = str.length();
        Stack<Integer> stack = new Stack<>();
        ArrayList<int[]> ranges = new ArrayList<>();


        for (int i = 0; i < n; i++) {
            if (str.charAt(i) == ch) {
                if (!stack.isEmpty()) {
                    int start = stack.pop();
                    ranges.add(new int[]{start, i});
                } else {
                    stack.push(i);
                }
            }
        }


        int[] diff = new int[n + 1];
        for (int[] range : ranges) {
            int start = range[0];
            int end = range[1];
            diff[start]++;
            if (end + 1 < n) {
                diff[end + 1]--;
            }
        }


        boolean[] deleteFlag = new boolean[n];
        int cover = 0;
        for (int i = 0; i < n; i++) {
            cover += diff[i];
            if (cover > 0) {
                deleteFlag[i] = true;
            }
        }


        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (!deleteFlag[i]) {
                result.append(str.charAt(i));
            }
        }

        return result.toString();
    }

    public static void main(String[] args) {
        // 示例测试
        String input = "a#b#c##d#e";
        char ch = '#';
        String output = removeEnclosedParts(input, ch);
        System.out.println("Original: " + input);
        System.out.println("After removal: " + output);
        // 输出: Original: a#b#c##d#e
        //       After removal: acd#e
    }
}