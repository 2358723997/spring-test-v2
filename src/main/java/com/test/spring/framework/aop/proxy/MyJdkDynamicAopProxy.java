package com.test.spring.framework.aop.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.test.spring.framework.aop.aspect.MyAdvice;
import com.test.spring.framework.aop.support.MyAdvisedSupport;

/**
 * MyJdkDynamicAopProxy类
 *
 * @author wangjixue
 * @date 8/14/21 5:57 PM
 */
public class MyJdkDynamicAopProxy implements InvocationHandler {
    private MyAdvisedSupport config;

    public MyJdkDynamicAopProxy(MyAdvisedSupport config) {
        this.config = config;
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),this.config.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, MyAdvice> adviceMap = config.getAdvices(method, null);
        Object result = null;
        try {
            invokeAdivce(adviceMap.get("before"));
            result = method.invoke(this.config.getTarget(), args);
            invokeAdivce(adviceMap.get("after"));
        } catch (Exception e) {
            invokeAdivce(adviceMap.get("afterThrowing"));
            e.printStackTrace();
        }
        return result;
    }

    private void invokeAdivce(MyAdvice advice) {
        try {
            advice.getMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

}
