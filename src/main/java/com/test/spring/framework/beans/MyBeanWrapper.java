package com.test.spring.framework.beans;

/**
 * MyBeanWrapper类
 *
 * @author wangjixue
 * @date 8/8/21 8:26 PM
 */
public class MyBeanWrapper {
    private Object wrapperInstance;
    private Class<?> wrapperClass;

    public MyBeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.wrapperClass = instance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrappedClass() {
        return wrapperClass;
    }
}
