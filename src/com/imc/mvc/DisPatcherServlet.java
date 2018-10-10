package com.imc.mvc;

import com.imc.mvc.annotation.Autowired;
import com.imc.mvc.annotation.Controller;
import com.imc.mvc.annotation.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author luoly
 * @date 2018/10/10 09:53
 * @description
 */
public class DisPatcherServlet extends HttpServlet {

    private List<String> classNames = new ArrayList<>();
    private Map<String, Object> instanceMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html;charset=utf-8");
        PrintWriter printWriter = resp.getWriter();

        printInfo(req, resp, printWriter);


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init() throws ServletException {
        System.out.println("初始化");
        scanPackage(getInitParameter("scanPackage"));

        doInstance();

        doAutoWired();
    }

    /**
     * 给所有的托管对象注入属性
     */
    private void doAutoWired() {

        if (instanceMap.size() == 0) {
            return;
        }

        //遍历所有被托管的对象
        for (Map.Entry<String, Object> entry : instanceMap.entrySet()) {

            //查找所有被Autowired注解的属性
            // getFields()获得某个类的所有的公共（public）的字段，包括父类
            // getDeclaredFields()获得某个类的所有申明的字段，即包括public、private和proteced，但是不包括父类的申明字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();

            for(Field field : fields) {
                if(!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                String beanName;

                //获取属性上注解的值
                Autowired autowired = field.getAnnotation(Autowired.class);
                if("".equals(autowired.value())) {
                    beanName = lowerFirstChar(field.getType().getSimpleName());
                }else {
                    beanName = autowired.value();
                }

                //将私有化属性设置为true，不然会打不开
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), instanceMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
            entry.getKey();
            entry.getValue();

        }

    }

    /**
     * 初始化所有被托管的bean
     */
    private void doInstance() {
        if (classNames.size() == 0) {
            return;
        }

        //遍历所有被托管的类，并且实例化
        for (String className : classNames) {
            try {

                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {

                    instanceMap.put(clazz.getSimpleName(), clazz.newInstance());

                } else if (clazz.isAnnotationPresent(Service.class)) {

                    Service service = clazz.getAnnotation(Service.class);
                    String serviceName = service.value();

                    //如果注解的值不为空则把值当作bean名称
                    if (!"".equals(serviceName.trim())) {
                        instanceMap.put(lowerFirstChar(serviceName.trim()), clazz.newInstance());
                    } else {
                        //为空则把实现的接口名当作bean名称
                        Class[] classes = clazz.getInterfaces();

                        //接口可能不止一个，这里简单处理第一个
                        for (int i = 0; i < classes.length; i++) {
                            instanceMap.put(lowerFirstChar(classes[i].getSimpleName()), classes[i].newInstance());
                            break;
                        }
                    }

                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 把第一个字母转换成小写
     *
     * @param str
     * @return
     */
    private String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 扫描包下的所有类
     *
     * @param pkgName
     */
    private void scanPackage(String pkgName) {
        //获取指定包的实际路径url，将com.imc.mvc转换成包路径格式
        URL url = getClass().getClassLoader().getResource("/" + pkgName.replaceAll("\\.", "/"));
        //转化成file对象
        File dir = new File(url.getFile());

        //递归查询所有的class文件
        for (File file : dir.listFiles()) {
            //如果是目录，就递归目录的下一层
            if (file.isDirectory()) {
                scanPackage(pkgName + "." + file.getName());

            } else {
                //如果不是class文件，说明不是被spring托管的
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                //举例，className=com.imc.controller.WebController
                String className = pkgName + "." + file.getName().replace(".class", "");
                //判断是否被Controller或者Service注解了，如果没注解，就不管
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)) {
                        classNames.add(className);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 解决get请求乱码
     *
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getNewString(String str) throws UnsupportedEncodingException {
        return new String((str + "<br />").getBytes("GBK"));
    }

    void printInfo(HttpServletRequest req, HttpServletResponse resp, PrintWriter printWriter) throws IOException {
        //http://localhost:8080/day08/DemoServlet?name=jack&passwd=123
        //1统一资源标记符,/day08/DemoServlet
        String uri = req.getRequestURI();
        printWriter.write(getNewString("统一资源标记符" + uri));

        //2统一资源定位符
        StringBuffer url = req.getRequestURL();
        printWriter.write(getNewString("统一资源定位符" + url));

        //3协议和版本 HTTP/1.1
        String protocol = req.getProtocol();
        printWriter.write(getNewString("协议和版本" + protocol));

        //4协议 http
        String scheme = req.getScheme();
        printWriter.write(getNewString("协议" + scheme));

        //5主机（域名）localhost,如果是ip就显示ip
        String servrName = req.getServerName();
        printWriter.write(getNewString("主机名" + servrName));

        //6请求参数，?后所有
        String queryString = req.getQueryString();
        printWriter.write(getNewString("所有请求参数" + queryString));

        //远程主机的ip地址
        String remoteAddr = req.getRemoteAddr();
        printWriter.write(getNewString("远程主机的ip地址" + remoteAddr));
    }

}
