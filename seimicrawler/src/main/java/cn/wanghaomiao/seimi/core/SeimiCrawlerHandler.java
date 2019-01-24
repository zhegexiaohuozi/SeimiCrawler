package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.annotation.Interceptor;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.http.hc.HcDownloader;
import cn.wanghaomiao.seimi.http.okhttp.OkHttpDownloader;
import cn.wanghaomiao.seimi.struct.BodyType;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.utils.StructValidator;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author github.com/zzldnl
 * @since 2019/01/24.
 */
public class SeimiCrawlerHandler implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Request request;
	private CrawlerModel crawlerModel;
	private BaseSeimiCrawler crawler;
	private SeimiQueue queue;
	private List<SeimiInterceptor> interceptors;
	
	public SeimiCrawlerHandler(Request request, CrawlerModel crawlerModel, List<SeimiInterceptor> interceptors) {
		// TODO Auto-generated constructor stub
		this.request=request;
		this.crawlerModel=crawlerModel;
		this.crawler = crawlerModel.getInstance();
		this.queue = crawlerModel.getQueueInstance();
		this.interceptors = interceptors;
	}
	
	private Pattern metaRefresh = Pattern.compile("<(?:META|meta|Meta)\\s+(?:HTTP-EQUIV|http-equiv)\\s*=\\s*\"refresh\".*(?:url|URL)=(\\S*)\".*/?>");

	@Override
	public void run() {
        try {
            //对请求开始校验
            if (!StructValidator.validateAnno(request)) {
                logger.warn("Request={} is illegal", JSON.toJSONString(request));
                return;
            }
            if (!StructValidator.validateAllowRules(crawler.allowRules(), request.getUrl())) {
                logger.warn("Request={} will be dropped by allowRules=[{}]", JSON.toJSONString(request), StringUtils.join(crawler.allowRules(), ","));
                return;
            }
            if (StructValidator.validateDenyRules(crawler.denyRules(), request.getUrl())) {
                logger.warn("Request={} will be dropped by denyRules=[{}]", JSON.toJSONString(request), StringUtils.join(crawler.denyRules(), ","));
                return;
            }
            //异常请求重试次数超过最大重试次数三次后，直接放弃处理
            if (request.getCurrentReqCount() >= request.getMaxReqCount()+3) {
                return;
            }

            SeimiDownloader downloader;
            if (SeimiHttpType.APACHE_HC.val() == crawlerModel.getSeimiHttpType().val()) {
                downloader = new HcDownloader(crawlerModel);
            } else {
                downloader = new OkHttpDownloader(crawlerModel);
            }

            Response seimiResponse = downloader.process(request);
            if (StringUtils.isNotBlank(seimiResponse.getContent()) && BodyType.TEXT.equals(seimiResponse.getBodyType())) {
                Matcher mm = metaRefresh.matcher(seimiResponse.getContent());
                int refreshCount = 0;
                while (!request.isUseSeimiAgent() && mm.find() && refreshCount < 3) {
                    String nextUrl = mm.group(1).replaceAll("'", "");
                    seimiResponse = downloader.metaRefresh(nextUrl);
                    mm = metaRefresh.matcher(seimiResponse.getContent());
                    refreshCount += 1;
                }
            }
            //处理回调函数
            if (!request.isLambdaCb()){
                doCallback(request, seimiResponse);
            }else {
                doLambdaCallback(request, seimiResponse);
            }
            logger.debug("Crawler[{}] ,url={} ,responseStatus={}", crawlerModel.getCrawlerName(), request.getUrl(), downloader.statusCode());
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            if (request == null) {
                return;
            }
            if (request.getCurrentReqCount() < request.getMaxReqCount()) {
                request.incrReqCount();
                queue.push(request);
                logger.info("Request process error,req will go into queue again,url={},maxReqCount={},currentReqCount={}", request.getUrl(), request.getMaxReqCount(), request.getCurrentReqCount());
            } else if (request.getCurrentReqCount() >= request.getMaxReqCount() && request.getMaxReqCount() > 0) {
                crawler.handleErrorRequest(request);
            }

        }

	}

    private void doCallback(Request request, Response seimiResponse) throws Exception {

        Method requestCallback = crawlerModel.getMemberMethods().get(request.getCallBack());
        if (requestCallback == null) {
            logger.info("can not find callback function");
            return;
        }
        for (SeimiInterceptor interceptor : interceptors) {
            Interceptor interAnno = interceptor.getClass().getAnnotation(Interceptor.class);
            if (interAnno.everyMethod() || requestCallback.isAnnotationPresent(interceptor.getTargetAnnotationClass()) || crawlerModel.getClazz().isAnnotationPresent(interceptor.getTargetAnnotationClass())) {
                interceptor.before(requestCallback, seimiResponse);
            }
        }
        if (crawlerModel.getDelay() > 0) {
            TimeUnit.SECONDS.sleep(crawlerModel.getDelay());
        }
        requestCallback.invoke(crawlerModel.getInstance(), seimiResponse);

        for (SeimiInterceptor interceptor : interceptors) {
            Interceptor interAnno = interceptor.getClass().getAnnotation(Interceptor.class);
            if (interAnno.everyMethod() || requestCallback.isAnnotationPresent(interceptor.getTargetAnnotationClass()) || crawlerModel.getClazz().isAnnotationPresent(interceptor.getTargetAnnotationClass())) {
                interceptor.after(requestCallback, seimiResponse);
            }
        }
    }

    private void doLambdaCallback(Request request, Response seimiResponse) throws Exception {
        Request.SeimiCallbackFunc<SeimiCrawler,Response> requestCallback = request.getCallBackFunc();
        if (requestCallback == null) {
            logger.info("can not find callback function");
            return;
        }
        for (SeimiInterceptor interceptor : interceptors) {
            Interceptor interAnno = interceptor.getClass().getAnnotation(Interceptor.class);
            if (interAnno.everyMethod()) {
                interceptor.before(null, seimiResponse);
            }
        }
        if (crawlerModel.getDelay() > 0) {
            TimeUnit.SECONDS.sleep(crawlerModel.getDelay());
        }
        requestCallback.call(crawler,seimiResponse);
        for (SeimiInterceptor interceptor : interceptors) {
            Interceptor interAnno = interceptor.getClass().getAnnotation(Interceptor.class);
            if (interAnno.everyMethod() ) {
                interceptor.after(null, seimiResponse);
            }
        }
    }
	
	
}
