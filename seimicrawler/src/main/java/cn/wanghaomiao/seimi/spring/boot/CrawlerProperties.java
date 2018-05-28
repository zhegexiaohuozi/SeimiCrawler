package cn.wanghaomiao.seimi.spring.boot;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * @author: github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2018/5/8.
 */
@ConfigurationProperties(prefix="seimi.crawler")
public class CrawlerProperties implements Serializable {
    private boolean enabled;
    private String names;
    private boolean enableRedissonQueue;
    private long bloomFilterExpectedInsertions;
    private double bloomFilterFalseProbability;
    /**
     * SeimiAgent host address,such as seimi.wanghaomiao.cn or 10.10.121.211
     */
    private String seimiAgentHost;

    /**
     * seimiAgent listening port
     */
    private int seimiAgentPort;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public boolean isEnableRedissonQueue() {
        return enableRedissonQueue;
    }

    public void setEnableRedissonQueue(boolean enableRedissonQueue) {
        this.enableRedissonQueue = enableRedissonQueue;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("enabled", enabled)
                .append("names", names)
                .append("enableRedissonQueue", enableRedissonQueue)
                .append("bloomFilterExpectedInsertions", bloomFilterExpectedInsertions)
                .append("bloomFilterFalseProbability", bloomFilterFalseProbability)
                .toString();
    }
}
