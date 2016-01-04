package cn.wanghaomiao.seimi.def;
/*
   Copyright 2016 汪浩淼(Haomiao Wang)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

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

    protected void push(Request request) {
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

    @Override
    public String proxy() {
        return null;
    }

    @Override
    public void handleErrorRequest(Request request) {
        logger.info("Seimi got a error request={}", request);
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
