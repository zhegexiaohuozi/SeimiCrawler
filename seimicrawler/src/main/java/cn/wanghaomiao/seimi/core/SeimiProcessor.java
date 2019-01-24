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

import cn.wanghaomiao.seimi.Constants;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
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
    private ExecutorService workersPool;

    public SeimiProcessor(List<SeimiInterceptor> interceptors, CrawlerModel crawlerModel) {
        this.queue = crawlerModel.getQueueInstance();
        this.interceptors = interceptors;
        this.crawlerModel = crawlerModel;
        this.crawler = crawlerModel.getInstance();
        workersPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),Constants.BASE_THREAD_NUM*Runtime.getRuntime().availableProcessors(),
                3,TimeUnit.SECONDS,new ArrayBlockingQueue(Runtime.getRuntime().availableProcessors()*5),new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void run() {
        while (true) {
            Request request = null;
            try {
                request = queue.bPop(crawlerModel.getCrawlerName());
                if (request == null) {
                    continue;
                }
                logger.debug("SeimiProcessor({}-{})[url:{}]",request.getCrawlerName(),request.getCallBack(),request.getUrl());
                if (crawlerModel == null) {
                    logger.error("No such crawler name:'{}'", request.getCrawlerName());
                    continue;
                }
                if (request.isStop()) {
                    logger.info("SeimiProcessor[{}] will stop!", Thread.currentThread().getName());
                    break;
                }
                workersPool.submit(new SeimiCrawlerHandler(request, crawlerModel, interceptors));
            } catch (Exception e) {
                logger.error("redission queue exception!",e);
                return;
            }
        }
    }
}
