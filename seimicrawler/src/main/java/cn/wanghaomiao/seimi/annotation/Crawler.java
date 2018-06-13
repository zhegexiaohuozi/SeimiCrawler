/*
   Copyright 2015 Wang Haomiao<seimimaster@gmail.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package cn.wanghaomiao.seimi.annotation;

import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.def.DefaultLocalQueue;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于定义一个类为爬虫规则文件
 *
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 *         Date: 2015/5/28.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Crawler {
    /**
     * 如果需要特殊指定爬虫规则的名字，那么就设置这个就好了，默认爬虫类名
     */
    String name() default "";

    /**
     * e.g.  http://user:passwd@host:port
     * https://user:passwd@host:port
     * socket://user:passwd@host:port
     */
    String proxy() default "";

    /**
     * 指定crawler是否启用cookie
     */
    boolean useCookie() default false;

    /**
     * 抓取请求间隔延时，单位为秒
     */
    int delay() default 0;

    /**
     * 用来指定消费队列的具体实现
     */
    Class<? extends SeimiQueue> queue() default DefaultLocalQueue.class;

    /**
     * 是否启用系统级去重机制，默认启用
     */
    boolean useUnrepeated() default true;

    /**
     * Downloader实现类型，默认的Downloader实现为APACHE_HC
     */
    SeimiHttpType httpType() default SeimiHttpType.APACHE_HC;

    /**
     * 支持自定义超时时间，单位毫秒，默认15000ms
     */
    int httpTimeOut() default 15000;
}
