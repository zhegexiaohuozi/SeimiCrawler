package cn.wanghaomiao.seimi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来指定一个类为消费队列的实现，只有打上这个注解才能被系统真正识别。
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/6/2.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Queue {
}
