package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.httpd.CrawlerStatusHttpdHandler;
import cn.wanghaomiao.seimi.httpd.PushRequestHttpdHandler;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import java.util.Map;

/**
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/16.
 */
public class Seimi extends SeimiContext {
    /**
     * 主启动
     * start master
     * @param crawlerNames
     */
    public void start(String... crawlerNames){
        if (crawlerNames==null||crawlerNames.length==0){
            for (Map.Entry<String,CrawlerModel> entry:crawlerModelContext.entrySet()){
                sendRequest(entry.getKey(),entry.getValue().getQueueInstance(),entry.getValue().getInstance().startUrls());
            }
        }else {
            for (String name:crawlerNames){
                CrawlerModel crawlerModel = crawlerModelContext.get(name);
                if (crawlerModel!=null){
                    sendRequest(crawlerModel.getCrawlerName(),crawlerModel.getQueueInstance(),crawlerModel.getInstance().startUrls());
                }else {
                    logger.error("error crawler name '{}',can not find it!",name);
                }
            }
        }
    }

    /**
     * 按名称启动爬虫并开启http服务接口API
     */
    public void startWithHttpd(int port,String... crawlerNames){
        start(crawlerNames);
        WebServer webServer = WebServers.createWebServer(port);
        if (crawlerNames==null||crawlerNames.length==0){
            for (Map.Entry<String,CrawlerModel> entry:crawlerModelContext.entrySet()){
                webServer.add("/push/"+entry.getKey(),new PushRequestHttpdHandler(entry.getValue().getQueueInstance(),entry.getKey()))
                        .add("/status/"+entry.getKey(),new CrawlerStatusHttpdHandler(entry.getValue().getQueueInstance(),entry.getKey()));
            }
        }else {
            for (String name:crawlerNames){
                CrawlerModel crawlerModel = crawlerModelContext.get(name);
                if (crawlerModel!=null){
                    webServer.add("/push/"+name,new PushRequestHttpdHandler(crawlerModel.getQueueInstance(),name))
                            .add("/status/"+name,new CrawlerStatusHttpdHandler(crawlerModel.getQueueInstance(),name));
                }
            }
        }
        webServer.start();
    }

    public void startAll(){
        start();
    }
    public void startAllWithHttpd(int port){
        startWithHttpd(port);
    }
    public void startWorkers(){
        //初始化Seimi对象时即完成了workers的创建，故这里仅用作引导说明。
        logger.info("workers started!");
    }

    private void sendRequest(String crawlerName,SeimiQueue queue,String[] startUrls){
        if (ArrayUtils.isNotEmpty(startUrls)){
            for (String url:startUrls){
                Request request = new Request();
                String[] urlPies = url.split("##");
                if (urlPies.length>=2&& StringUtils.lowerCase(urlPies[1]).equals("post")){
                    request.setHttpMethod(HttpMethod.POST);
                }
                request.setCrawlerName(crawlerName);
                request.setUrl(url);
                request.setCallBack("start");
                queue.push(request);
                logger.info("{} started",crawlerName);
            }
        }else {
            logger.error("crawler:{} can not find start urls!",crawlerName);
        }
    }
}
