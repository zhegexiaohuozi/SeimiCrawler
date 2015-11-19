package cn.wanghaomiao.seimi.annotation;

import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.def.DefaultLocalQueue;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于定义一个类为爬虫规则文件
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/5/28.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Crawler {
    /**
     * 如果需要特殊指定爬虫规则的名字，那么就设置这个就好了，默认爬虫类名
     */
    String name() default "";

    /**
     * e.g.  http://user:passwd@host:port
     *       https://user:passwd@host:port
     *       socket://user:passwd@host:port
     */
    String proxy() default "";

    /**
     * 指定crawler是否启用cookie
     */
    boolean useCookie() default false;

    /**
     * 用来指定消费队列的具体实现
     */
    Class<? extends SeimiQueue> queue() default DefaultLocalQueue.class;
}
