package cn.wanghaomiao.seimi.def;

import cn.wanghaomiao.seimi.annotation.Queue;
import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.struct.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/7/21.
 */
@Queue
public class DefaultLocalQueue implements SeimiQueue {
    private Map<String,LinkedBlockingQueue<Request>> queueMap = new HashMap<>();
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
    public int len(String crawlerName) {
        LinkedBlockingQueue<Request> queue = getQueue(crawlerName);
        return queue.size();
    }

    public LinkedBlockingQueue<Request> getQueue(String crawlerName){
        LinkedBlockingQueue<Request> queue = queueMap.get(crawlerName);
        if (queue==null){
            queue = new LinkedBlockingQueue<>();
            queueMap.put(crawlerName,queue);
        }
        return queue;
    }
}
