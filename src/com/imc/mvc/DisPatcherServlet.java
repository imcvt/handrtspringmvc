package com.imc.mvc;

import com.imc.mvc.annotation.*;
import com.imc.mvc.model.HandlerModel;
import com.imc.mvc.util.Play;
import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private Map<String, HandlerModel> handlerMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html;charset=utf-8");
        PrintWriter printWriter = resp.getWriter();

//        printInfo(req, resp, printWriter);
        doPost(req, resp);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter printWriter = resp.getWriter();

        //根据请求的url查找对应的method
        try {
            boolean isMatcher = pattern(req, resp);
            if(!isMatcher) {
                printWriter.write("404 not found");
            }
        }catch (Exception e) {
            printWriter.write(e.getMessage());
        }

    }

    @Override
    public void init() throws ServletException {
        System.out.println("初始化开始-->");
        scanPackage(getInitParameter("scanPackage"));

        doInstance();
        System.out.println("bean映射-->" + instanceMap);

        doAutoWired();

        doHandlerMapping();
        System.out.println("方法映射-->" + handlerMapping);

        System.out.println("初始化结束-->");
    }

    private boolean pattern(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        if(handlerMapping.isEmpty()) {
            return false;
        }

        String uri = req.getRequestURI().replaceAll("/+", "/");
//        String context
        for(Map.Entry<String, HandlerModel> entry : handlerMapping.entrySet()) {
            if(uri.equals(entry.getKey())) {

                HandlerModel handlerModel = entry.getValue();
                Object[] params = new Object[handlerModel.paramMap.size()];

                //按顺序存储的方法的类型
                Class<?>[] paramTypes = handlerModel.method.getParameterTypes();

                for(Map.Entry<String, Integer> paramEntry : handlerModel.paramMap.entrySet()) {

//                    String reqParam = req.getParameter(paramEntry.getKey());

                    String key = paramEntry.getKey();
                    if(key.equals(HttpServletRequest.class.getName())) {
                        params[paramEntry.getValue()] = req;
                    }else if(key.equals(HttpServletResponse.class.getName())) {
                        params[paramEntry.getValue()] = resp;
                    }else {
                        //paramEntry.getValue()得到的就是方法参数所在的序号，paramTypes数组刚好是按顺序保存的方法的参数类型，不得不感叹设计者的厉害
                        params[paramEntry.getValue()] = convertType(req.getParameter(paramEntry.getKey()), paramTypes[paramEntry.getValue()]);
                    }
                }
                handlerModel.method.invoke(handlerModel.controller, params);
                return true;
            }
        }

        return false;
    }

    /**
     * 把String转换成对应的参数类型
     * @param target
     * @param type
     * @return
     */
    private Object convertType(String target, Class type) {
        if(type == String.class) {
            return target;
        }else if(type == Integer.class) {
            return Integer.valueOf(target);
        }else if(type == Long.class) {
            return Long.valueOf(target);
        }else if(type == Boolean.class) {
            if(target.toLowerCase().equals("true")) {
                return true;
            }else if(target.toLowerCase().equals("false")) {
                return false;
            }
            throw new RuntimeException("不支持的参数类型");
        }else {
            return null;
        }
    }

    /**
     * 建立url到方法的映射
     */
    private void doHandlerMapping() {

        if(instanceMap.size() == 0) {
            return;
        }

        //遍历controller，把url和对应的方法放到map中
        for(Map.Entry<String, Object> entry : instanceMap.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();
            //非controller注解跳过
            if(!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }

            //类注解上的url,url前面都加了一个"/"，是为了防止用户在写路径url的时候不写"/"或者多写
            String clsUrl = "/" + clazz.getAnnotation(RequestMapping.class).value();
            //方法注解上的url
            String mthdUrl = "";
            //拼接
            String url = "";
            //遍历方法
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {

                //非RequestMapping注解跳过
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                mthdUrl = method.getAnnotation(RequestMapping.class).value();

                url = (clsUrl + "/" + mthdUrl).replaceAll("/+", "/");

                //此处不能只保存url和method,因为调用invoke(Object, Object...)需要知道当前方法所在的类及方法中的所有参数
//                handlerMapping.put(url, method);

                Annotation[][] annotations = method.getParameterAnnotations();

                //获取方法里按顺序排列的所有参数名(引用了asm-3.3.1.jar, 需要在WEB-INF下新建lib文件夹,把jar放入)
                //比如Controllerr的add方法, 将得到["name", "addr", "request", "response"]
                String[] paramNames = Play.getMethodParameterNamesByAsm4(clazz, method);

                Map<String, Integer> paramMap = new HashMap<>();

                //获取参数类型,提取Request和Response的索引
                Class<?>[] paramTypes = method.getParameterTypes();

                for(int i=0; i<annotations.length; i++) {
                    //获取每个参数上的所有注解, 当i=0时说明获取第一个参数上的所有注解
                    Annotation[] anots = annotations[i];
                    if (anots.length == 0) {
                        //如果没有注解,则是如String abc, Request request这种
                        Class<?> type = paramTypes[i];
                        if(type == HttpServletRequest.class || type == HttpServletResponse.class) {
                            paramMap.put(type.getName(), i);
                        }
                    }else {
                        //参数没有@RequestParam,只写了String name, 那么通过java是无法取到name这个属性名的
                        //可以通过asm获取
                        paramMap.put(paramNames[i], i);
                        continue;
                    }

                    //如果有注解,遍历每个参数上的所有注解
                    for(Annotation ans : anots) {
                        //找到被RequestParam注解的参数,并取value的值
                        if(ans.annotationType() == RequestParam.class) {
                            String paramName = ((RequestParam) ans).value();
                            if(!"".equals(paramName.trim())) {
                                paramMap.put(paramName, i);
                            }
                        }
                    }

                    HandlerModel model = new HandlerModel(method, entry.getValue(), paramMap);

                    handlerMapping.put(url, model);
                }


            }

        }

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
                    //@TODO field.set方法
                    field.set(entry.getValue(), instanceMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

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
                        instanceMap.put(serviceName.trim(), clazz.newInstance());
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
