package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.annotation.Interceptor;
import cn.wanghaomiao.seimi.annotation.Queue;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.exception.SeimiInitExcepiton;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.utils.StrFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 初始化上下文环境
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/6/26.
 */
public class SeimiContext {
    private int BASE_THREAD_NUM = 16;
    protected ApplicationContext applicationContext;
    protected SeimiScanner seimiScanner;
    protected Set<Class<? extends BaseSeimiCrawler>> crawlers;
    protected List<SeimiInterceptor> interceptors;
    protected Map<String,CrawlerModel> crawlerModelContext;
    protected ExecutorService workersPool;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    public SeimiContext(){
        init();
        if(!CollectionUtils.isEmpty(crawlers)){
            prepareCrawlerModels();
            workersPool = Executors.newFixedThreadPool(BASE_THREAD_NUM*Runtime.getRuntime().availableProcessors()*crawlers.size());
            prepareWorkerThread();
        }else {
            logger.error("can not find any crawlers,please check!");
        }
    }

    private void init(){
        String[] targetPkgs = {"crawlers","queues","interceptors","cn.wanghaomiao.seimi"};
        seimiScanner = new SeimiScanner();
        Set<Class<?>> aladdin = seimiScanner.scan(targetPkgs, Crawler.class, Queue.class, Interceptor.class);
        applicationContext = seimiScanner.getContext();
        crawlers = new HashSet<>();
        interceptors = new LinkedList<>();
        crawlerModelContext = new HashMap<>();
        for (Class cls:aladdin){
            if (BaseSeimiCrawler.class.isAssignableFrom(cls)){
                crawlers.add(cls);
            }else if (SeimiInterceptor.class.isAssignableFrom(cls)){
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
