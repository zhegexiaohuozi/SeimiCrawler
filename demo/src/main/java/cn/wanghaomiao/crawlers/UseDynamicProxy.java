package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.apache.commons.lang.math.RandomUtils;

import java.util.List;

/**
 * 使用动态代理，每次请求随机选取一个代理，当实现动态代理方法，注解中代理配置则失效（如果配了）
 * e.g. http://user:passwd@host:port
 *      https://user:passwd@host:port
 *      socket://user:passwd@host:port
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/21.
 */
@Crawler(name = "dyProxy")
public class UseDynamicProxy extends BaseSeimiCrawler {
    @Override
    public String[] startUrls() {
        return new String[]{"http://www.cnblogs.com/"};
    }

    @Override
    public String proxy() {
        String[] proxies = new String[]{"socket://127.0.0.1:8888","http://127.0.0.1:8880"};
        return proxies[RandomUtils.nextInt()%proxies.length];
    }

    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> urls = doc.sel("//a[@class='titlelnk']/@href");
            logger.info("{}", urls.size());
            for (Object s:urls){
                push(new Request(s.toString(),"getTitle"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getTitle(Response response){
        JXDocument doc = response.document();
        try {
            logger.info("url:{} {}", response.getUrl(), doc.sel("//h1[@class='postTitle']/a/text()|//a[@id='cb_post_title_url']/text()"));
            //do something
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
