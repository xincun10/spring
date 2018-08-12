package spring.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spring.anno.EnjoyAutowried;
import spring.anno.EnjoyController;
import spring.anno.EnjoyRequestMapping;
import spring.anno.EnjoyRequestParam;
import spring.service.MyService;

@EnjoyController
@EnjoyRequestMapping("/hello")
public class MyController {

	@EnjoyAutowried("MyServiceImpl")//map.get("MyServiceImpl")
	private MyService myService;
	
	@EnjoyRequestMapping("/query")
	public void query(HttpServletRequest request, HttpServletResponse response,
			@EnjoyRequestParam("name") String name, @EnjoyRequestParam("age") String age) throws IOException
	{
		PrintWriter out = response.getWriter();
		String result = myService.query(name, age);
		out.write(result);
	}
}
