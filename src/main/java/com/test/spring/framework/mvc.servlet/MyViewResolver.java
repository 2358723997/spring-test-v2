package com.test.spring.framework.mvc.servlet;

import java.io.File;

import lombok.Data;

/**
 * MyViewResolver类
 *
 * @author wangjixue
 * @date 8/9/21 12:16 AM
 */
@Data
public class MyViewResolver {

    private static final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File tempateRootDir;

    public MyViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        tempateRootDir = new File(templateRootPath);
    }

    public MyView resolveViewName(String viewName) {
        if(viewName == null || "".equals(viewName)){
            return null;
        }
        viewName =viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)?viewName:(viewName+DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((tempateRootDir.getPath()+"/"+viewName).replaceAll("/+","/"));
        return new MyView(templateFile);
    }
}
