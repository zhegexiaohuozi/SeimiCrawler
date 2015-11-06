package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.dao.StoreToDbDAO;
import cn.wanghaomiao.model.BlogContent;
import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 将解析出来的数据直接存储到数据库中
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/21.
 */
@Crawler(name = "storedb")
public class StoreInDB extends BaseSeimiCrawler {
    @Autowired
    private StoreToDbDAO storeToDbDAO;

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
                push(Request.build(s.toString(),"renderBean"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void renderBean(Response response){
        try {
            BlogContent blog = response.render(BlogContent.class);
            logger.info("bean resolve res={},url={}",blog,response.getUrl());
            //使用神器paoding-jade存储到DB
            int blogId = storeToDbDAO.save(blog);
            logger.info("store sus,blogId = {}",blogId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
