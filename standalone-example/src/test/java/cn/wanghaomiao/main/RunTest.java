package cn.wanghaomiao.main;

import cn.wanghaomiao.seimi.config.SeimiConfig;
import cn.wanghaomiao.seimi.core.Seimi;
import org.junit.Test;

/**
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2018/6/5.
 */

public class RunTest {

    @Test
    public void basicTest(){
        Seimi s = new Seimi();
        s.goRun("basic");
    }

    @Test
    public void distributedTest(){
        SeimiConfig config = new SeimiConfig();
        config.redisSingleServer().setAddress("redis://127.0.0.1:6379");
        config.setBloomFilterExpectedInsertions(1000000);
        Seimi s = new Seimi(config);
        s.goRun("distributed");
    }
}
