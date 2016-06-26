package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/6/26.
 */
public interface SeimiDownloader {
    /**
     * 处理抓取请求生成response
     */
    Response process(Request request) throws Exception;

    /**
     * 处理meta标签refresh场景
     *
     * @param nextUrl 重定向URL
     * @return 请求的最终返回体
     */
    Response metaRefresh(String nextUrl) throws Exception;

    /**
     * http请求状态
     * @return http状态码
     */
    int statusCode();

}
