package cn.wanghaomiao.seimi.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author github.com/zhegexiaohuozi et.tw@163.com
 * @since 2023/4/18.
 */
public class SysEnvUtil {
    /**
     * 自定义全局工作线程池绝对线程数量
     */
    private final static String absoluteThreadNumJvmOpt = "seimi.crawler.thread-num";
    private final static Pattern numCheck = Pattern.compile("\\d+");
    private static Logger logger = LoggerFactory.getLogger(SysEnvUtil.class);

    public static int customThreadNum(){
        int cfgThreadNum = 0;
        String cfg = System.getProperty(absoluteThreadNumJvmOpt, "");
        Matcher matcher = numCheck.matcher(cfg);
        if (matcher.matches()){
            int cfgN = Integer.parseInt(cfg);
            if (cfgN > 0){
                logger.info("发现自定义线程数：{}，将以此值进行配置。", cfgN);
                cfgThreadNum = cfgN;
            }
        }
        return cfgThreadNum;
    }
}
