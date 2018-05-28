SeimiCrawler
============
[![GitHub release](https://img.shields.io/github/release/zhegexiaohuozi/SeimiCrawler.svg)](https://github.com/zhegexiaohuozi/JsoupXpath/releases)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

An agile,powerful,standalone,distributed crawler framework.Support spring boot and redisson.

SeimiCrawler的目标是成为Java里最实用的爬虫框架，大家一起加油。

# 简介 #

SeimiCrawler是一个敏捷的，独立部署的，支持分布式的Java爬虫框架，希望能在最大程度上降低新手开发一个可用性高且性能不差的爬虫系统的门槛，以及提升开发爬虫系统的开发效率。在SeimiCrawler的世界里，绝大多数人只需关心去写抓取的业务逻辑就够了，其余的Seimi帮你搞定。设计思想上SeimiCrawler受Python的爬虫框架Scrapy启发，同时融合了Java语言本身特点与Spring的特性，并希望在国内更方便且普遍的使用更有效率的XPath解析HTML，所以SeimiCrawler默认的HTML解析器是[JsoupXpath](http://jsoupxpath.wanghaomiao.cn)(独立扩展项目，非jsoup自带),默认解析提取HTML数据工作均使用XPath来完成（当然，数据处理亦可以自行选择其他解析器）。并结合[SeimiAgent](https://github.com/zhegexiaohuozi/SeimiAgent)彻底完美解决复杂动态页面渲染抓取问题。

# V2.0版本新特性 #

- 支持 spring boot，同时也依然保留可以独立的启动运行
- 支持方法引用，更自然方式去设置回调函数
- 分布式消息队列改用 Redisson（基于redis的分布式计算框架） 实现
- 分布式场景去重，默认采用 BloomFilter ，参数可自行配置

# 原理示例 #
## 基本原理 ##
![SeimiCrawler原理图](http://img.wanghaomiao.cn/v2_Seimi.png)

## 集群原理 ##
![SeimiCrawler集群原理图](http://img.wanghaomiao.cn/v1_distributed.png)

# 快速开始 #

添加maven依赖(中央maven库最新版本1.3.5)：
```
<dependency>
    <groupId>cn.wanghaomiao</groupId>
    <artifactId>SeimiCrawler</artifactId>
    <version>1.3.5</version>
</dependency>
```

在包`crawlers`下添加爬虫规则，例如：
```
@Crawler(name = "basic")
public class Basic extends BaseSeimiCrawler {
    @Override
    public String[] startUrls() {
        return new String[]{"http://www.cnblogs.com/"};
    }
    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> urls = doc.sel("//a[@class='titlelnk']/@href");
            logger.info("{}", urls.size());
            for (Object s:urls){
                push(new Request(s.toString(),"getTitle"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getTitle(Response response){
        JXDocument doc = response.document();
        try {
            logger.info("url:{} {}", response.getUrl(), doc.sel("//h1[@class='postTitle']/a/text()|//a[@id='cb_post_title_url']/text()"));
            //do something
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
然后随便某个包下添加启动Main函数，启动SeimiCrawler：
```
public class Boot {
    public static void main(String[] args){
        Seimi s = new Seimi();
        s.start("basic");
    }
}
```
以上便是一个最简单的爬虫系统开发流程。

## 工程化打包部署 ##
上面可以方便的用来开发或是调试，当然也可以成为生产环境下一种启动方式。但是，为了便于工程化部署与分发，SeimiCrawler提供了专门的打包插件用来对SeimiCrawler工程进行打包，打好的包可以直接分发部署运行了。

pom中添加添加plugin
```
<plugin>
    <groupId>cn.wanghaomiao</groupId>
    <artifactId>maven-seimicrawler-plugin</artifactId>
    <version>1.2.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>build</goal>
            </goals>
        </execution>
    </executions>
    <!--<configuration>-->
        <!-- 默认target目录 -->
        <!--<outputDirectory>/some/path</outputDirectory>-->
    <!--</configuration>-->
</plugin>
```
执行`mvn clean package`即可，打好包目录结构如下：
```
.
├── bin             # 相应的脚本中也有具体启动参数说明介绍，在此不再敖述
│   ├── run.bat    #windows下启动脚本
│   └── run.sh     #Linux下启动脚本
└── seimi
    ├── classes     #Crawler工程业务类及相关配置文件目录
    └── lib         #工程依赖包目录
```
接下来就可以直接用来分发与部署了。

> 详细请继续参阅[maven-seimicrawler-plugin](https://github.com/zhegexiaohuozi/maven-seimicrawler-plugin)

# 更多文档 #

目前可以参考demo工程中的样例，基本包含了主要的特性用法。更为细致的文档移步[SeimiCrawler主页](http://seimi.wanghaomiao.cn)中进一步查看

# 社区讨论 #
大家有什么问题或建议现在都可以选择通过下面的邮件列表讨论，首次发言前需先订阅并等待审核通过（主要用来屏蔽广告宣传等）

- 订阅:请发邮件到 `seimicrawler+subscribe@googlegroups.com`

- 发言:请发邮件到 `seimicrawler@googlegroups.com`

- 退订:请发邮件至 `seimicrawler+unsubscribe@googlegroups.com`

- QQ群:`557410934`

![QQ群](http://wjcdn.u.qiniudn.com/seimiqq.png)

这个就是给大家自由沟通啦

- 微信订阅号

![weixin](http://wjcdn.u.qiniudn.com/seimiweixin.jpg)

里面会发布一些使用案例等文章，以及seimi体系相关项目的最新更新动态等。

# Change log #

请参阅 [ChangeLog.md](https://github.com/zhegexiaohuozi/SeimiCrawler/blob/master/ChangeLog.md)

# 项目源码 #
[Github](https://github.com/zhegexiaohuozi/SeimiCrawler)
> **BTW:**
> 如果您觉着这个项目不错，到github上`star`一下，我是不介意的 ^_^
