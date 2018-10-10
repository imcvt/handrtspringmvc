package com.imc.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author luoly
 * @date 2018/10/10 11:43
 * @description
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    String value() default "";
}
