package spring.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import spring.anno.EnjoyAutowried;
import spring.anno.EnjoyController;
import spring.anno.EnjoyRequestMapping;
import spring.anno.EnjoyRequestParam;
import spring.anno.EnjoyService;
import spring.controller.MyController;

public class DispatcherServlet extends HttpServlet{
	//保存所有的classNames
	List<String> classNames = new ArrayList<>();
	//存储beans
	Map<String, Object> beans = new HashMap<>();
	//存储url和对象的映射关系
	Map<String, Object> handlerMap = new HashMap<>();
	
	//IOC是在项目启动时就加载
	@Override
	public void init(ServletConfig config) throws ServletException {
		//先得把所有的class进行收集...
		scanPackage("spring");
		//IOC容器map.put(key, instance)
		//对扫描出来的类进行实例化...
		doInstance();//beans(key, instance)
		
		for(Map.Entry<String, Object> entry:beans.entrySet())
		{
			System.out.println(entry.getKey()+":"+entry.getValue());
		}
		//依赖注入
		doAutowired();
		//建立一个URL与method的映射关系
		UrlMapping();
		
	}

	private void UrlMapping() {
		// 建立一个URL与method的映射关系
		for(Map.Entry<String, Object> entry:beans.entrySet())
		{
			Object instance = entry.getValue();
			//获取类的对象
			Class<?> clazz = instance.getClass();
			
			//先拿到类上面的EnjoyRequestMapping，再拿到方法上的
			if(clazz.isAnnotationPresent(EnjoyController.class))
			{
				EnjoyRequestMapping requestMapping = clazz.getAnnotation(EnjoyRequestMapping.class);
				String classPath = requestMapping.value();//得到/value
				
				Method[] methods = clazz.getMethods();
				//拿到方法上的requestMapping
				for(Method method:methods)
				{
					if(method.isAnnotationPresent(EnjoyRequestMapping.class))
					{
						EnjoyRequestMapping url = method.getAnnotation(EnjoyRequestMapping.class);
						String methodPath = url.value();//得到/query
						//拼接路径，存储映射关系
						handlerMap.put(classPath+methodPath, method);
					}
					else
					{
						continue;
					}
				}
			}
		}
		
	}

	private void doAutowired() {
		// 依赖注入
		if(beans.entrySet().size()<=0)
		{
			System.out.println("没有实例化一个类");
			return;
		}
		//beans遍历
		for(Map.Entry<String, Object> entry:beans.entrySet())
		{
			Object instance = entry.getValue();//获取bean实例Controller bean
			Class<?> clazz = instance.getClass();//class来获取类里面有哪些方法、属性....
			//判断是否声明了EnjoyController注解
			if(clazz.isAnnotationPresent(EnjoyController.class))
			{
				//获取类里面有哪些属性
				Field[] fields = clazz.getDeclaredFields();
				//判断哪个属性上面有autowired
				for(Field field:fields)
				{
					if(field.isAnnotationPresent(EnjoyAutowried.class))
					{
						EnjoyAutowried auto = field.getAnnotation(EnjoyAutowried.class);
						String key = auto.value();//获取到MyServiceImpl key
						field.setAccessible(true);//因为controller里面MyServiceImpl的访问权限是private
						//所以要打开访问权限，否则没办法注入
						try {
							field.set(instance, beans.get(key));
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
					{
						continue;
					}
				}
			}else {
				continue;
			}
		}
	}

	private void doInstance() {
		//对扫描出来的类进行实例化
		if(classNames.size()<=0)
		{
			System.out.println("找不到一个class文件！");
			return;
		}
		//遍历所有刚被扫描到的class文件全类名路径
		for(String className:classNames)
		{
			String cn = className.replace(".class", "");
			try {
				//加载类,处理声明在class类上面的
				Class<?> clazz = Class.forName(cn);
				//判断是否声明了EnjoyController注解
				if(clazz.isAnnotationPresent(EnjoyController.class))
				{				
					Object instance = clazz.newInstance(); 
					//ioc map:map.put(key, instance)
					EnjoyRequestMapping requestMapping = clazz.getAnnotation(EnjoyRequestMapping.class);
					String key = requestMapping.value();
					//将键值对放入map里面
					beans.put(key, instance);
				}
				else if(clazz.isAnnotationPresent(EnjoyService.class))
				{
					//判断是否声明了EnjoyService注解
					Object instance = clazz.newInstance(); 
					//ioc map:map.put(key, instance)
					EnjoyService service = clazz.getAnnotation(EnjoyService.class);
					String key = service.value();
					//将键值对放入map里面
					beans.put(key, instance);//service类型的bean放到beans
				}
				else
				{
					continue;
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void scanPackage(String basePackage) {
		//扫描包里面的类进行实例化
		//com.xxy==>com/xxy
		URL url = this.getClass().getClassLoader().
				getResource("/"+basePackage.replaceAll("\\.", "/"));
		String fileStr = url.getFile();
		File file = new File(fileStr);
		
		String[] filesStr = file.list();
		for(String path:filesStr)
		{
			File filePath = new File(fileStr+path);
			if(filePath.isDirectory())//如果扫描的路径是目录的话
			{
				scanPackage(basePackage+"."+path);//spring.controller
			}
			else
			{
				//不是路径的话就应该是类
				//使用map来保存class路径
				classNames.add(basePackage+"."+filePath.getName());
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uri = req.getRequestURI();//spring/hello/query
		//去掉前面的项目名
		String context = req.getContextPath();
		String path = uri.replace(context, "");//hello/query
		
		Method method = (Method) handlerMap.get(path);
		MyController instance = (MyController) beans.get("/"+path.split("/")[1]);
		
		Object args[] = hand(req, resp, method);
		try {
			method.invoke(instance, args);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//解析方法里面的所有参数
	private static Object[] hand(HttpServletRequest req, HttpServletResponse resp, 
			Method method)
	{
		//拿到当前待执行的方法有哪些参数
		Class<?>[] paramClazzs = method.getParameterTypes();
		//根据参数的个数，new 一个参数的数组，将方法里的所有参数赋值到args里面
		Object[] args = new Object[paramClazzs.length];
		
		int args_i = 0;
		int index = 0;
		for(Class<?> paramClazz:paramClazzs)
		{
			if(ServletRequest.class.isAssignableFrom(paramClazz))
			{
				args[args_i++] = req;
			}
			if(ServletResponse.class.isAssignableFrom(paramClazz))
			{
				args[args_i++] = resp;
			}
			//从0到3判断有没有RequestParam注解，很明显paramClazz为0和1时，不是
			//当为2和3时为@RequestParam，需要解析
			Annotation[] paramAns = method.getParameterAnnotations()[index];
			if(paramAns.length > 0)
			{
				for(Annotation paramAn: paramAns)
				{
					if(EnjoyRequestParam.class.isAssignableFrom(paramAn.getClass()))
					{
						EnjoyRequestParam rp = (EnjoyRequestParam) paramAn;
						//找到注解里的name和age
						args[args_i++] = req.getParameter(rp.value());
					}
				}
			}
			index++;
		}
		return args;
	}
	
}
