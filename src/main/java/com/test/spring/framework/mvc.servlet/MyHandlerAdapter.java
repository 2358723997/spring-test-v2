package com.test.spring.framework.mvc.servlet;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.test.spring.framework.annocation.MyRequestMapping;
import lombok.Data;

/**
 * MyHandlerAdapter类
 *
 * @author wangjixue
 * @date 8/9/21 12:07 AM
 */
@Data
public class MyHandlerAdapter {

    public MyModelAndView handler(HttpServletRequest req, HttpServletResponse resp, MyHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {
        //保存形参列表
        //将参数名称和参数的位置，这种关系保存起来
        Map<String, Integer> paramIndexMapping = new HashMap<>();

        //通过运行时的状态去拿到
        Annotation[][] pa = handler.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof MyRequestMapping) {
                    String paramName = ((MyRequestMapping) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        //初始化
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramterType = paramTypes[i];
            if (paramterType == HttpServletResponse.class || paramterType == HttpServletRequest.class) {
                paramIndexMapping.put(paramterType.getName(), i);
            }
        }
        //去拼接实参列表
        //http://localhost/web/query?name=Tom&Cat
        Map<String, String[]> params = req.getParameterMap();

        Object[] paramValues = new Object[paramTypes.length];

        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue())
                .replaceAll("\\[|\\]", "")
                .replaceAll("\\s+", ",");
            if (!paramIndexMapping.containsKey(param.getKey())) {continue;}

            int index = paramIndexMapping.get(param.getKey());

            //允许自定义的类型转换器Converter
            paramValues[index] = castStringValue(value, paramTypes[index]);
        }

        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        //反射调用真实方法
        Object result = handler.getMethod().invoke(handler.getController(), paramValues);
        if (result == null || result instanceof Void) {return null;}

        boolean isModelAndView = handler.getMethod().getReturnType() == MyModelAndView.class;

        if (isModelAndView) {
            return (MyModelAndView) result;
        }

        return null;
    }

    private Object castStringValue(String value, Class<?> paramType) {

        if (String.class == paramType) {
            return value;
        } else if (Integer.class == paramType) {
            return Integer.parseInt(value);
        } else if (Double.class == paramType) {
            return Double.valueOf(value);
        } else {
            if (value != null) {
                return value;
            }
            return null;
        }
    }
}
