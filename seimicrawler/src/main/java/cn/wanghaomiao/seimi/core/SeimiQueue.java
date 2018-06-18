/*
   Copyright 2015 Wang Haomiao<seimimaster@gmail.com>

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
package cn.wanghaomiao.seimi.core;

import cn.wanghaomiao.seimi.struct.Request;

import java.io.Serializable;

/**
 * 定义系统队列的基本接口，可自由选择实现，只要符合规范就行。
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2015/6/2.
 */
public interface SeimiQueue extends Serializable {
    /**
     * 阻塞式出队一个请求
     * @param crawlerName --
     * @return --
     */
    Request bPop(String crawlerName);
    /**
     * 入队一个请求
     * @param req 请求
     * @return --
     */
    boolean push(Request req);
    /**
     * 任务队列剩余长度
     * @param crawlerName --
     * @return num
     */
    long len(String crawlerName);

    /**
     * 判断一个URL是否处理过了
     * @param req --
     * @return --
     */
    boolean isProcessed(Request req);

    /**
     * 记录一个处理过的请求
     * @param req --
     */
    void addProcessed(Request req);

    /**
     * 目前总共的抓取数量
     * @param crawlerName --
     * @return num
     */
    long totalCrawled(String crawlerName);

    /**
     * 清除抓取记录
     * @param crawlerName --
     */
    void clearRecord(String crawlerName);
}
