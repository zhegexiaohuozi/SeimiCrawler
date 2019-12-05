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
package cn.wanghaomiao.seimi.http.hc;

import cn.wanghaomiao.seimi.config.SeimiConfig;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.exception.SeimiProcessExcepiton;
import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.http.SeimiAgentContentType;
import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2016/4/14.
 */
public class HcRequestGenerator {
    public static RequestBuilder getHttpRequestBuilder(Request request, CrawlerModel crawlerModel) {
        RequestBuilder requestBuilder;
        BaseSeimiCrawler crawler = crawlerModel.getInstance();
        if (request.isUseSeimiAgent()) {
            SeimiConfig config = CrawlerCache.getConfig();
            if (config == null||StringUtils.isBlank(config.getSeimiAgentHost())) {
                throw new SeimiProcessExcepiton("SeimiAgentHost is blank.");
            }
            String seimiAgentUrl = "http://" + config.getSeimiAgentHost() + (config.getSeimiAgentPort() != 80 ? (":" + config.getSeimiAgentPort()) : "") + "/doload";
            requestBuilder = RequestBuilder.post().setUri(seimiAgentUrl);
            List<NameValuePair> nameValuePairList = new LinkedList<>();
            nameValuePairList.add(new BasicNameValuePair("url", request.getUrl()));
            if (StringUtils.isNotBlank(crawler.proxy())) {
                nameValuePairList.add(new BasicNameValuePair("proxy", crawler.proxy()));
            }
            if (request.getSeimiAgentRenderTime() > 0) {
                nameValuePairList.add(new BasicNameValuePair("renderTime", String.valueOf(request.getSeimiAgentRenderTime())));
            }
            if (StringUtils.isNotBlank(request.getSeimiAgentScript())) {
                nameValuePairList.add(new BasicNameValuePair("script", request.getSeimiAgentScript()));
            }
            //如果针对SeimiAgent的请求设置是否使用cookie，以针对请求的设置为准，默认使用全局设置
            if ((request.isSeimiAgentUseCookie() == null && crawlerModel.isUseCookie()) || (request.isSeimiAgentUseCookie() != null && request.isSeimiAgentUseCookie())) {
                nameValuePairList.add(new BasicNameValuePair("useCookie", "1"));
            }
            if (request.getParams() != null && request.getParams().size() > 0) {
                nameValuePairList.add(new BasicNameValuePair("postParam", JSON.toJSONString(request.getParams())));
            }
            if (request.getSeimiAgentContentType().val() > SeimiAgentContentType.HTML.val()) {
                nameValuePairList.add(new BasicNameValuePair("contentType", request.getSeimiAgentContentType().typeVal()));
            }
            requestBuilder.setEntity(new UrlEncodedFormEntity(nameValuePairList, Charset.forName("utf8")));
        } else {
            if (HttpMethod.POST.equals(request.getHttpMethod())) {
                requestBuilder = RequestBuilder.post().setUri(request.getUrl());
                if (StringUtils.isNotBlank(request.getJsonBody())){
                    requestBuilder.addHeader("Content-type","application/json; charset=utf-8");
                    requestBuilder.setEntity(new StringEntity(request.getJsonBody(), Charset.forName("UTF-8")));
                }else if (request.getParams() != null) {
                    List<NameValuePair> nameValuePairList = new LinkedList<>();
                    for (Map.Entry<String, String> entry : request.getParams().entrySet()) {
                        nameValuePairList.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
                    }
                    requestBuilder.setEntity(new UrlEncodedFormEntity(nameValuePairList, Charset.forName("utf8")));
                }
            } else {
                requestBuilder = RequestBuilder.get().setUri(request.getUrl());
                if (request.getParams() != null) {
                    for (Map.Entry<String, String> entry : request.getParams().entrySet()) {
                        requestBuilder.addParameter(entry.getKey(), entry.getValue());
                    }
                }
            }
            RequestConfig config = RequestConfig.custom().setProxy(crawlerModel.getProxy()).setCircularRedirectsAllowed(true).build();


            requestBuilder.setConfig(config).setHeader("User-Agent", crawlerModel.isUseCookie() ? crawlerModel.getCurrentUA() : crawler.getUserAgent());
            requestBuilder.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            requestBuilder.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        }
        if (!CollectionUtils.isEmpty(request.getHeader())) {
            for (Map.Entry<String, String> entry : request.getHeader().entrySet()) {
                requestBuilder.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return requestBuilder;
    }
}
