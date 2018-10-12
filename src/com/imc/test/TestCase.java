package com.imc.test;

import com.imc.mvc.model.HandlerModel;
import org.junit.Test;
import sun.tools.jar.Main;

import java.io.File;
import java.net.URL;

/**
 * @author luoly
 * @date 2018/10/10 11:46
 * @description
 */
public class TestCase {


    @Test
    public void getUrl() {
        // 当前类(class)所在的包目录
        System.out.println("当前类(class)所在的包目录-->"+HandlerModel.class.getResource(""));
        // class path根目录
        System.out.println("class path根目录-->" + HandlerModel.class.getResource("/"));

        // HandlerModel.class在<bin>/testpackage包中
        // 2.properties  在<bin>/testpackage包中
        System.out.println(HandlerModel.class.getResource("2.properties"));

        // TestMain.class在<bin>/testpackage包中
        // 3.properties  在<bin>/testpackage.subpackage包中
        System.out.println(HandlerModel.class.getResource("subpackage/3.properties"));

        // TestMain.class在<bin>/testpackage包中
        // 1.properties  在bin目录（class根目录）
        System.out.println(HandlerModel.class.getResource("/1.properties"));
    }

    /**
     * 查看父类加载器
     */
    @Test
    public void loader() {
        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
        System.out.println("系统类装载器:" + appClassLoader);
        ClassLoader extensionClassLoader = appClassLoader.getParent();
        System.out.println("系统类装载器的父类加载器——扩展类加载器:" + extensionClassLoader);
        ClassLoader bootstrapClassLoader = extensionClassLoader.getParent();
        System.out.println("扩展类加载器的父类加载器——引导类加载器:" + bootstrapClassLoader);
    }

    /**
     * 获取classpath路径等
     */
    @Test
    public void getClassPath() {

        URL url = Thread.currentThread().getContextClassLoader().getResource("/");
        System.out.println(url);

        //获取指定包的实际路径url，将com.imc.mvc转换成包路径格式
        URL url1 = getClass().getClassLoader().getResource("/com/imc/mvc");
        System.out.println(url1);
        //转化成file对象
        File dir0 = new File(url1.getFile());
        System.out.println(dir0.listFiles());
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
