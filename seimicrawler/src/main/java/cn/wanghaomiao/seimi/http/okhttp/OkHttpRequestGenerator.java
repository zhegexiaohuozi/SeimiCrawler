package cn.wanghaomiao.seimi.http.okhttp;

import cn.wanghaomiao.seimi.config.SeimiConfig;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.exception.SeimiProcessExcepiton;
import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.http.SeimiAgentContentType;
import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import com.alibaba.fastjson.JSON;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2016/6/26.
 */
public class OkHttpRequestGenerator {
    public static Request.Builder getOkHttpRequesBuilder(cn.wanghaomiao.seimi.struct.Request seimiReq, CrawlerModel crawlerModel){
        BaseSeimiCrawler crawler = crawlerModel.getInstance();
        Request.Builder requestBuilder = new Request.Builder();
        if (seimiReq.isUseSeimiAgent()){
            SeimiConfig config = CrawlerCache.getConfig();
            if (config==null||StringUtils.isBlank(config.getSeimiAgentHost())) {
                throw new SeimiProcessExcepiton("SeimiAgentHost is blank.");
            }
            String seimiAgentUrl = "http://" + config.getSeimiAgentHost() + (config.getSeimiAgentPort() != 80 ? (":" + config.getSeimiAgentPort()) : "") + "/doload";
            FormBody.Builder formBodyBuilder = new FormBody.Builder()
                    .add("url", seimiReq.getUrl());
            if (StringUtils.isNotBlank(crawler.proxy())){
                formBodyBuilder.add("proxy", crawler.proxy());
            }
            if (seimiReq.getSeimiAgentRenderTime() > 0){
                formBodyBuilder.add("renderTime", String.valueOf(seimiReq.getSeimiAgentRenderTime()));
            }
            if (StringUtils.isNotBlank(seimiReq.getSeimiAgentScript())){
                formBodyBuilder.add("script", seimiReq.getSeimiAgentScript());
            }
            //如果针对SeimiAgent的请求设置是否使用cookie，以针对请求的设置为准，默认使用全局设置
            if ((seimiReq.isSeimiAgentUseCookie() == null && crawlerModel.isUseCookie()) || (seimiReq.isSeimiAgentUseCookie() != null && seimiReq.isSeimiAgentUseCookie())) {
                formBodyBuilder.add("useCookie", "1");
            }
            if (seimiReq.getParams() != null && seimiReq.getParams().size() > 0) {
                formBodyBuilder.add("postParam", JSON.toJSONString(seimiReq.getParams()));
            }
            if (seimiReq.getSeimiAgentContentType().val()> SeimiAgentContentType.HTML.val()){
                formBodyBuilder.add("contentType",seimiReq.getSeimiAgentContentType().typeVal());
            }
            requestBuilder.url(seimiAgentUrl).post(formBodyBuilder.build()).build();
        }else {
            requestBuilder.url(seimiReq.getUrl());
            requestBuilder.header("User-Agent", crawlerModel.isUseCookie() ? crawlerModel.getCurrentUA() : crawler.getUserAgent())
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
            //自定义header
            if (!CollectionUtils.isEmpty(seimiReq.getHeader())) {
                for (Map.Entry<String,String> entry:seimiReq.getHeader().entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            if (HttpMethod.POST.equals(seimiReq.getHttpMethod())) {
                if (StringUtils.isNotBlank(seimiReq.getJsonBody())){
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),seimiReq.getJsonBody());
                    requestBuilder.post(requestBody);
                }else {
                    FormBody.Builder formBodyBuilder = new FormBody.Builder();
                    if (seimiReq.getParams() != null) {
                        for (Map.Entry<String, String> entry : seimiReq.getParams().entrySet()) {
                            formBodyBuilder.add(entry.getKey(), entry.getValue());
                        }
                    }
                    requestBuilder.post(formBodyBuilder.build());
                }
            } else {
                String queryStr = "";
                if (seimiReq.getParams()!=null&&!seimiReq.getParams().isEmpty()){
                    queryStr += "?";
                    for (Map.Entry<String, String> entry : seimiReq.getParams().entrySet()) {
                        queryStr= queryStr+entry.getKey()+"="+entry.getValue()+"&";
                    }
                    requestBuilder.url(seimiReq.getUrl()+queryStr);
                }
                requestBuilder.get();
            }
        }
        return requestBuilder;
    }
}
