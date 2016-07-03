package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;

import java.util.List;

/**
 * 使用seimicrawler提供的默认redis队列实现，需在配置文件（标准的spring配置文件[文件名应以applicationContext开头]）中注入redis的地址端口等信息
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/21.
 */
//需要使用时打开注释
//@Crawler(name = "DefRedis",queue = DefaultRedisQueue.class,useUnrepeated = false)
public class DefaultRedisQueueEG extends BaseSeimiCrawler {
    @Override
    public String[] startUrls() {
        return new String[]{"http://www.cnblogs.com/"};
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
