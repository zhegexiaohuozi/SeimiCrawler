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
package cn.wanghaomiao.seimi.http.hc;

import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.exception.SeimiProcessExcepiton;
import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.http.SeimiAgentContentType;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;

import java.util.Map;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/4/14.
 */
public class HcRequestGenerator {
    public static RequestBuilder getHttpRequestBuilder(Request request, CrawlerModel crawlerModel) {
        RequestBuilder requestBuilder;
        BaseSeimiCrawler crawler = crawlerModel.getInstance();
        if (request.isUseSeimiAgent()) {
            if (StringUtils.isBlank(crawler.seimiAgentHost())) {
                throw new SeimiProcessExcepiton("SeimiAgentHost is blank.");
            }
            String seimiAgentUrl = "http://" + crawler.seimiAgentHost() + (crawler.seimiAgentPort() != 80 ? (":" + crawler.seimiAgentPort()) : "") + "/doload";
            requestBuilder = RequestBuilder.post().setUri(seimiAgentUrl);
            requestBuilder.addParameter("url", request.getUrl());
            if (StringUtils.isNotBlank(crawler.proxy())) {
                requestBuilder.addParameter("proxy", crawler.proxy());
            }
            if (request.getSeimiAgentRenderTime() > 0) {
                requestBuilder.addParameter("renderTime", String.valueOf(request.getSeimiAgentRenderTime()));
            }
            if (StringUtils.isNotBlank(request.getSeimiAgentScript())) {
                requestBuilder.addParameter("script", request.getSeimiAgentScript());
            }
            //如果针对SeimiAgent的请求设置是否使用cookie，以针对请求的设置为准，默认使用全局设置
            if ((request.isSeimiAgentUseCookie() == null && crawlerModel.isUseCookie()) || (request.isSeimiAgentUseCookie() != null && request.isSeimiAgentUseCookie())) {
                requestBuilder.addParameter("useCookie", "1");
            }
            if (request.getParams() != null && request.getParams().size() > 0) {
                requestBuilder.addParameter("postParam", JSON.toJSONString(request.getParams()));
            }
            if (request.getSeimiAgentContentType().val()> SeimiAgentContentType.HTML.val()){
                requestBuilder.addParameter("contentType",request.getSeimiAgentContentType().typeVal());
            }
        } else {
            if (HttpMethod.POST.equals(request.getHttpMethod())) {
                requestBuilder = RequestBuilder.post().setUri(request.getUrl());
            } else {
                requestBuilder = RequestBuilder.get().setUri(request.getUrl());
            }
            RequestConfig config = RequestConfig.custom().setProxy(crawlerModel.getProxy()).setCircularRedirectsAllowed(true).build();

            if (request.getParams() != null) {
                for (Map.Entry<String, String> entry : request.getParams().entrySet()) {
                    requestBuilder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            requestBuilder.setConfig(config).setHeader("User-Agent", crawlerModel.isUseCookie() ? crawlerModel.getCurrentUA() : crawler.getUserAgent());
            requestBuilder.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            requestBuilder.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        }
        return requestBuilder;
    }
}
