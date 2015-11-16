package cn.wanghaomiao.seimi.httpd;

import cn.wanghaomiao.seimi.core.SeimiQueue;
import com.alibaba.fastjson.JSON;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.handler.StringHttpHandler;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/16.
 */
public class CrawlerStatusHttpdHandler extends StringHttpHandler {
    private SeimiQueue seimiQueue;
    private String crawlerName;
    public CrawlerStatusHttpdHandler(SeimiQueue seimiQueue, String crawlerName) {
        super("application/json", "");
        this.seimiQueue = seimiQueue;
        this.crawlerName = crawlerName;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) {
        Map<String, Long> body = new HashMap<>();
        body.put("queueLen",seimiQueue.len(crawlerName));
        body.put("totalCrawled",seimiQueue.totalCrawled(crawlerName));
        response.content(JSON.toJSONString(body)).charset(Charset.forName("UTF-8")).header("server", "SeimiCrawler")
                .header("Content-Type", "application/json; charset=UTF-8").end();
        control.nextHandler();
    }
}
