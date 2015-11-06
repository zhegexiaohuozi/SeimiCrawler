package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

/**
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/21.
 */
@Crawler(name = "savefile")
public class StoreInFile extends BaseSeimiCrawler {
    @Override
    public String[] startUrls() {
        return new String[]{"http://www.cnblogs.com/"};
    }

    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> urls = doc.sel("//a[@class='titlelnk']/@href");
            logger.info("{}",urls.size());
            for (Object s:urls){
                push(new Request(s.toString(),"saveFile"));
            }
            List<Object> imgs = doc.sel("//a/img/@src");
            for (Object u:imgs){
                push(new Request(u.toString(),"saveFile"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveFile(Response response){
        try {
            String fileName = StringUtils.substringAfterLast(response.getUrl(),"/");
            String path = "d:/temp/cnblogs/"+fileName;
            response.saveTo(new File(path));
            logger.info("file done = {}",fileName);
        }catch (Exception e){
            //
        }
    }
}
