package spring.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})//注解使用在参数
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnjoyRequestParam {
	String value() default "";
}
