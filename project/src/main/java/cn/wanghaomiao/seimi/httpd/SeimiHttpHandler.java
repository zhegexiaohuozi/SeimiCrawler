package cn.wanghaomiao.seimi.httpd;

import cn.wanghaomiao.seimi.struct.CrawlerModel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/19.
 */
public class SeimiHttpHandler extends AbstractHandler {
    private Map<String,CrawlerModel> crawlerContext;
    private Map<String,HttpRequestProcessor> requestMapper;
    public SeimiHttpHandler(Map<String, CrawlerModel> crawlerContext) {
        this.crawlerContext = crawlerContext;
        this.requestMapper = new HashMap<>();
    }

    public SeimiHttpHandler add(String path,HttpRequestProcessor processor){
        requestMapper.put(path,processor);
        return this;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setHeader("server", "SeimiCrawler");
        HttpRequestProcessor processor = requestMapper.get(target);
        if (processor!=null){
            response.setStatus(HttpServletResponse.SC_OK);
            processor.handleHttpRequest(request,response);
        }else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        baseRequest.setHandled(true);
    }
}
