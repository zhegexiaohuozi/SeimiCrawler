package cn.wanghaomiao.seimi.annotation;

import cn.wanghaomiao.seimi.spring.boot.SeimiCrawlerBeanRegistar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2018/5/8.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(SeimiCrawlerBeanRegistar.class)
public @interface EnableSeimiCrawler {
}
