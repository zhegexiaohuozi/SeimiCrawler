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


import cn.wanghaomiao.seimi.config.SeimiConfig;
import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.spring.common.SeimiCrawlerBaseConfig;
import cn.wanghaomiao.seimi.spring.common.SeimiCrawlerBeanPostProcessor;
import cn.wanghaomiao.seimi.spring.common.SeimiCrawlerBootstrapListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


/**
 * 初始化上下文环境
 *
 * @author github.com/zhegexiaohuozi seimimaster@gmail.com
 * Date: 2015/6/26.
 */
public class SeimiContext extends AnnotationConfigApplicationContext {
    SeimiContext(SeimiConfig seimiConfig) {
        CrawlerCache.setConfig(seimiConfig);
        CrawlerCache.setSpringBoot(false);

        register(SeimiCrawlerBaseConfig.class, SeimiDefScanConfig.class, SeimiCrawlerBootstrapListener.class, SeimiCrawlerBeanPostProcessor.class);
        String[] targetPkgs = {"**/crawlers", "**/queues", "**/interceptors", "cn.wanghaomiao.seimi"};
        scan(targetPkgs);
        refresh();
    }


    public static SeimiContext init() {
        return new SeimiContext(null);
    }

    public static SeimiContext init(SeimiConfig config) {
        return new SeimiContext(config);
    }

}
