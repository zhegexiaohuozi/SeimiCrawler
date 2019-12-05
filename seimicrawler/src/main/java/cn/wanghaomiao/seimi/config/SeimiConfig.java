package cn.wanghaomiao.seimi.config;

import org.redisson.codec.FstCodec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.ReplicatedServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;

/**
 * @author 汪浩淼  seimimaster@gmail.com
 * @since 2018/5/11.
 */
public class SeimiConfig {

    public SeimiConfig() {
        this.redissonConfig = new Config();
    }

    /**
     * 如果开启分布式设置默认启用分布式队列
     */
    public SeimiConfig(Config config) {
        this.enableRedissonQueue = true;
        this.redissonConfig = config;
        this.redissonConfig.setCodec(new FstCodec());
    }

    private boolean enableRedissonQueue = false;
    /**
     * 预期BloomFilter要插入的元素数量
     */
    private long bloomFilterExpectedInsertions;
    /**
     * BloomFilter 的期望误差率
     */
    private double bloomFilterFalseProbability;

    /**
     * 设置SeimiAgent的主机地址，如 seimi.wanghaomiao.cn or 10.10.121.211
     */
    private String seimiAgentHost;

    /**
     * seimiAgent监听端口
     */
    private int seimiAgentPort = 80;

    /**
     * redisson 各种分布式配置可以参考 https://github.com/redisson/redisson/wiki/2.-%E9%85%8D%E7%BD%AE%E6%96%B9%E6%B3%95
     */
    private Config redissonConfig;

    public boolean isEnableRedissonQueue() {
        return enableRedissonQueue;
    }

    public SeimiConfig enableDistributedQueue() {
        this.enableRedissonQueue = true;
        return this;
    }

    /**
     * Init master/slave servers configuration.
     *
     * config.useMasterSlaveServers()
     *     //可以用"rediss://"来启用SSL连接
     *     .setMasterAddress("redis://127.0.0.1:6379")
     *     .addSlaveAddress("redis://127.0.0.1:6389", "redis://127.0.0.1:6332", "redis://127.0.0.1:6419")
     *     .addSlaveAddress("redis://127.0.0.1:6399");
     *
     * @return MasterSlaveServersConfig
     */
    public MasterSlaveServersConfig redisMasterSlaveServers() {
        enableDistributedQueue();
        return redissonConfig.useMasterSlaveServers();
    }
    /**
     * Init sentinel servers configuration.
     *
     * config.useSentinelServers()
     *     .setMasterName("mymaster")
     *     //可以用"rediss://"来启用SSL连接
     *     .addSentinelAddress("redis://127.0.0.1:26389", "redis://127.0.0.1:26379")
     *     .addSentinelAddress("redis://127.0.0.1:26319");
     *
     * @return SentinelServersConfig
     */
    public SentinelServersConfig redisSentinelServers() {
        enableDistributedQueue();
        return redissonConfig.useSentinelServers();
    }

    /**
     * Init single server configuration.
     *
     *  config.useSingleServer().setAddress("redis://myredisserver:6379");
     *
     * @return SingleServerConfig
     */
    public SingleServerConfig redisSingleServer() {
        enableDistributedQueue();
        return redissonConfig.useSingleServer();
    }

    /**
     * Init Replicated servers configuration.
     * Most used with Azure Redis Cache or AWS Elasticache
     *
     * config.useReplicatedServers()
     *     .setScanInterval(2000) // 主节点变化扫描间隔时间
     *     //可以用"rediss://"来启用SSL连接
     *     .addNodeAddress("redis://127.0.0.1:7000", "redis://127.0.0.1:7001")
     *     .addNodeAddress("redis://127.0.0.1:7002");
     *
     * @return ReplicatedServersConfig
     */
    public ReplicatedServersConfig redisReplicatedServers() {
        enableDistributedQueue();
        return redissonConfig.useReplicatedServers();
    }

    /**
     * Init cluster servers configuration
     *
     * config.useClusterServers()
     *     .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
     *     //可以用"rediss://"来启用SSL连接
     *     .addNodeAddress("redis://127.0.0.1:7000", "redis://127.0.0.1:7001")
     *     .addNodeAddress("redis://127.0.0.1:7002");
     *
     * @return config
     */
    public ClusterServersConfig redisClusterServers() {
        enableDistributedQueue();
        return redissonConfig.useClusterServers();
    }

    public long getBloomFilterExpectedInsertions() {
        return bloomFilterExpectedInsertions;
    }

    public void setBloomFilterExpectedInsertions(long bloomFilterExpectedInsertions) {
        this.bloomFilterExpectedInsertions = bloomFilterExpectedInsertions;
    }

    public double getBloomFilterFalseProbability() {
        return bloomFilterFalseProbability;
    }

    public void setBloomFilterFalseProbability(double bloomFilterFalseProbability) {
        this.bloomFilterFalseProbability = bloomFilterFalseProbability;
    }

    public String getSeimiAgentHost() {
        return seimiAgentHost;
    }

    public void setSeimiAgentHost(String seimiAgentHost) {
        this.seimiAgentHost = seimiAgentHost;
    }

    public int getSeimiAgentPort() {
        return seimiAgentPort;
    }

    public void setSeimiAgentPort(int seimiAgentPort) {
        this.seimiAgentPort = seimiAgentPort;
    }

    public Config getRedissonConfig() {
        return redissonConfig;
    }
}
