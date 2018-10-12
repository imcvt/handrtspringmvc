package com.imc.test;

import org.junit.Test;

/**
 * @author luoly
 * @date 2018/10/12 15:15
 * @description
 */

public class testLoader {


    /**
     * 父类classloader
     * @throws Exception
     */
    @Test
    public void test2() throws Exception{
        MyClassLoader loader = new MyClassLoader();
        Class<?> c = loader.loadClass("com.imc.test.HighRichHandsome");
        System.out.println("Loaded by :" + c.getClassLoader());

        Person p = (Person) c.newInstance();
        p.say();

        HighRichHandsome man = (HighRichHandsome) c.newInstance();
        man.say();
    }

    /**
     * 自己的classloader加载
     * @throws Exception
     */
    @Test
    public void test3() throws Exception{
        MyClassLoader loader = new MyClassLoader();
        Class<?> c = loader.findClass("com.imc.test.HighRichHandsome");
        System.out.println("Loaded by :" + c.getClassLoader());

        Person p = (Person) c.newInstance();
        p.say();

        //注释下面两行则不报错
        HighRichHandsome man = (HighRichHandsome) c.newInstance();
        man.say();
    }
}
