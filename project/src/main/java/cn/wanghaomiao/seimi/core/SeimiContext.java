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


import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.annotation.Interceptor;
import cn.wanghaomiao.seimi.annotation.Queue;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.exception.SeimiInitExcepiton;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.utils.StrFormatUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 初始化上下文环境
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/6/26.
 */
public class SeimiContext  extends AnnotationConfigApplicationContext {
    private int BASE_THREAD_NUM = 2;
    protected ApplicationContext applicationContext;
    protected SeimiScanner seimiScanner;
    protected Set<Class<? extends BaseSeimiCrawler>> crawlers;
    protected Set<Class<? extends SeimiQueue>> hasUsedQuene;
    protected List<SeimiInterceptor> interceptors;
    protected Map<String,CrawlerModel> crawlerModelContext;
    protected ExecutorService workersPool;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    public SeimiContext(String... crawlerNames){
        register(ScanConfig.class);
        init(crawlerNames);
        if(!CollectionUtils.isEmpty(crawlers)){
            prepareCrawlerModels();
            workersPool = Executors.newFixedThreadPool(BASE_THREAD_NUM*Runtime.getRuntime().availableProcessors()*crawlers.size());
            prepareWorkerThread();
        }else {
            logger.error("can not find any crawlers,please check!");
        }
    }

    private void init(String... crawlerNames){
        String[] targetPkgs = {"crawlers","queues","interceptors","cn.wanghaomiao.seimi"};
        seimiScanner = new SeimiScanner(this);
        Set<Class<?>> aladdin = seimiScanner.scan(targetPkgs, Crawler.class, Queue.class, Interceptor.class);
        applicationContext = this;
        crawlers = new HashSet<>();
        hasUsedQuene = new HashSet<>();
        interceptors = new LinkedList<>();
        crawlerModelContext = new HashMap<>();
        List<Class<?>> registList = new LinkedList<>();
        for (Class cls:aladdin){
            if (BaseSeimiCrawler.class.isAssignableFrom(cls)){
                Crawler c = (Crawler) cls.getAnnotation(Crawler.class);
                String crawlerName = StringUtils.isBlank(c.name()) ? cls.getSimpleName() : c.name();
                if (isNeededInit(crawlerName, crawlerNames)) {
                	hasUsedQuene.add(c.queue());
                	crawlers.add(cls);
                	registList.add(cls);
				}
            }else if (SeimiInterceptor.class.isAssignableFrom(cls)){
                registList.add(cls);
            }
        }
        //收集会使用到的seimiQueue并注册到context
        for (Class cls:aladdin){
            if (SeimiQueue.class.isAssignableFrom(cls)&&hasUsedQuene.contains(cls)){
                registList.add(cls);
            }
        }
        //统一注册需要用到的类
        seimiScanner.regist(registList);
        //获取注册后的拦截器实例
        for (Class cls:aladdin){
            if (SeimiInterceptor.class.isAssignableFrom(cls)){
                interceptors.add((SeimiInterceptor)applicationContext.getBean(cls));
            }
        }
        Collections.sort(interceptors, new Comparator<SeimiInterceptor>() {
            //对拦截器按照设定的权重进行倒序排序，如：88,66,11
            @Override
            public int compare(SeimiInterceptor o1, SeimiInterceptor o2) {
                return o1.getWeight() > o2.getWeight() ? -1 : 1;
            }
        });
    }
    
    private boolean isNeededInit(String crawlerName, String... crawlerNames) {
    	if (crawlerNames == null && crawlerName.length() == 0) {
			return true;
		}
    	
    	for (String name : crawlerNames) {
			if (crawlerName.equals(name)) {
				return true;
			}
		}
    	return false;
    }
    

    private void prepareCrawlerModels(){
        for (Class<? extends BaseSeimiCrawler> a:crawlers){
            CrawlerModel crawlerModel = new CrawlerModel(a,applicationContext);
            if (crawlerModelContext.containsKey(crawlerModel.getCrawlerName())){
                logger.error("Crawler:{} is repeated,please check",crawlerModel.getCrawlerName());
                throw new SeimiInitExcepiton(StrFormatUtil.info("Crawler:{} is repeated,please check",crawlerModel.getCrawlerName()));
            }
            crawlerModelContext.put(crawlerModel.getCrawlerName(),crawlerModel);
        }
    }

    private void prepareWorkerThread(){
        for (Map.Entry<String,CrawlerModel> crawlerEntry:crawlerModelContext.entrySet()){
            for (int i =0;i<BASE_THREAD_NUM*Runtime.getRuntime().availableProcessors();i++){
                workersPool.execute(new SeimiProcessor(interceptors,crawlerEntry.getValue()));
            }
        }
    }

}
