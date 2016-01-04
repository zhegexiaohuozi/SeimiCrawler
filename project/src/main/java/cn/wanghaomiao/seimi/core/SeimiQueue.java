package cn.wanghaomiao.seimi.core;
/*
   Copyright 2016 汪浩淼(Haomiao Wang)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

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
