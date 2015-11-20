package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.annotation.Interceptor;
import cn.wanghaomiao.seimi.http.HttpClientFactory;
import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.struct.BodyType;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/8/21.
 */
public class SeimiProcessor implements Runnable {
    private SeimiQueue queue;
    private List<SeimiInterceptor> interceptors;
    private CrawlerModel crawlerModel;
    private Logger logger = LoggerFactory.getLogger(getClass());
    public SeimiProcessor(List<SeimiInterceptor> interceptors,CrawlerModel crawlerModel){
        this.queue = crawlerModel.getQueueInstance();
        this.interceptors = interceptors;
        this.crawlerModel = crawlerModel;
    }
    @Override
    public void run() {
        while (true){
            try {
                Request request = queue.bPop(crawlerModel.getCrawlerName());
                if (request==null){
                    continue;
                }
                if (crawlerModel==null){
                    logger.error("no such crawler name:'{}'",request.getCrawlerName());
                    continue;
                }
                if (request.isStop()){
                    logger.info("SeimiProcessor[{}] will stop!",Thread.currentThread().getName());
                    break;
                }
                //判断一个Request是否已经被处理过了
                if (queue.isProcessed(request)){
                    logger.info("this request has bean processed,so current request={} will be dropped!", JSON.toJSONString(request));
                    continue;
                }
                HttpClient hc;
                if (crawlerModel.isUseCookie()){
                    hc = HttpClientFactory.getHttpClient(10000,crawlerModel.getInstance().getCookieStore());
                }else {
                    hc = HttpClientFactory.getHttpClient();
                }
                RequestConfig config = RequestConfig.custom().setProxy(crawlerModel.getProxy()).build();
                RequestBuilder requestBuilder;
                if (HttpMethod.POST.equals(request.getHttpMethod())){
                    requestBuilder = RequestBuilder.post().setUri(request.getUrl());
                }else {
                    requestBuilder = RequestBuilder.get().setUri(request.getUrl());
                }
                if (request.getParams()!=null){
                    for (Map.Entry<String,String> entry:request.getParams().entrySet()){
                        requestBuilder.addParameter(entry.getKey(),entry.getValue());
                    }
                }
                requestBuilder.setConfig(config);
                HttpResponse httpResponse = hc.execute(requestBuilder.build());
                Response seimiResponse = renderResponse(httpResponse,request);
                Method requestCallback = crawlerModel.getMemberMethods().get(request.getCallBack());
                if (requestCallback==null){
                    continue;
                }
                for (SeimiInterceptor interceptor : interceptors) {
                    Interceptor interAnno = interceptor.getClass().getAnnotation(Interceptor.class);
                    if (interAnno.everyMethod()||requestCallback.isAnnotationPresent(interceptor.getTargetAnnotationClass())||crawlerModel.getClazz().isAnnotationPresent(interceptor.getTargetAnnotationClass())){
                        interceptor.before(requestCallback, seimiResponse);
                    }
                }
                if (crawlerModel.getDelay()>0){
                    TimeUnit.SECONDS.sleep(crawlerModel.getDelay());
                }
                requestCallback.invoke(crawlerModel.getInstance(),seimiResponse);
                for (SeimiInterceptor interceptor : interceptors) {
                    Interceptor interAnno = interceptor.getClass().getAnnotation(Interceptor.class);
                    if (interAnno.everyMethod()||requestCallback.isAnnotationPresent(interceptor.getTargetAnnotationClass())||crawlerModel.getClazz().isAnnotationPresent(interceptor.getTargetAnnotationClass())){
                        interceptor.after(requestCallback, seimiResponse);
                    }
                }
                logger.debug("Crawler[{}] ,url={} ,responseStatus={}",crawlerModel.getCrawlerName(),request.getUrl(),httpResponse.getStatusLine().getStatusCode());
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        }
    }
    private Response renderResponse(HttpResponse httpResponse,Request request){
        Response seimiResponse = new Response();
        HttpEntity entity = httpResponse.getEntity();
        seimiResponse.setHttpResponse(httpResponse);
        seimiResponse.setReponseEntity(entity);
        seimiResponse.setUrl(request.getUrl());
        seimiResponse.setRequest(request);
        if (entity != null) {
            Header referer = httpResponse.getFirstHeader("Referer");
            if (referer!=null){
                seimiResponse.setReferer(referer.getValue());
            }
            if (!entity.getContentType().getValue().contains("image")){
                seimiResponse.setBodyType(BodyType.TEXT);
                try {
                    seimiResponse.setContent(EntityUtils.toString(entity));
                    seimiResponse.setData(seimiResponse.getContent().getBytes());
                } catch (Exception e) {
                    logger.error("no content data");
                }
            }else {
                seimiResponse.setBodyType(BodyType.BINARY);
                try {
                    seimiResponse.setData(EntityUtils.toByteArray(entity));
                    seimiResponse.setContent(StringUtils.substringAfterLast(request.getUrl(),"/"));
                } catch (Exception e) {
                    logger.error("no data can be read from httpResponse");
                }
            }
        }
        return seimiResponse;
    }
}
