package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * 这个例子演示如何使用SeimiAgent进行复制动态页面信息抓取
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/4/14.
 */
@Crawler(name = "seimiagent")
public class SeimiAgentDemo extends BaseSeimiCrawler{

    /**
     * 在resource/config/seimi.properties中配置方便更换，当然也可以自行根据情况使用自己的统一配置中心等服务
     */
    @Value("${seimiAgentHost}")
    private String seimiAgentHost;

    @Value("${seimiAgentPort}")
    private int seimiAgentPort;

    @Override
    public String[] startUrls() {
        return new String[]{"https://www.baidu.com"};
    }

    @Override
    public String seiAgentHost() {
        return this.seimiAgentHost;
    }

    @Override
    public int seimiAgentPort() {
        return this.seimiAgentPort;
    }

    @Override
    public void start(Response response) {
        Request seimiAgentReq = Request.build("https://www.souyidai.com","getTotalTransactions")
                .useSeimiAgent()
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
