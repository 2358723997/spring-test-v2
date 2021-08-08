package com.test.spring.framework.beans.support;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.test.spring.framework.beans.config.MyBeanDefinition;

/**
 * MyBeanDefinitionReader类
 *
 * @author wangjixue
 * @date 8/8/21 8:26 PM
 */
public class MyBeanDefinitionReader {
    private static final String DEFAULT_SCAN_PACKAGE = "scanPackage";

    private Properties configProperties;

    //保存扫描的结果
    private List<String> regitryBeanClassList = new ArrayList<String>();

    public MyBeanDefinitionReader(String... configLocations) {
        //1.加载配置项
        loadConfig(configLocations[0]);
        //扫描配置文件中的配置的相关的类
        doScanner(configProperties.getProperty(DEFAULT_SCAN_PACKAGE));
    }

    /**
     * 扫描
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        //jar 、 war 、zip 、rar
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));

        File classPath = new File(url.getFile());

        //当成是一个ClassPath文件夹
        File[] files = classPath.listFiles();
        for (File file : files) {

            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else {
                if(!file.getName().endsWith(".class")){
                    continue;
                }
                String className = scanPackage+"."+file.getName().replace(".class","");
                regitryBeanClassList.add(className);
            }
        }

    }

    /**
     * 将配置文件加载到properties对象中
     *
     * @param configLocation
     */
    private void loadConfig(String configLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation.replaceAll("classpath:", ""));
        try {
            configProperties.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 加载BeanDefinition
     *
     * @return
     */
    public List<MyBeanDefinition> loadBeanDefinitions() {
        List<MyBeanDefinition> beanDefinitionList = new ArrayList<MyBeanDefinition>();
        for (String className : regitryBeanClassList) {
            try {
                Class<?> beanClass = Class.forName(className);
                //保存类对应的ClassName（全类名） 和 beanName

                //1、默认是类名首字母小写
                beanDefinitionList.add(createBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));

                //2、自定义
                //3、接口注入
                for (Class<?> interfaces : beanClass.getInterfaces()) {
                    beanDefinitionList.add(createBeanDefinition(interfaces.getName(),beanClass.getName()));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return beanDefinitionList;
    }

    private MyBeanDefinition createBeanDefinition(String beanName, String beanClassName) {
        MyBeanDefinition beanDefinition = new MyBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] +=32;
        return chars.toString();
    }

    public Properties getConfig() {
        return configProperties;
    }
}
