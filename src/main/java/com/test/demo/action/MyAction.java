package com.test.demo.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.test.demo.service.ModifyService;
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
@MyRequestMapping("/web")
public class MyAction {

	@MyAutowired
	QueryService queryService;
	@MyAutowired
	ModifyService modifyService;

	@MyRequestMapping("/query.json")
	public MyModelAndView query(HttpServletRequest request, HttpServletResponse response,
								@MyRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@MyRequestMapping("/add*.json")
	public MyModelAndView add(HttpServletRequest request,HttpServletResponse response,
			   @MyRequestParam("name") String name,@MyRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		return out(response,result);
	}
	
	@MyRequestMapping("/remove.json")
	public MyModelAndView remove(HttpServletRequest request, HttpServletResponse response,
								 @MyRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@MyRequestMapping("/edit.json")
	public MyModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@MyRequestParam("id") Integer id,
			@MyRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private MyModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
