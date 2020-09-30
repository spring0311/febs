package cc.mrbird.febs.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MethodAnnotation.java详细代码
 *
 * @author MrBird
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerEndpoint {
    //操作 默认为空
    String operation() default "";

    //异常消息
    String exceptionMessage() default "系统内部异常";
}
