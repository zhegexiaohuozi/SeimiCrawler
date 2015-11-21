package cn.wanghaomiao.seimi.annotation.validate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于校验一个字段是否为空
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/21.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotNull {
}
