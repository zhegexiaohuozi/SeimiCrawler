package cn.wanghaomiao.seimi.def;

import cn.wanghaomiao.seimi.core.SeimiCrawler;
import cn.wanghaomiao.seimi.core.SeimiQueue;
import cn.wanghaomiao.seimi.struct.Request;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/8/17.
 */
public abstract class BaseSeimiCrawler implements SeimiCrawler {

    protected SeimiQueue queue;
    protected CookieStore cookieStore = new BasicCookieStore();
    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected String crawlerName;

    protected void push(Request request){
        request.setCrawlerName(crawlerName);
        queue.push(request);
    }

    @Override
    public String getUserAgent() {
        return "SeimiCrawler/JsoupXpath";
    }
    @Override
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public String[] allowRules() {
        return null;
    }

    @Override
    public String[] denyRules() {
        return null;
    }

    public void setQueue(SeimiQueue queue) {
        this.queue = queue;
    }

    public void setCrawlerName(String crawlerName) {
        this.crawlerName = crawlerName;
    }

    public String getCrawlerName() {
        return crawlerName;
    }
}
