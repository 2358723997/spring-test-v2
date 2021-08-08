package com.test.spring.framework.beans.config;

import lombok.Data;

/**
 * BeanDefinition类
 *
 * @author wangjixue
 * @date 8/8/21 8:25 PM
 */

@Data
public class MyBeanDefinition {

    private String factoryBeanName;

    private String beanClassName;

}
