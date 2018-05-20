package cn.wanghaomiao.seimi.spring.common;

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
}
