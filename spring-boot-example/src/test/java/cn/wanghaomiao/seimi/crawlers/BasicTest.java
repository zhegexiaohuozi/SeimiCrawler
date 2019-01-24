package cn.wanghaomiao.seimi.crawlers;

import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class BasicTest {

    @Test
    public void startTest(){

        CrawlerCache.consumeRequest(Request.build("http://www.cnblogs.com/","start").setCrawlerName("basic_a"));
        try {
            Thread.sleep(1000*30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
