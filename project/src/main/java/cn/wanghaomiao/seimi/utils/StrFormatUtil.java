package cn.wanghaomiao.seimi.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/7/21.
 */
public class StrFormatUtil {
    public static String info(String pattern,Object... params){
        for (Object p:params){
            pattern = StringUtils.replaceOnce(pattern, "{}", p.toString());
        }
        return pattern;
    }
}
