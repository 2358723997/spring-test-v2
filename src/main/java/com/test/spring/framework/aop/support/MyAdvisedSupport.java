package com.test.spring.framework.aop.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.test.spring.framework.aop.aspect.MyAdvice;
import com.test.spring.framework.aop.config.MyAopConfig;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * MyAdvisedSupport类
 *
 * @author wangjixue
 * @date 8/14/21 5:51 PM
 */
@Data
public class MyAdvisedSupport {

    private Class<?> targetClass;

    private Object target;

    private MyAopConfig config;
    private Pattern pointCutClassPattern;

    private Map<Method, Map<String, MyAdvice>> methodCache;

    public MyAdvisedSupport(MyAopConfig config) {
        this.config = config;
    }

    /**
     * 给ApplicationContext首先IOC中的对象初始化时调用，决定要不要生成代理类的逻辑
     *
     * @return
     */
    public boolean pointCutMath() {
        return pointCutClassPattern.matcher(targetClass.toString()).matches();
    }

    /**
     * 根据一个目标代理类的方法，获得其对应的通知
     *
     * @param method
     * @param o
     * @return
     */
    public Map<String, MyAdvice> getAdvices(Method method, Object o) throws NoSuchMethodException {
        //享元设计模式的应用
        Map<String, MyAdvice> adviceMap = methodCache.get(method);
        if (null == adviceMap) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            adviceMap = methodCache.get(m);
            methodCache.put(m, adviceMap);
        }
        return adviceMap;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    /**
     * 解析配置文件的方法
     */
    private void parse() {
        //把Spring的Excpress变成Java能够识别的正则表达式
        String pointCut = config.getPointCut()
            .replaceAll("\\.", "\\\\.")
            .replaceAll("\\\\.\\*", ".*")
            .replaceAll("\\(", "\\\\(")
            .replaceAll("\\)", "\\\\)");
        //保存专门匹配Class的正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        //保存专门匹配方法的正则
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));
        //享元的共享池
        methodCache = new HashMap<>();
        //保存专门匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try {
            Class<?> aspectClass = Class.forName(config.getAspectClass());
            Map<String, Method> aspectMethods = new HashMap<String, Method>();

            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            for (Method method : targetClass.getMethods()) {
                String methodStr = method.toString();
                if (methodStr.contains("throws")) {
                    methodStr = methodStr.substring(0, methodStr.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pointCutPattern.matcher(methodStr);

                if (matcher.matches()) {
                    Map<String, MyAdvice> advices = new HashMap();

                    if (StringUtils.isNotBlank(config.getAspectBefore())) {
                        advices.put("before", new MyAdvice(aspectClass.newInstance(), aspectMethods.get(config.getAspectBefore())));
                    }
                    if (StringUtils.isNotBlank(config.getAspectAfter())) {
                        advices.put("after", new MyAdvice(aspectClass.newInstance(), aspectMethods.get(config.getAspectAfter())));
                    }
                    if (StringUtils.isNotBlank(config.getAspectAfterThrow())) {
                        MyAdvice advice = new MyAdvice(aspectClass.newInstance(), aspectMethods.get(config.getAspectAfterThrow()));
                        advice.setThrowName(config.getAspectAfterThrowingName());
                        advices.put("afterThrow", advice);
                    }
                    //跟目标代理类的业务方法和Advices建立一对多个关联关系，以便在Porxy类中获得
                    methodCache.put(method, advices);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
