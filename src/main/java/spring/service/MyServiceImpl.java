package spring.service;

import spring.anno.EnjoyService;

@EnjoyService("MyServiceImpl") //相当于map.put(MyServiceImpl, new MyServiceImpl())
public class MyServiceImpl implements MyService{

	public String query(String name, String age) {
		// TODO Auto-generated method stub
		return "name="+name+";age="+age;
	}

}
