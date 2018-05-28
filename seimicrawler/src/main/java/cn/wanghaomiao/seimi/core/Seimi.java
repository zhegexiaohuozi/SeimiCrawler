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

import cn.wanghaomiao.seimi.config.SeimiConfig;
import cn.wanghaomiao.seimi.httpd.CrawlerStatusHttpProcessor;
import cn.wanghaomiao.seimi.httpd.PushRequestHttpProcessor;
import cn.wanghaomiao.seimi.httpd.SeimiHttpHandler;
import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2015/10/16.
 */
public class Seimi {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private SeimiConfig config;

    public Seimi(SeimiConfig config) {
        this.config = config;
    }

    public Seimi(){
        this.config = null;
    }

    /**
     * 主启动
     * start master
     *
     * @param ifBlock      是否要开始等待线程池结束
     * @param crawlerNames ~~
     */
    public void goRun(boolean ifBlock, String... crawlerNames) {
        if (crawlerNames == null || crawlerNames.length == 0) {
            logger.info("start all crawler as workers.");
        } else {
            SeimiContext.init(config);
            for (String name : crawlerNames) {
                CrawlerModel crawlerModel = CrawlerCache.getCrawlerModel(name);
                if (crawlerModel != null) {
                    crawlerModel.startRequest();
                } else {
                    logger.error("error crawler name '{}',can not find it!", name);
                }
            }
        }
        //是否开始等待线程池结束
//        if (ifBlock){
//            waitToEnd();
//        }
    }

    public void goRun(String... crawlerNames) {
        goRun(true, crawlerNames);
    }

    @Deprecated
    public void start(String... crawlerNames) {
        goRun(true, crawlerNames);
    }

    /**
     * 按名称启动爬虫并开启http服务接口API
     */
    public void goRunWithHttpd(int port, String... crawlerNames) {
        goRun(false, crawlerNames);
        Map<String, CrawlerModel> crawlerModelContext = CrawlerCache.getCrawlerModelContext();
        SeimiHttpHandler seimiHttpHandler = new SeimiHttpHandler(crawlerModelContext);
        if (crawlerNames == null || crawlerNames.length == 0) {
            for (Map.Entry<String, CrawlerModel> entry : crawlerModelContext.entrySet()) {
                seimiHttpHandler.add("/push/" + entry.getKey(), new PushRequestHttpProcessor(entry.getValue().getQueueInstance(), entry.getKey()))
                        .add("/status/" + entry.getKey(), new CrawlerStatusHttpProcessor(entry.getValue().getQueueInstance(), entry.getKey()));
            }
        } else {
            for (String name : crawlerNames) {
                CrawlerModel crawlerModel = crawlerModelContext.get(name);
                if (crawlerModel != null) {
                    seimiHttpHandler.add("/push/" + name, new PushRequestHttpProcessor(crawlerModel.getQueueInstance(), name))
                            .add("/status/" + name, new CrawlerStatusHttpProcessor(crawlerModel.getQueueInstance(), name));
                }
            }
        }
        logger.info("Http request push service also started on port:{}", port);
        startJetty(port, seimiHttpHandler);
    }

    public void startAll() {
        SeimiContext.init(config);
        for (Map.Entry<String, CrawlerModel> entry : CrawlerCache.getCrawlerModelContext().entrySet()) {
            entry.getValue().startRequest();
        }
//        waitToEnd();
    }

    private void startJetty(int port, SeimiHttpHandler seimiHttpHandler) {
        Server server = new Server(port);
        server.setHandler(seimiHttpHandler);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            logger.error("http service start error,{}", e.getMessage(), e);
        }
    }
}
