package com.test.spring.framework.context;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.test.spring.framework.annocation.MyAutowired;
import com.test.spring.framework.annocation.MyController;
import com.test.spring.framework.annocation.MyService;
import com.test.spring.framework.aop.config.MyAopConfig;
import com.test.spring.framework.aop.proxy.MyJdkDynamicAopProxy;
import com.test.spring.framework.aop.support.MyAdvisedSupport;
import com.test.spring.framework.beans.MyBeanWrapper;
import com.test.spring.framework.beans.config.MyBeanDefinition;
import com.test.spring.framework.beans.support.MyBeanDefinitionReader;

/**
 * MyApplicationContext类
 *
 * @author wangjixue
 * @date 8/8/21 8:22 PM
 */
public class MyApplicationContext {
    private MyBeanDefinitionReader reader;

    private Map<String, MyBeanDefinition> beanDefinitionMap = new HashMap<>();
    //IOC容器
    private Map<String, MyBeanWrapper> factoryBeanInstanceCache = new HashMap<>();

    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    public MyApplicationContext(String... configLocations) {
        try {
            //1.加载配置文件
            reader = new MyBeanDefinitionReader(configLocations);
            //2.将配置文件解析为BeanDefinition
            List<MyBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
            //3.将BeanDefinition缓存起来
            doRegistBeanDefinition(beanDefinitions);
            //4.完成依赖注入
            doAutowrited();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowrited() {
        //调用getBean()方法
        //这一步，所有的Bean并没有真正的实例化，还只是配置阶段
        for (Map.Entry<String, MyBeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            getBean(beanName);
        }
    }

    private void doRegistBeanDefinition(List<MyBeanDefinition> beanDefinitions) throws Exception {
        for (MyBeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The" + beanDefinition.getFactoryBeanName() + " is exist.");
            }

            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    public Object getBean(Class clazz) {
        return getBean(clazz.getName());
    }

    //Bean的实例化，DI是从而这个方法开始的
    public Object getBean(String beanName) {
        //1、获取BeanDefinition配置信息
        MyBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        //2、反射实例化newInstance();
        Object instance = instantiateBean(beanName, beanDefinition);
        //3、封装成BeanWrapper
        MyBeanWrapper beanWrapper = new MyBeanWrapper(instance);
        //4、保存到IoC容器
        factoryBeanInstanceCache.put(beanName, beanWrapper);
        //5、执行依赖注入
        populateBean(beanName, beanDefinition, beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    /**
     * 注入Bean对象
     *
     * 问题：循环依赖如何解决？
     * A{ B b}
     * B{ A b}
     * 用两个缓存，循环两次
     * 1、把第一次读取结果为空的BeanDefinition存到第一个缓存
     * 2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值
     *
     * @param beanName
     * @param beanDefinition
     * @param beanWrapper
     */
    private void populateBean(String beanName, MyBeanDefinition beanDefinition, MyBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrapperInstance();
        Class<?> clazz = beanWrapper.getWrappedClass();
        //在Spring中@Component
        if (!(clazz.isAnnotationPresent(MyController.class) || clazz.isAnnotationPresent(MyService.class))) {
            return;
        }

        //把所有的包括private/protected/default/public 修饰字段都取出来
        for (Field field : clazz.getFields()) {

            if (!field.isAnnotationPresent(MyAutowired.class)) {
                continue;
            }

            MyAutowired autowired = field.getAnnotation(MyAutowired.class);

            String autowiredBeanName = autowired.value().trim();
            //如果用户没有自定义的beanName，就默认根据类型注入
            if ("".equals(autowiredBeanName)) {
                //field.getType().getName() 获取字段的类型
                autowiredBeanName = field.getType().getName();
            }

            //暴力访问
            field.setAccessible(true);

            try {
                if (factoryBeanInstanceCache.containsKey(autowiredBeanName)) {
                    continue;
                }
                //ioc.get(beanName) 相当于通过接口的全名拿到接口的实现的实例
                field.set(instance, factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

        }
    }

    /**
     * 创建真正的实例对象
     *
     * @param beanName
     * @param beanDefinition
     * @return
     */
    private Object instantiateBean(String beanName, MyBeanDefinition beanDefinition) {
        Object instance = null;
        try {
            if (factoryBeanObjectCache.containsKey(beanName)) {
                instance = factoryBeanInstanceCache.get(beanName);
            } else {
                //默认的类名首字母小写
                Class<?> beanClazz = Class.forName(beanDefinition.getBeanClassName());

                instance = beanClazz.newInstance();
                //==================AOP开始=========================
                //如果满足条件，就直接返回Proxy对象
                //1、加载AOP的配置文件
                MyAdvisedSupport config = instantionAopConfig(beanDefinition);
                config.setTargetClass(beanClazz);
                config.setTarget(instance);
                //判断规则，要不要生成代理类，如果要就覆盖原生对象
                if(config.pointCutMath()){
                    instance = new MyJdkDynamicAopProxy(config).getProxy();
                }
                //===================AOP结束========================

                //如果不要就不做任何处理，返回原生对象
                factoryBeanObjectCache.put(beanName, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private MyAdvisedSupport instantionAopConfig(MyBeanDefinition beanDefinition) {
        MyAopConfig config = new MyAopConfig();
        config.setPointCut(reader.getConfig().getProperty("pointCut"));
        config.setAspectAfter(reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(reader.getConfig().getProperty("aspectAfterThrowingName"));
        config.setAspectBefore(reader.getConfig().getProperty("aspectBefore"));
        config.setAspectClass(reader.getConfig().getProperty("aspectClass"));
        return new MyAdvisedSupport(config);
    }

    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[this.getBeanDefinitionCount()]);
    }

    public Properties getConfig() {
        return reader.getConfig();
    }
}
