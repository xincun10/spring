package spring.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)//注解使用在类上面
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnjoyController {
	String value() default "";
}
