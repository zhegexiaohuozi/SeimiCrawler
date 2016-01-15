package cn.wanghaomiao.seimi.def;
/*
   Copyright 2015 - now original author

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

import cn.wanghaomiao.seimi.annotation.Queue;
import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.struct.Request;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/7/21.
 */
@Queue
public class DefaultLocalQueue implements SeimiQueue {
    private Map<String,LinkedBlockingQueue<Request>> queueMap = new HashMap<>();
    private Map<String,ConcurrentSkipListSet<String>> processedData = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public Request bPop(String crawlerName) {
        try {
            LinkedBlockingQueue<Request> queue = getQueue(crawlerName);
            return queue.take();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    @Override
    public boolean push(Request req) {
        try {
            LinkedBlockingQueue<Request> queue = getQueue(req.getCrawlerName());
            queue.put(req);
            return true;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
        return false;
    }

    @Override
    public long len(String crawlerName) {
        LinkedBlockingQueue<Request> queue = getQueue(crawlerName);
        return queue.size();
    }

    @Override
    public boolean isProcessed(Request req) {
        ConcurrentSkipListSet<String> set = getProcessedSet(req.getCrawlerName());
        String sign = DigestUtils.md5Hex(req.getUrl());
        return set.contains(sign);
    }

    @Override
    public void addProcessed(Request req) {
        ConcurrentSkipListSet<String> set = getProcessedSet(req.getCrawlerName());
        String sign = DigestUtils.md5Hex(req.getUrl());
        set.add(sign);
    }

    @Override
    public long totalCrawled(String crawlerName) {
        ConcurrentSkipListSet<String> set = getProcessedSet(crawlerName);
        return set.size();
    }

    public LinkedBlockingQueue<Request> getQueue(String crawlerName){
        LinkedBlockingQueue<Request> queue = queueMap.get(crawlerName);
        if (queue==null){
            queue = new LinkedBlockingQueue<>();
            queueMap.put(crawlerName,queue);
        }
        return queue;
    }

    public ConcurrentSkipListSet<String> getProcessedSet(String crawlerName){
        ConcurrentSkipListSet<String> set = processedData.get(crawlerName);
        if (set == null){
            set = new ConcurrentSkipListSet<>();
            processedData.put(crawlerName,set);
        }
        return set;
    }
}
