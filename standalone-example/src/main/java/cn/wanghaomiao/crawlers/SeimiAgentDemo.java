package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import org.apache.commons.lang3.StringUtils;
import org.seimicrawler.xpath.JXDocument;

/**
 * 这个例子演示如何使用SeimiAgent进行复杂动态页面信息抓取
 * 独立启动需通过 {@link cn.wanghaomiao.seimi.config.SeimiConfig} 配置，spring boot通过标准的 application.properties来进行配置，
 * #seimi.crawler.seimi-agent-host=
 * #seimi.crawler.seimi-agent-port=
 *
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/4/14.
 */
@Crawler(name = "seimiagent")
public class SeimiAgentDemo extends BaseSeimiCrawler{

    @Override
    public String[] startUrls() {
        return new String[]{"https://www.baidu.com"};
    }

    @Override
    public void start(Response response) {
        Request seimiAgentReq = Request.build("https://www.souyidai.com",SeimiAgentDemo::getTotalTransactions)
                .useSeimiAgent()
//                告诉SeimiAgent针对这个请求是否使用cookie，如果没有设置使用当前Crawler关于cookie使用条件作为默认值。
//                .setSeimiAgentUseCookie(true)
                //设置全部load完成后给SeimiAgent多少时间用于执行js并渲染页面，单位为毫秒
                .setSeimiAgentRenderTime(5000);
        push(seimiAgentReq);
    }

    /**
     * 获取搜易贷首页总成交额
     * @param response
     */
    public void getTotalTransactions(Response response){
        JXDocument doc = response.document();
        try {
            String trans = StringUtils.join(doc.sel("//div[@class='homepage-amount']/div[@class='number font-arial']/div/span/text()"),"");
            logger.info("Final Res:{}",trans);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
