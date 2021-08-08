package com.test.spring.framework.mvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import lombok.Data;

/**
 * MyHandlerMapping类
 *
 * @author wangjixue
 * @date 8/9/21 12:03 AM
 */
@Data
public class MyHandlerMapping {
    /**
     * URL
     */
    private Pattern pattern;
    /**
     * 对应的Method
     */
    private Method method;
    /**
     * Method对应的实例对象
     */
    private Object controller;

    public MyHandlerMapping(Pattern pattern, Object instance, Method method) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
    }
}
