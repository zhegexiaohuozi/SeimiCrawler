package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.struct.Response;
import org.apache.http.client.CookieStore;

/**
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/5/28.
 */
public interface SeimiCrawler {

    public String getUserAgent();

    /**
     * 如果开启cookies通过此方法获取cookiesStore
     * @return CookieStore
     */
    public CookieStore getCookieStore();
    /**
     * 设置起始url
     * @return
     */
    public String[] startUrls();

    /**
     * 用于设置允许的请求URL匹配规则
     * @return 白名单规则正则表达式列表
     */
    public String[] allowRules();

    /**
     * 用于设置要放弃访问的请求URL匹配规则
     * @return 黑名单规则正则表达式列表
     */
    public String[] denyRules();
    /**
     * 针对startUrl生成首批的response回调这个初始接口
     * @param response
     * @return
     */
    public void start(Response response);
}
