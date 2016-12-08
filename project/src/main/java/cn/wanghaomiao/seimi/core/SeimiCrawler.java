/*
   Copyright 2015 Wang Haomiao<et.tw@163.com>

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


import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;

import java.util.List;

/**
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/5/28.
 */
public interface SeimiCrawler {

    String getUserAgent();

    /**
     * 可以自定义返回随机的代理
     * @return
     */
    String proxy();

    /**
     * 设置起始url
     * @return
     */
    String[] startUrls();

    /**
     * 起始的Request，可以应对更复杂的情况，当<code>String[] startUrls();</code>无法满足需求的情况下推荐使用
     * @return
     */
    List<Request> startRequests();

    /**
     * 用于设置允许的请求URL匹配规则
     * @return 白名单规则正则表达式列表
     */
    String[] allowRules();

    /**
     * 用于设置要放弃访问的请求URL匹配规则
     * @return 黑名单规则正则表达式列表
     */
    String[] denyRules();
    /**
     * 针对startUrl生成首批的response回调这个初始接口
     * @param response
     * @return
     */
    void start(Response response);

    /**
     * 当一个请求处理异常次数超过开发者所设置或是默认设置的最大重新处理次数时会调用该方法记录异常请求
     * @param request
     */
    void handleErrorRequest(Request request);

    /**
     * 设置SeimiAgent的主机地址，如 seimi.wanghaomiao.cn or 10.10.15.211
     * @return
     */
    String seimiAgentHost();

    /**
     * seimiAgent监听端口
     * @return
     */
    int seimiAgentPort();
}
