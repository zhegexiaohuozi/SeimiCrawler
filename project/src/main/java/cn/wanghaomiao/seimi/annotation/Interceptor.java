package cn.wanghaomiao.seimi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个拦截器，用于解析引擎识别
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/5/28.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Interceptor {
    /**
     * 用于判断是否每个拦截到的方法都进行拦截并执行
     * @return
     */
    boolean everyMethod() default false;
}
