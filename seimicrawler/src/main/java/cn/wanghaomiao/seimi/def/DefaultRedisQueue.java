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
package cn.wanghaomiao.seimi.def;

import cn.wanghaomiao.seimi.annotation.Queue;
import cn.wanghaomiao.seimi.config.SeimiConfig;
import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.exception.SeimiInitExcepiton;
import cn.wanghaomiao.seimi.spring.boot.CrawlerProperties;
import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.utils.GenericUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.FstCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2015/8/21.
 */
@Queue
public class DefaultRedisQueue implements SeimiQueue {
    private String quueNamePrefix = "SEIMI_CRAWLER_QUEUE_";
    private String setNamePrefix = "SEIMI_CRAWLER_SET_";
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 默认的数据量预计消耗100M 内存,请根据使用情况自行调整
     * 可参考 <a href="https://hur.st/bloomfilter/?n=100M&p=0.01&m=&k=">bloomfilter</a>
     */
    private long expectedInsertions = 1_0000_0000L;
    private double falseProbability = 0.01;
    private Map<String,RBlockingQueue<Request>> queueCache = new HashMap<>();
    private Map<String,RBloomFilter<String>> bloomFilterCache = new HashMap<>();

    @Autowired(required = false)
    private CrawlerProperties crawlerProperties;
    @Autowired(required = false)
    private RedissonClient redisson;

    @PostConstruct
    public void init() {
        boolean isEnableRedissonQueue = false;
        if (!CrawlerCache.isSpringBoot()) {
            SeimiConfig seimiConfig = CrawlerCache.getConfig();
            if (seimiConfig != null && seimiConfig.isEnableRedissonQueue()) {
                //直接启动的方式
                isEnableRedissonQueue = true;
                if (seimiConfig.getBloomFilterExpectedInsertions() > 0) {
                    expectedInsertions = seimiConfig.getBloomFilterExpectedInsertions();
                }
                if (seimiConfig.getBloomFilterFalseProbability() > 0) {
                    falseProbability = seimiConfig.getBloomFilterFalseProbability();
                }
            }
        } else {
            if (crawlerProperties != null && crawlerProperties.isEnableRedissonQueue()) {
                //spring boot 方式
                isEnableRedissonQueue = true;
                if (crawlerProperties.getBloomFilterExpectedInsertions() > 0) {
                    expectedInsertions = crawlerProperties.getBloomFilterExpectedInsertions();
                }
                if (crawlerProperties.getBloomFilterFalseProbability() > 0) {
                    falseProbability = crawlerProperties.getBloomFilterFalseProbability();
                }
            }
        }
        if (isEnableRedissonQueue) {
            if (redisson == null) {
                logger.error("");
                throw new SeimiInitExcepiton(" 检测到启用了的 Redison queue，但是没有发现Redisson配置，请参考：https://github.com/redisson/redisson/wiki/2.-%E9%85%8D%E7%BD%AE%E6%96%B9%E6%B3%95");
            }
        }
        logger.info("springboot={},isEnableRedissonQueue = {},redisson = {}", CrawlerCache.isSpringBoot(), isEnableRedissonQueue, redisson);
    }

    @Override
    public Request bPop(String crawlerName) {
        Request request = null;
        try {
            RBlockingQueue<Request> rBlockingQueue = getQueue(crawlerName);
            request = rBlockingQueue.take();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return request;
    }

    @Override
    public boolean push(Request req) {
        try {
            RBlockingQueue<Request> rBlockingQueue = getQueue(req.getCrawlerName());
            rBlockingQueue.put(req);
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return false;
    }

    @Override
    public long len(String crawlerName) {
        long len = 0;
        try {
            RBlockingQueue<Request> rBlockingQueue = getQueue(crawlerName);
            len = rBlockingQueue.size();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return len;
    }

    @Override
    public boolean isProcessed(Request req) {
        boolean res = false;
        try {
            String sign = GenericUtils.signRequest(req);
            RBloomFilter<String> bloomFilter = getFilter(req.getCrawlerName());
            res = bloomFilter.contains(sign);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return res;
    }

    @Override
    public void addProcessed(Request req) {
        try {
            String sign = DigestUtils.md5Hex(req.getUrl());
            RBloomFilter<String> bloomFilter = getFilter(req.getCrawlerName());
            bloomFilter.add(sign);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    @Override
    public long totalCrawled(String crawlerName) {
        long count = 0;
        try {
            RBloomFilter<String> bloomFilter = getFilter(crawlerName);
            count = bloomFilter.count();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return count;
    }

    /**
     * 清除抓取记录
     *
     */
    @Override
    public void clearRecord(String crawlerName) {
        try {
            RBloomFilter<String> bloomFilter = getFilter(crawlerName);
            bloomFilter.delete();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }

    private RBlockingQueue<Request> getQueue(String crawlerName){
        RBlockingQueue<Request> rBlockingQueue = queueCache.get(crawlerName);
        if (rBlockingQueue == null){
            rBlockingQueue = redisson.getBlockingQueue(quueNamePrefix + crawlerName, new FstCodec());
            queueCache.put(crawlerName,rBlockingQueue);
        }
        return rBlockingQueue;
    }

    private RBloomFilter<String> getFilter(String crawlerName){
        RBloomFilter<String> bloomFilter = bloomFilterCache.get(crawlerName);
        if (bloomFilter == null){
            bloomFilter = redisson.getBloomFilter(setNamePrefix + crawlerName, new StringCodec());
            bloomFilter.tryInit(expectedInsertions, falseProbability);
            bloomFilterCache.put(crawlerName,bloomFilter);
        }
        return bloomFilter;
    }
}
