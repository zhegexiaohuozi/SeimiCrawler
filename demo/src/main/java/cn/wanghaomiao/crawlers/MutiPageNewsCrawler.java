package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 内分页文章整合抓取
 * @since 2016/6/14.
 */
@Crawler(name = "mutipagenews")
public class MutiPageNewsCrawler extends BaseSeimiCrawler {
    @Override
    public String[] startUrls() {
        return new String[]{"http://bbs.miercn.com/bd/201606/thread_744157_1.html"};
    }

    @Override
    public void start(Response response) {
        try {
            JXDocument document = response.document();
            //获取上游处理函数传过来的数据，如果为空则初始化一个，一会要用来向下游传递数据
            Map<String,String> meta = response.getMeta()!=null?response.getMeta():new HashMap<String, String>();
            String preBody = meta.get("body")!=null?meta.get("body"):"";
            meta.put("body",preBody+StringUtils.join(document.sel("//div[@id='cc2']//text()"),""));

            String urlPrefix = response.getUrl().substring(0,response.getUrl().lastIndexOf("/")+1);
            //拿到下一页的地址后缀
            String nextPage = StringUtils.join(document.sel("//div[@class='page1']/a[text()*='下一页']/@href"),"");
            if (nextPage.matches(".*thread_\\d+.*")){
                //用这一个回调函数就够了
                Request req = Request.build(urlPrefix+nextPage,"start");
                logger.info("nextPage={}",req.getUrl());
                //用来向下游回调函数传送数据
                req.setMeta(meta);
                push(req);
            }else {
                //已经收集完毕
                logger.info("完整新闻体为：{}",meta.get("body"));
            }
        }catch (Exception e){
            logger.debug(e.getMessage(),e);
        }
    }
}
