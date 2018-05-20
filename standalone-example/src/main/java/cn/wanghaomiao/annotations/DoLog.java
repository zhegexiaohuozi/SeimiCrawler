package cn.wanghaomiao.annotations;

import java.lang.annotation.*;

/**
 * demo演示
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/4.
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoLog {
}
