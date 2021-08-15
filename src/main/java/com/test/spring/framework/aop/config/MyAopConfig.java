package com.test.spring.framework.aop.config;

import lombok.Data;

/**
 * MyAopConfig类
 *
 * @author wangjixue
 * @date 8/14/21 5:59 PM
 */
@Data
public class MyAopConfig {
    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
