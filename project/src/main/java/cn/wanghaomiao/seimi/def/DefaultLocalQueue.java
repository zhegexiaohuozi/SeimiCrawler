package cn.wanghaomiao.seimi.def;

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
        if (set.contains(sign)){
            return true;
        }else {
            set.add(sign);
        }
        return false;
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
