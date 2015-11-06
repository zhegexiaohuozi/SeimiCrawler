package cn.wanghaomiao.seimi.annotation;

import java.lang.annotation.*;

/**
 * 定义一个用于提取数据到指定字段的xpath路径
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/5/28.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Xpath {
    /**
     * JsoupXpath支持的XPath语句，JsoupXpath默认支持标准的XPath语句，但也支持一些很有帮助的扩展，具体可以参见：https://github.com/zhegexiaohuozi/JsoupXpath
     * @return
     */
    String value();
}
