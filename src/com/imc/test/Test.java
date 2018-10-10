package com.imc.test;

/**
 * @author luoly
 * @date 2018/10/10 11:46
 * @description
 */
public class Test {

    public static void main(String[] args) {
        String s = "LinkageError";
        System.out.println(lowerFirstChar(s));
    }

    private static String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
