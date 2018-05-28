package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import org.seimicrawler.xpath.JXDocument;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;

/**
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
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
                push(Request.build(s.toString(),StoreInFile::saveFile));
            }
            List<Object> imgs = doc.sel("//a/img/@src");
            for (Object u:imgs){
                push(Request.build(u.toString(),StoreInFile::saveFile));
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
