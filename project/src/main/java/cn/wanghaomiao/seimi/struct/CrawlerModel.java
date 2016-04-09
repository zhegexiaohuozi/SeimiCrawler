package cn.wanghaomiao.seimi.struct;
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


import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.utils.StrFormatUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/7/17.
 */
public class CrawlerModel {
    private ApplicationContext context;
    private BaseSeimiCrawler instance;
    private Class<? extends BaseSeimiCrawler> clazz;
    private SeimiQueue queueInstance;
    private Class<? extends SeimiQueue> queueClass;
    private Map<String,Method> memberMethods;
    private String crawlerName;
    private HttpHost proxy;
    private boolean useCookie = false;
    private boolean useUnrepeated = true;
    private int delay = 0;
    private Logger logger = LoggerFactory.getLogger(CrawlerModel.class);

    public CrawlerModel(Class<? extends BaseSeimiCrawler> cls,ApplicationContext applicationContext){
        super();
        this.context = applicationContext;
        this.clazz = cls;
        this.instance = context.getBean(cls);
        init();
    }

    private void init(){
        Crawler c = clazz.getAnnotation(Crawler.class);
        Assert.notNull(c, StrFormatUtil.info("crawler {} lost annotation @cn.wanghaomiao.seimi.annotation.Crawler!",clazz.getName()));
        this.queueClass = c.queue();
        this.queueInstance = context.getBean(queueClass);
        Assert.notNull(queueInstance, StrFormatUtil.info("can not get {} instance,please check scan path", queueClass));
        instance.setQueue(queueInstance);
        memberMethods = new HashMap<>();
        ReflectionUtils.doWithMethods(clazz, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                memberMethods.put(method.getName(),method);
            }
        });
        this.crawlerName = StringUtils.isNoneBlank(c.name())?c.name():clazz.getSimpleName();
        instance.setCrawlerName(this.crawlerName);
        resolveProxy(c.proxy());
        this.useCookie = c.useCookie();
        this.delay = c.delay();
        this.useUnrepeated = c.useUnrepeated();
        logger.info("Crawler[{}] init complete.", crawlerName);
    }

    private HttpHost resolveProxy(String proxyStr){
        HttpHost r = null;
        if (StringUtils.isBlank(proxyStr)){
            return null;
        }
        if (proxyStr.matches("(http|https|socket)://([0-9a-zA-Z]+\\.?)+:\\d+")){
            String[] pies = proxyStr.split(":");
            String scheme = pies[0];
            int port = Integer.parseInt(pies[2]);
            String host = pies[1].substring(2);
            if (scheme.equals("socket")){
                r = new HttpHost(host,port);
            }else {
                r = new HttpHost(host,port,scheme);
            }
        }else {
            logger.error("proxy must like ‘http|https|socket://host:port’");
        }
        proxy = r;
        return r;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    public BaseSeimiCrawler getInstance() {
        return instance;
    }

    public void setInstance(BaseSeimiCrawler instance) {
        this.instance = instance;
    }

    public Class<? extends BaseSeimiCrawler> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends BaseSeimiCrawler> clazz) {
        this.clazz = clazz;
    }

    public SeimiQueue getQueueInstance() {
        return queueInstance;
    }

    public void setqueueImpl(SeimiQueue queueImpl) {
        this.queueInstance = queueImpl;
    }

    public Class<? extends SeimiQueue> getqueueClass() {
        return queueClass;
    }

    public void setqueueClass(Class<? extends SeimiQueue> queueClass) {
        this.queueClass = queueClass;
    }

    public Map<String, Method> getMemberMethods() {
        return memberMethods;
    }

    public void setMemberMethods(Map<String, Method> memberMethods) {
        this.memberMethods = memberMethods;
    }

    public String getCrawlerName() {
        return crawlerName;
    }

    public HttpHost getProxy() {
        if (StringUtils.isNotBlank(instance.proxy())){
            return resolveProxy(instance.proxy());
        }
        return proxy;
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
}
