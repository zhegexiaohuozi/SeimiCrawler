package cn.wanghaomiao.seimi.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * StrFormatUtil Tester.
 *
 * @author <seimimaster@gmail.com>
 * @version 1.0
 */
public class StrFormatUtilTest {

    /**
     * Method: parseCharset(String target)
     */
    @Test
    public void testParseCharset() throws Exception {
        String t = "text/html; charset=utf-16;text/html; charset=utf-8";
        String r = StrFormatUtil.parseCharset(t);
        System.out.println(r);
        Assert.assertTrue("utf-8".equals(r));
    }
}
