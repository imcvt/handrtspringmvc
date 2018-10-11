package com.imc.mvc.util;

import org.objectweb.asm.*;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author luoly
 * @date 2018/10/11 15:48
 * @description
 */
public class Play {

    /**
     * 获取指定类指定方法的所有参数名
     *
     * @param clazz
     * @param method
     * @return 按参数顺序排列的参数列表, 如果没有参数则返回null
     */
    public static String[] getMethodParameterNamesByAsm4(final Class clazz, final Method method) {

        final String methodName = method.getName();
        final Class<?>[] methodParamTypes = method.getParameterTypes();
        final int methodParamCount = methodParamTypes.length;

        //clazz直接取?
        String className = method.getDeclaringClass().getName();

        final boolean isStatic = Modifier.isStatic(method.getModifiers());
        final String[] methodParamNames = new String[methodParamCount];

        int lastDotIndex = className.lastIndexOf(".");
        className = className.substring(lastDotIndex + 1) + ".class";
        InputStream is = clazz.getResourceAsStream(className);

        try {
            ClassReader cr = new ClassReader(is);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cr.accept(new ClassAdapter(cw) {

                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

                    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                    final Type[] argTypes = Type.getArgumentTypes(desc);

                    //参数类型不一致
                    if(!methodName.equals(name) || !matchTypes(argTypes, methodParamTypes)) {
                        return mv;
                    }

                    return new MethodAdapter(mv) {
                        @Override
                        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                            //如果是静态方法,第一个不是方法参数,非静态方法,则第一个参数是this,然后老师方法的参数
                            int methodParamIndex = isStatic ? index : index -1;
                            if(0 <= methodParamIndex && methodParamIndex < methodParamCount) {
                                methodParamNames[methodParamIndex] = name;

                            }
                            super.visitLocalVariable(name, desc, signature, start, end, index);
                        }
                    };
                }
            }, 0);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return methodParamNames;
    }

    /**
     * 比较参数是否一致
     * @param types
     * @param methodParamTypes
     * @return
     */
    private static boolean matchTypes(Type[] types, Class<?>[] methodParamTypes) {

        if(types.length != methodParamTypes.length) {
            return false;
        }

        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(methodParamTypes[i]).equals(types[i])) {
                return false;
            }
        }

        return true;
    }
}
