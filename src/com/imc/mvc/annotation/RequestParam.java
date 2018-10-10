package com.imc.mvc.annotation;

import java.lang.annotation.*;

/**
 * @author luoly
 * @date 2018/10/10 11:43
 * @description
 */

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

    String value() default "";
}
