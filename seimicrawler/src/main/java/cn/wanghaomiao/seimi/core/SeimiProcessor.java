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

import cn.wanghaomiao.seimi.annotation.Interceptor;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.http.hc.HcDownloader;
import cn.wanghaomiao.seimi.http.okhttp.OkHttpDownloader;
import cn.wanghaomiao.seimi.struct.BodyType;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.struct.SeimiCallbackFunc;
import cn.wanghaomiao.seimi.utils.StructValidator;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2015/8/21.
 */
public class SeimiProcessor implements Runnable {
    private SeimiQueue queue;
    private List<SeimiInterceptor> interceptors;
    private CrawlerModel crawlerModel;
    private BaseSeimiCrawler crawler;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public SeimiProcessor(List<SeimiInterceptor> interceptors, CrawlerModel crawlerModel) {
        this.queue = crawlerModel.getQueueInstance();
        this.interceptors = interceptors;
        this.crawlerModel = crawlerModel;
        this.crawler = crawlerModel.getInstance();
    }

    private Pattern metaRefresh = Pattern.compile("<(?:META|meta|Meta)\\s+(?:HTTP-EQUIV|http-equiv)\\s*=\\s*\"refresh\".*(?:url|URL)=(\\S*)\".*/?>");

    @Override
    public void run() {
        while (true) {
            Request request = null;
            try {
                request = queue.bPop(crawlerModel.getCrawlerName());
                if (request == null) {
                    continue;
                }
                if (crawlerModel == null) {
                    logger.error("No such crawler name:'{}'", request.getCrawlerName());
                    continue;
                }
                if (request.isStop()) {
                    logger.info("SeimiProcessor[{}] will stop!", Thread.currentThread().getName());
                    break;
                }
                //对请求开始校验
                if (!StructValidator.validateAnno(request)) {
                    logger.warn("Request={} is illegal", JSON.toJSONString(request));
                    continue;
                }
                if (!StructValidator.validateAllowRules(crawler.allowRules(), request.getUrl())) {
                    logger.warn("Request={} will be dropped by allowRules=[{}]", JSON.toJSONString(request), StringUtils.join(crawler.allowRules(), ","));
                    continue;
                }
                if (StructValidator.validateDenyRules(crawler.denyRules(), request.getUrl())) {
                    logger.warn("Request={} will be dropped by denyRules=[{}]", JSON.toJSONString(request), StringUtils.join(crawler.denyRules(), ","));
                    continue;
                }
                //异常请求重试次数超过最大重试次数三次后，直接放弃处理
                if (request.getCurrentReqCount() >= request.getMaxReqCount()+3) {
                    continue;
                }
                //如果启用了系统级去重机制并且为首次处理则判断一个Request是否已经被处理过了
                if (!request.isSkipDuplicateFilter() && crawlerModel.isUseUnrepeated() && queue.isProcessed(request) && request.getCurrentReqCount() == 0) {
                    logger.info("This request has bean processed,so current request={} will be dropped!", JSON.toJSONString(request));
                    continue;
                }
                queue.addProcessed(request);

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
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                if (request == null) {
                    continue;
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
        SeimiCallbackFunc<Response> requestCallback = request.getCallBackFunc();
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
        requestCallback.accept(seimiResponse);
        for (SeimiInterceptor interceptor : interceptors) {
            Interceptor interAnno = interceptor.getClass().getAnnotation(Interceptor.class);
            if (interAnno.everyMethod() ) {
                interceptor.after(null, seimiResponse);
            }
        }
    }
}
