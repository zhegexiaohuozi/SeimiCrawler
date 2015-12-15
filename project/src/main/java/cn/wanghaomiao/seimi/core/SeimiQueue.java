package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.struct.Request;

/**
 * 定义系统队列的基本接口，可自由选择实现，只要符合规范就行。
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/6/2.
 */
public interface SeimiQueue {
    /**
     * 阻塞式出队一个请求
     * @return
     */
    Request bPop(String crawlerName);
    /**
     * 入队一个请求
     * @param req
     * @return
     */
    boolean push(Request req);
    /**
     * 任务队列剩余长度
     * @return
     */
    long len(String crawlerName);

    /**
     * 判断一个URL是否处理过了
     * @param req
     * @return
     */
    boolean isProcessed(Request req);

    /**
     * 记录一个处理过的请求
     * @param req
     */
    void addProcessed(Request req);

    /**
     * 目前总共的抓取数量
     * @return
     */
    long totalCrawled(String crawlerName);
}
