package cn.wanghaomiao.seimi.def;
/*
   Copyright 2016 汪浩淼(Haomiao Wang)

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
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/8/21.
 */
@Queue
public class DefaultRedisQueue implements SeimiQueue {
    /**
     * host,port,password,queueName需要开发者自行在配置文件中注入，如：
     <struct id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
         <property name="locations">
         <list>
         <value>classpath:config/seimi.properties</value>
         </list>
         </property>
     </struct>
     再配一个properties文件
     redis.host=192.168.1.11
     redis.port=6379
     redis.password=
     */
    @Value("${redis.host:127.0.0.1}")
    private String host;
    @Value("${redis.port:6379}")
    private int port;
    @Value("${redis.password:}")
    private String password;
    private String quueNamePrefix = "SEIMI_CRAWLER_QUEUE_";
    private String setNamePrefix = "SEIMI_CRAWLER_SET_";
    private JedisPool wpool = null;
    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init(){
        getWritePool();
    }

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
            logger.info("create redisPool host={},port={}",this.host,this.port);
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
            List<String> res = jedis.brpop(0, quueNamePrefix +crawlerName);
            request = JSON.parseObject(res.get(1),Request.class);
        }catch (Exception e){
            logger.warn(e.getMessage());
            refresh();
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
            res = jedis.lpush(quueNamePrefix +req.getCrawlerName(),JSON.toJSONString(req))>0;
        }catch (Exception e){
            logger.warn(e.getMessage());
            refresh();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public long len(String crawlerName) {
        long len = 0;
        Jedis jedis = null;
        try {
            jedis = getWClient();
            len = jedis.llen(quueNamePrefix +crawlerName);
        }catch (Exception e){
            logger.warn(e.getMessage());
            refresh();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return len;
    }

    @Override
    public boolean isProcessed(Request req) {
        Jedis jedis = null;
        boolean res = false;
        try {
            jedis = getWClient();
            String sign = DigestUtils.md5Hex(req.getUrl());
            res = jedis.sismember(setNamePrefix +req.getCrawlerName(),sign);
        }catch (Exception e){
            logger.warn(e.getMessage());
            refresh();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return res;
    }

    @Override
    public void addProcessed(Request req) {
        Jedis jedis = null;
        try {
            jedis = getWClient();
            String sign = DigestUtils.md5Hex(req.getUrl());
            jedis.sadd(setNamePrefix +req.getCrawlerName(),sign);
        }catch (Exception e){
            logger.warn(e.getMessage());
            refresh();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
    }

    @Override
    public long totalCrawled(String crawlerName) {
        long count = 0;
        Jedis jedis = null;
        try {
            jedis = getWClient();
            count = jedis.scard(setNamePrefix + crawlerName);
        }catch (Exception e){
            logger.warn(e.getMessage());
            refresh();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        return count;
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

    public String getQuueNamePrefix() {
        return quueNamePrefix;
    }

    public void setQuueNamePrefix(String quueNamePrefix) {
        this.quueNamePrefix = quueNamePrefix;
    }
}
