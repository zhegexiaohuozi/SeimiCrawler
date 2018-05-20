package cn.wanghaomiao.seimi.spring.common;

import cn.wanghaomiao.seimi.config.SeimiConfig;
import cn.wanghaomiao.seimi.core.SeimiInterceptor;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.exception.SeimiProcessExcepiton;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2018/5/7.
 */
public class CrawlerCache {
    private final static List<SeimiInterceptor> interceptors = new LinkedList<>();
    private final static Set<Class<? extends BaseSeimiCrawler>> crawlers = new HashSet<>();
    private final static Map<String,CrawlerModel> crawlerModelContext  = new HashMap<>();
    private static SeimiConfig config = null;
    private static boolean springBoot = true;


    static void addInterceptor(SeimiInterceptor seimiInterceptor) {
        interceptors.add(seimiInterceptor);
    }

    static void addCrawler(Class<? extends BaseSeimiCrawler> crawlerClass) {
        crawlers.add(crawlerClass);
    }

    static List<SeimiInterceptor> getInterceptors(){
        //对拦截器按照设定的权重进行倒序排序，如：88,66,11
        interceptors.sort((o1, o2) -> o1.getWeight() > o2.getWeight() ? -1 : 1);
        return interceptors;
    }

    static Set<Class<? extends BaseSeimiCrawler>> getCrawlers(){
        return crawlers;
    }

    public static Map<String, CrawlerModel> getCrawlerModelContext() {
        return crawlerModelContext;
    }

    public static CrawlerModel getCrawlerModel(String crawlerName) {
        return crawlerModelContext.get(crawlerName);
    }

    static void putCrawlerModel(String name,CrawlerModel model){
        crawlerModelContext.put(name,model);
    }

    static boolean isExist(String crawlerName){
        return crawlerModelContext.containsKey(crawlerName);
    }

    public static SeimiConfig getConfig() {
        return config;
    }

    public static void setConfig(SeimiConfig config) {
        CrawlerCache.config = config;
    }

    public static boolean isSpringBoot() {
        return springBoot;
    }

    public static void setSpringBoot(boolean springBoot) {
        CrawlerCache.springBoot = springBoot;
    }

    public static void consumeRequest(Request request){
        if (request == null){
            throw new  SeimiProcessExcepiton("reques can not be null");
        }
        CrawlerModel crawlerModel = getCrawlerModel(request.getCrawlerName());
        if (crawlerModel == null){
            throw new  SeimiProcessExcepiton("can not find any crawler named '"+request.getCrawlerName()+"'.");
        }
        crawlerModel.sendRequest(request);
    }
}
