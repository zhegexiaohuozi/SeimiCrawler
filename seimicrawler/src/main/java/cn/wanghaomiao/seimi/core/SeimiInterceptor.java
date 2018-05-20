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
package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.struct.Response;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 爬虫执行方法的通用拦截器
 *
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2015/5/28.
 */
public interface SeimiInterceptor {
    /**
     * 获取目标方法应标记的注解
     *
     * @return Annotation
     */
    public Class<? extends Annotation> getTargetAnnotationClass();

    /**
     * 当需要控制多个拦截器执行的先后顺序时可以重写这个方法
     *
     * @return 权重, 权重越大越在外层，优先拦截
     */
    public int getWeight();

    /**
     * 可以在目标方法执行之前定义一些处理逻辑
     */
    public void before(Method method, Response response);

    /**
     * 可以在目标方法执行之后定义一些处理逻辑
     */
    public void after(Method method, Response response);
}
