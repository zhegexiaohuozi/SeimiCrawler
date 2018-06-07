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
package cn.wanghaomiao.seimi.struct;


import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.http.SeimiHttpType;
import cn.wanghaomiao.seimi.http.okhttp.CookiesManager;
import cn.wanghaomiao.seimi.utils.GenericUtils;
import cn.wanghaomiao.seimi.utils.StrFormatUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 * Date: 2015/7/17.
 */
public class CrawlerModel {
    private ApplicationContext context;
    private BaseSeimiCrawler instance;
    private Class<? extends BaseSeimiCrawler> clazz;
    private SeimiQueue queueInstance;
    private Class<? extends SeimiQueue> queueClass;
    private Map<String, Method> memberMethods;
    private Map<Integer, String> referenceToMethod = new HashMap<>();
    private String crawlerName;
    private HttpHost proxy;
    private boolean useCookie = false;
    private String currentUA;
    private boolean useUnrepeated = true;
    private int delay = 0;
    private SeimiHttpType seimiHttpType;
    private int httpTimeOut;
    private CookieStore aphcCookieStore = new BasicCookieStore();
    private CookiesManager okHttpCookiesManager = new CookiesManager();
    private Logger logger = LoggerFactory.getLogger(CrawlerModel.class);

    public CrawlerModel(Class<? extends BaseSeimiCrawler> cls, ApplicationContext applicationContext) {
        super();
        this.context = applicationContext;
        this.clazz = cls;
        this.instance = context.getBean(cls);
        init();
    }

    private void init() {
        Crawler c = clazz.getAnnotation(Crawler.class);
        Assert.notNull(c, StrFormatUtil.info("crawler {} lost annotation @cn.wanghaomiao.seimi.annotation.Crawler!", clazz.getName()));
        this.queueClass = c.queue();
        this.queueInstance = context.getBean(queueClass);
        Assert.notNull(queueInstance, StrFormatUtil.info("can not get {} instance,please check scan path", queueClass));
        memberMethods = new HashMap<>();
        ReflectionUtils.doWithMethods(clazz, method -> memberMethods.put(method.getName(), method));
        this.crawlerName = StringUtils.isNoneBlank(c.name()) ? c.name() : clazz.getSimpleName();
        instance.setCrawlerName(this.crawlerName);
        resolveProxy(c.proxy());
        this.useCookie = c.useCookie();
        this.currentUA = instance.getUserAgent();
        this.delay = c.delay();
        this.useUnrepeated = c.useUnrepeated();
        this.seimiHttpType = c.httpType();
        this.httpTimeOut = c.httpTimeOut();
        logger.info("Crawler[{}] init complete.", crawlerName);
    }

    private HttpHost resolveProxy(String proxyStr) {
        HttpHost r = null;
        if (StringUtils.isBlank(proxyStr)) {
            return null;
        }
        if (proxyStr.matches("(http|https|socket)://([0-9a-zA-Z]+\\.?)+:\\d+")) {
            String[] pies = proxyStr.split(":");
            String scheme = pies[0];
            int port = Integer.parseInt(pies[2]);
            String host = pies[1].substring(2);
            if (scheme.equals("socket")) {
                r = new HttpHost(host, port);
            } else {
                r = new HttpHost(host, port, scheme);
            }
        } else {
            logger.error("proxy must like ‘http|https|socket://host:port’");
        }
        proxy = r;
        return r;
    }

    private Proxy getStdProxy(String proxyStr) {
        Proxy proxy = null;
        if (StringUtils.isBlank(proxyStr)) {
            return null;
        }
        if (proxyStr.matches("(http|https|socket)://([0-9a-zA-Z]+\\.?)+:\\d+")) {
            String[] pies = proxyStr.split(":");
            String scheme = pies[0];
            int port = Integer.parseInt(pies[2]);
            String host = pies[1].substring(2);
            if (scheme.equals("socket")) {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
            } else {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            }
        } else {
            logger.error("proxy must like ‘http|https|socket://host:port’");
        }
        return proxy;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public BaseSeimiCrawler getInstance() {
        return instance;
    }

    public Class<? extends BaseSeimiCrawler> getClazz() {
        return clazz;
    }

    public Class<? extends SeimiQueue> getqueueClass() {
        return queueClass;
    }

    public Map<String, Method> getMemberMethods() {
        return memberMethods;
    }

    public String getCrawlerName() {
        return crawlerName;
    }

    public HttpHost getProxy() {
        if (StringUtils.isNotBlank(instance.proxy())) {
            return resolveProxy(instance.proxy());
        }
        return proxy;
    }

    public Proxy getStdProxy() {
        if (StringUtils.isNotBlank(instance.proxy())) {
            return getStdProxy(instance.proxy());
        }
        return null;
    }

    public boolean isUseCookie() {
        return useCookie;
    }

    public int getDelay() {
        return delay;
    }

    public boolean isUseUnrepeated() {
        return useUnrepeated;
    }

    public String getCurrentUA() {
        return currentUA;
    }

    public SeimiHttpType getSeimiHttpType() {
        return seimiHttpType;
    }

    public int getHttpTimeOut() {
        return httpTimeOut;
    }

    public CookieStore getCookieStore() {
        return aphcCookieStore;
    }

    public CookiesManager getOkHttpCookiesManager() {
        return okHttpCookiesManager;
    }

    public SeimiQueue getQueueInstance() {
        return queueInstance;
    }

    public void startRequest() {
        String[] startUrls = instance.startUrls();
        boolean trigger = false;
        if (ArrayUtils.isNotEmpty(startUrls)) {
            for (String url : startUrls) {
                Request request = new Request();
                String[] urlPies = url.split("##");
                if (urlPies.length >= 2 && StringUtils.lowerCase(urlPies[1]).equals("post")) {
                    request.setHttpMethod(HttpMethod.POST);
                }
                request.setCrawlerName(crawlerName);
                request.setUrl(url);
                request.setCallBack("start");
                queueInstance.push(request);
                logger.info("{} url={} started", crawlerName, url);
            }
            trigger = true;
        }
        if (!CollectionUtils.isEmpty(instance.startRequests())) {
            for (Request request : instance.startRequests()) {
                request.setCrawlerName(crawlerName);
                if (StringUtils.isBlank(request.getCallBack())) {
                    request.setCallBack("start");
                }
                queueInstance.push(request);
                logger.info("{} url={} started", crawlerName, request.getUrl());
            }
            trigger = true;
        }
        if (!trigger) {
            logger.error("crawler:{} can not find start urls!", crawlerName);
        }
    }

    public void sendRequest(Request request) {
        request.setCrawlerName(crawlerName);
        if (!checkRequest(request)){
            return;
        }
        if (request.isLambdaCb()) {
            request.setCallBack(getMethodName(request.getCallBackFunc()));
        }
        queueInstance.push(request);
    }

    public boolean checkRequest(Request request){
        //如果启用了系统级去重机制并且为首次处理则判断一个Request是否已经被处理过了
        if (!request.isSkipDuplicateFilter() && isUseUnrepeated() && queueInstance.isProcessed(request) && request.getCurrentReqCount() == 0) {
            logger.debug("This request has bean processed,so current request={} will be dropped!", JSON.toJSONString(request));
            return false;
        }
        return true;
    }

    public String queueInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("currentLen", queueInstance.len(getCrawlerName()));
        data.put("total", queueInstance.totalCrawled(getCrawlerName()));
        return JSON.toJSONString(data);
    }

    public String getMethodName(Request.SeimiCallbackFunc methodRef) {
        String methodName = referenceToMethod.get(methodRef.hashCode());
        if (StringUtils.isNotBlank(methodName)) {
            return methodName;
        }
        Method method = GenericUtils.getReferencedMethod(clazz, methodRef);
        methodName = method.getName();
        referenceToMethod.put(methodRef.hashCode(), methodName);
        return methodName;
    }
}
