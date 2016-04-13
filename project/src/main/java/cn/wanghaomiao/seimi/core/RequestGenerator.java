package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;

import java.util.Map;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/4/14.
 */
public class RequestGenerator {
    public static RequestBuilder getHttpRequestBuilder(Request request, CrawlerModel crawlerModel){
        RequestBuilder requestBuilder;
        BaseSeimiCrawler crawler = crawlerModel.getInstance();
        if (request.isUseSeimiAgent()){
            String seimiAgentUrl = "http://"+crawler.seiAgentHost()+(crawler.seimiAgentPort()!=80?(":"+crawler.seimiAgentPort()):"")+"/doload";
            requestBuilder = RequestBuilder.post().setUri(seimiAgentUrl);
            if (StringUtils.isNotBlank(crawler.proxy())){
                requestBuilder.addParameter("proxy",crawler.proxy());
            }
            // TODO: 2016/4/14 renderTime deng
        }else {
            if (HttpMethod.POST.equals(request.getHttpMethod())){
                requestBuilder = RequestBuilder.post().setUri(request.getUrl());
            }else {
                requestBuilder = RequestBuilder.get().setUri(request.getUrl());
            }
            RequestConfig config = RequestConfig.custom().setProxy(crawlerModel.getProxy()).build();
            if (request.getParams()!=null){
                for (Map.Entry<String,String> entry:request.getParams().entrySet()){
                    requestBuilder.addParameter(entry.getKey(),entry.getValue());
                }
            }
            requestBuilder.setConfig(config).setHeader("User-Agent",crawler.getUserAgent());
        }
        return requestBuilder;
    }
}
