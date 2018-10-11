package com.imc.mvc.model;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author luoly
 * @date 2018/10/11 15:02
 * @description
 */
public class HandlerModel {

    public Method method;

    public Object controller;

    public Map<String, Integer> paramMap;

    public HandlerModel(Method method, Object controller, Map<String, Integer> paramMap) {
        this.method = method;
        this.controller = controller;
        this.paramMap = paramMap;
    }
}
