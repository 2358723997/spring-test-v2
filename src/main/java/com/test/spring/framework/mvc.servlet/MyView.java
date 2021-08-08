package com.test.spring.framework.mvc.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Data;

/**
 * MyView类
 *
 * @author wangjixue
 * @date 8/9/21 12:35 AM
 */
@Data
public class MyView {
    private File viewFile;

    public MyView(File templateFile) {
        viewFile = templateFile;
    }

    public void render(Map<String, ?> model, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        StringBuffer buffer = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(viewFile, "r");

        String line = null;

        while ((line = ra.readLine()) != null){
            line = new String(line.getBytes("ISO-8859-1"),"UTF-8");
            Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}",Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()){
                String paramName = matcher.group();
                paramName = paramName.replaceAll("￥\\{|\\}", "");
                Object paramValue = model.get(paramName);
                line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                matcher = pattern.matcher(line);
            }

            buffer.append(line);
        }
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(buffer.toString());
    }

    /**
     * 处理特殊字符，防止HTML格式不正确
     *
     * @param paramValue
     * @return
     */
    private String makeStringForRegExp(String paramValue) {
        return paramValue.replace("\\", "\\\\").replace("*", "\\*")
            .replace("+", "\\+").replace("|", "\\|")
            .replace("{", "\\{").replace("}", "\\}")
            .replace("(", "\\(").replace(")", "\\)")
            .replace("^", "\\^").replace("$", "\\$")
            .replace("[", "\\[").replace("]", "\\]")
            .replace("?", "\\?").replace(",", "\\,")
            .replace(".", "\\.").replace("&", "\\&");
    }
}
