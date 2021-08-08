package com.test.demo.action;

import java.util.HashMap;
import java.util.Map;

import com.test.demo.service.QueryService;
import com.test.spring.framework.annocation.MyAutowired;
import com.test.spring.framework.annocation.MyController;
import com.test.spring.framework.annocation.MyRequestMapping;
import com.test.spring.framework.annocation.MyRequestParam;
import com.test.spring.framework.mvc.servlet.MyModelAndView;

/**
 * 公布接口url
 * @author Tom
 *
 */
@MyController
@MyRequestMapping("/")
public class PageAction {

    @MyAutowired
    QueryService queryService;

    @MyRequestMapping("/first.html")
    public MyModelAndView query(@MyRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new MyModelAndView("first.html",model);
    }

}
