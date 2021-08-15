package com.test.spring.framework.mvc.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.test.spring.framework.annocation.MyController;
import com.test.spring.framework.annocation.MyRequestMapping;
import com.test.spring.framework.context.MyApplicationContext;

/**
 * MyDispatcherServlet类
 *
 * @author wangjixue
 * @date 8/8/21 11:14 PM
 */
public class MyDispatcherServlet extends HttpServlet {
    private MyApplicationContext applicationContext;

    private List<MyHandlerMapping> handlerMappings = new ArrayList<>();

    private Map<MyHandlerMapping, MyHandlerAdapter> handlerAdapters = new HashMap<>();

    private List<MyViewResolver> viewResolvers = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化Spring核心IOC容器
        applicationContext = new MyApplicationContext(config.getInitParameter("contextConfigLocation"));
        //完成了IOC、DI和MVC部分对接

        //初始化九大组件
        initStrategies(applicationContext);

    }

    private void initStrategies(MyApplicationContext context) {
        //多文件上传的组件
        //initMultipartResolver(context);
        //初始化本地语言环境
        //initLocaleResolver(context);
        //初始化模板处理器
        //initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化异常拦截器
        //initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        //initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
        //FlashMap管理器
        //initFlashMapManager(context);
    }

    /**
     * 初始化视图转换器
     *
     * @param context
     */
    private void initViewResolvers(MyApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");

        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);

        for (File file : templateRootDir.listFiles()) {
            viewResolvers.add(new MyViewResolver(templateRoot));
        }

    }

    /**
     * @param context
     */
    private void initHandlerAdapters(MyApplicationContext context) {
        for (MyHandlerMapping handlerMapping : handlerMappings) {
            handlerAdapters.put(handlerMapping, new MyHandlerAdapter());
        }
    }

    private void initHandlerMappings(MyApplicationContext context) {
        if (context.getBeanDefinitionCount() == 0) {
            return;
        }

        for (String beanName : context.getBeanDefinitionNames()) {
            Object instance = context.getBean(beanName);

            Class<?> clazz = instance.getClass();

            if (!clazz.isAnnotationPresent(MyController.class)) {
                continue;
            }

            String baseUrl = "";

            //相当于提取 class上配置的url
            if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取public的方法
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(MyRequestMapping.class)) {
                    continue;
                }
                //提取每个方法上面配置的url
                MyRequestMapping mapping = method.getAnnotation(MyRequestMapping.class);

                // //demo//query --> /demo/query
                String regex = ("/" + baseUrl + "/" + mapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new MyHandlerMapping(pattern, instance, method));

            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            //6、委派模式,根据URL去找到一个对应的Method并通过response返回
            doDispatch(req, resp);
        } catch (Exception e) {
            try {
                processDispatchResult(req, resp, new MyModelAndView("500"));
            } catch (Exception e1) {
                e1.printStackTrace();
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    /**
     * 作用：
     * 1、完成了对HandlerMapping的封装
     * 2、完成了对方法返回值的封装ModelAndView
     *
     * HandlerMapping--保存了URL映射关系
     * HandlerAdapter--动态参数适配器（方法形参）
     * ViewResolvers--视图转换器，模板引擎
     *
     * @param req
     * @param resp
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1、通过URL获得一个HandlerMapping
        MyHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req, resp, new MyModelAndView("404"));
            return;
        }
        //2、根据一个HandlerMaping获得一个HandlerAdapter
        MyHandlerAdapter adapter = getHandlerAdapter(handler);
        //3、解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        MyModelAndView modelAndView = adapter.handler(req, resp, handler);
        //4、ModelAndView变成一个ViewResolver
        processDispatchResult(req, resp, modelAndView);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, MyModelAndView modelAndView) throws Exception {
        if(modelAndView == null){
            return;
        }
        if(viewResolvers.isEmpty()){
            return;
        }
        for (MyViewResolver resolver : viewResolvers) {
           MyView view =  resolver.resolveViewName(modelAndView.getViewName());

            //直接往浏览器输出
            view.render(modelAndView.getModel(),req,resp);

            return;
        }

    }

    private MyHandlerAdapter getHandlerAdapter(MyHandlerMapping handler) {

        if(handlerAdapters.isEmpty()){
            return null;
        }

        return handlerAdapters.get(handler);
    }

    private MyHandlerMapping getHandler(HttpServletRequest req) {
        if (handlerMappings.isEmpty()) {
            return null;
        }

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        uri = uri.replaceAll(contextPath, "").replaceAll("/+", "/");
        for (MyHandlerMapping handlerMapping : handlerMappings) {

            if (!handlerMapping.getPattern().matcher(uri).matches()) {
                continue;
            }

            return handlerMapping;
        }

        return null;
    }
}
