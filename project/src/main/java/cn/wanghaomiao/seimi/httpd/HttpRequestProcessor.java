package cn.wanghaomiao.seimi.httpd;

import cn.wanghaomiao.seimi.core.SeimiQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/11/19.
 */
public abstract class HttpRequestProcessor {
    protected SeimiQueue seimiQueue;
    protected String crawlerName;

    public HttpRequestProcessor(SeimiQueue seimiQueue, String crawlerName) {
        this.seimiQueue = seimiQueue;
        this.crawlerName = crawlerName;
    }

    public abstract void handleHttpRequest(HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException;
}
