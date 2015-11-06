package cn.wanghaomiao.queues;

import cn.wanghaomiao.seimi.annotation.Queue;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.core.SeimiQueue;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/27.
 */
@Queue
public class MySelfRedisQueueImpl implements SeimiQueue {
    private String host = "127.0.0.1";
    private int port = 6379;
    private String password = "";
    private String quueName = "SEIMI_CRAWLER_QUEUE";
    private JedisPool wpool = null;
    private Logger logger = LoggerFactory.getLogger(getClass());
    public void refresh(){
        if (wpool!=null){
            this.wpool.destroy();
            this.wpool = null;
        }
    }
    public JedisPool getWritePool() {
        if (wpool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(500);
            config.setMaxIdle(200);
            config.setMinIdle(100);
            config.setMaxWaitMillis(1000 * 100);
            if (StringUtils.isNotBlank(password)){
                wpool = new JedisPool(config, this.host, this.port, 0, password);
            }else {
                wpool = new JedisPool(config, this.host, this.port, 0);
            }
        }
        return wpool;
    }

    public Jedis getWClient() {
        return getWritePool().getResource();
    }
    @Override
    public Request bPop(String crawlerName) {
        Jedis jedis = null;
        Request request = null;
        try {
            jedis = getWClient();
            List<String> res = jedis.brpop(0,quueName+crawlerName);
            request = JSON.parseObject(res.get(1),Request.class);
        }catch (Exception e){
            logger.warn(e.getMessage());
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return request;
    }

    @Override
    public boolean push(Request req) {
        Jedis jedis = null;
        boolean res = false;
        try {
            jedis = getWClient();
            res = jedis.lpush(quueName+req.getCrawlerName(),JSON.toJSONString(req))>0;
        }catch (Exception e){
            logger.warn(e.getMessage());
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public int len(String crawlerName) {
        long len = 0;
        Jedis jedis = null;
        try {
            jedis = getWClient();
            len = jedis.llen(quueName+crawlerName);
        }catch (Exception e){
            logger.warn(e.getMessage());
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return (int) len;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQuueName() {
        return quueName;
    }

    public void setQuueName(String quueName) {
        this.quueName = quueName;
    }
}
