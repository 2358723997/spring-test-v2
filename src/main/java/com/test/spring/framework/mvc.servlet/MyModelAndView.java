package com.test.spring.framework.mvc.servlet;

import java.util.Map;

/**
 * Created by Tom.
 */
public class MyModelAndView {
    private String viewName;
    private Map<String,?> model;

    public MyModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public MyModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}


