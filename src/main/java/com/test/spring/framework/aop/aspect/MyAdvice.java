package com.test.spring.framework.aop.aspect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import lombok.Data;

/**
 * MyAdvice类
 *
 * @author wangjixue
 * @date 8/14/21 6:11 PM
 */
@Data
public class MyAdvice {

    private Object aspect;
    private Method method;
    private String throwName;

    public MyAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.method = adviceMethod;
    }

}
