package cn.wanghaomiao.seimi.httpd;

import cn.wanghaomiao.seimi.core.SeimiQueue;
import com.alibaba.fastjson.JSON;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/16.
 */
public class CrawlerStatusHttpProcessor extends HttpRequestProcessor {
    public CrawlerStatusHttpProcessor(SeimiQueue seimiQueue, String crawlerName) {
        super(seimiQueue, crawlerName);
    }

    @Override
    public void handleHttpRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json; charset=utf-8");
        Map<String, Long> body = new HashMap<>();
        body.put("queueLen", seimiQueue.len(crawlerName));
        body.put("totalCrawled", seimiQueue.totalCrawled(crawlerName));
        PrintWriter out = response.getWriter();
        out.println(JSON.toJSONString(body));
    }
}
