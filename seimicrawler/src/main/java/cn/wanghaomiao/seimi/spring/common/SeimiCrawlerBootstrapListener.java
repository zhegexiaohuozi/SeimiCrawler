package cn.wanghaomiao.seimi.spring.common;

import cn.wanghaomiao.seimi.Constants;
import cn.wanghaomiao.seimi.config.SeimiConfig;
import cn.wanghaomiao.seimi.core.SeimiProcessor;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.exception.SeimiInitExcepiton;
import cn.wanghaomiao.seimi.spring.boot.CrawlerProperties;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.utils.StrFormatUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: github.com/zhegexiaohuozi seimimaster@gmail.com
 * @since 2018/5/7.
 */
public class SeimiCrawlerBootstrapListener implements ApplicationListener<ContextRefreshedEvent> {

    private ExecutorService workersPool;
    private boolean isSpringBoot = false;

    public SeimiCrawlerBootstrapListener(){
        super();
    }

    public SeimiCrawlerBootstrapListener(boolean isSpringBoot) {
        super();
        this.isSpringBoot = isSpringBoot;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //解决某些场景执行两次问题
        if (event.getApplicationContext().getParent() == null) {
            return ;
        }

        ApplicationContext context = event.getApplicationContext();
        if (isSpringBoot){
            CrawlerProperties crawlerProperties = context.getBean(CrawlerProperties.class);
            if (!crawlerProperties.isEnabled()){
                logger.warn("{} is not enabled",Constants.SEIMI_CRAWLER_BOOTSTRAP_ENABLED);
                return;
            }
        }

        if (context != null) {
            if (CollectionUtils.isEmpty(CrawlerCache.getCrawlers())) {
                logger.info("Not find any crawler,may be you need to check.");
                return;
            }
            workersPool = Executors.newFixedThreadPool(Constants.BASE_THREAD_NUM * Runtime.getRuntime().availableProcessors() * CrawlerCache.getCrawlers().size());
            for (Class<? extends BaseSeimiCrawler> a : CrawlerCache.getCrawlers()) {
                CrawlerModel crawlerModel = new CrawlerModel(a, context);
                if (CrawlerCache.isExist(crawlerModel.getCrawlerName())) {
                    logger.error("Crawler:{} is repeated,please check", crawlerModel.getCrawlerName());
//                    重名应该不允许覆盖提示个错误信息，而不是直接中断
                    continue;
//                    throw new SeimiInitExcepiton(StrFormatUtil.info("Crawler:{} is repeated,please check", crawlerModel.getCrawlerName()));
                }
                CrawlerCache.putCrawlerModel(crawlerModel.getCrawlerName(), crawlerModel);
            }

            for (Map.Entry<String, CrawlerModel> crawlerEntry : CrawlerCache.getCrawlerModelContext().entrySet()) {
                for (int i = 0; i < Constants.BASE_THREAD_NUM * Runtime.getRuntime().availableProcessors(); i++) {
                    workersPool.execute(new SeimiProcessor(CrawlerCache.getInterceptors(), crawlerEntry.getValue()));
                }
            }

            if (isSpringBoot){
                CrawlerProperties crawlerProperties = context.getBean(CrawlerProperties.class);
                String crawlerNames = crawlerProperties.getNames();
                if (StringUtils.isBlank(crawlerNames)){
                    logger.info("Spring boot start [{}] as worker.",StringUtils.join(CrawlerCache.getCrawlerModelContext().keySet(),","));
                }else {
                    String[] crawlers = crawlerNames.split(",");
                    for (String cn:crawlers){
                        CrawlerModel crawlerModel = CrawlerCache.getCrawlerModel(cn);
                        if (crawlerModel == null){
                            logger.warn("Crawler name = {} is not existent.",cn);
                            continue;
                        }
                        crawlerModel.startRequest();
                    }
                }
                //统一通用配置信息至 seimiConfig
                SeimiConfig config = new SeimiConfig();
                config.setBloomFilterExpectedInsertions(crawlerProperties.getBloomFilterExpectedInsertions());
                config.setBloomFilterFalseProbability(crawlerProperties.getBloomFilterFalseProbability());
                config.setSeimiAgentHost(crawlerProperties.getSeimiAgentHost());
                config.setSeimiAgentPort(crawlerProperties.getSeimiAgentPort());
                CrawlerCache.setConfig(config);
            }
        }
    }
}
