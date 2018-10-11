package com.imc.test;

import com.imc.mvc.DisPatcherServlet;

/**
 * @author luoly
 * @date 2018/10/10 11:46
 * @description
 */
public class Test {

    public static void main(String[] args) {
//        System.out.println(lowerFirstChar("LinkageError"));

//        replaceAll("///aa//f/s");

        classname(DisPatcherServlet.class);
    }

    private static String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private static void replaceAll(String url) {
        System.out.println(url.replaceAll("/+", "/"));
    }


    private static void classname(Class clazz) {
        System.out.println("name->" + clazz.getName() + ",simplename->" + clazz.getSimpleName());
    }
}
