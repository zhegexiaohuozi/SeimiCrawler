package cn.wanghaomiao.seimi.spring.common;

import cn.wanghaomiao.seimi.config.SeimiConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2018/5/8.
 */
@Configuration
@ImportResource("classpath*:**/seimi*.xml")
@EnableScheduling
public class SeimiCrawlerBaseConfig {

    @Bean
    @Conditional(StandaloneCondition.class)
    public RedissonClient initRedisson(){
        SeimiConfig config = CrawlerCache.getConfig();
        if (config == null||!config.isEnableRedissonQueue()){
            return null;
        }
        return Redisson.create(config.getRedissonConfig());
    }
}
