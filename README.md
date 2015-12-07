SeimiCrawler
==========
An agile,powerful,distributed crawler framework.

SeimiCrawler的目标是成为Java世界最好用最实用的爬虫框架。

# 简介 #

SeimiCrawler是一个敏捷的，支持分布式的爬虫开发框架，希望能在最大程度上降低新手开发一个可用性高且性能不差的爬虫系统的门槛，以及提升开发爬虫系统的开发效率。在SeimiCrawler的世界里，绝大多数人只需关心去写抓取的业务逻辑就够了，其余的Seimi帮你搞定。设计思想上SeimiCrawler受Python的爬虫框架Scrapy启发很大，同时融合了Java语言本身特点与Spring的特性，并希望在国内更方便且普遍的使用更有效率的XPath解析HTML，所以SeimiCrawler默认的HTML解析器是[JsoupXpath](http://jsoupxpath.wanghaomiao.cn)(独立扩展项目，非jsoup自带),默认解析提取HTML数据工作均使用XPath来完成（当然，数据处理亦可以自行选择其他解析器）。

# 原理示例 #
## 基本原理 ##
![SeimiCrawler原理图](http://77g8ty.com1.z0.glb.clouddn.com/v2_Seimi.png)

## 集群原理 ##
![SeimiCrawler集群原理图](http://77g8ty.com1.z0.glb.clouddn.com/v1_distributed.png)

# 快速开始 #

添加maven依赖(中央maven库最新版本0.2.3)：
```
<dependency>
    <groupId>cn.wanghaomiao</groupId>
    <artifactId>SeimiCrawler</artifactId>
    <version>0.2.3</version>
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

# 更多文档 #

目前可以参考demo工程中的样例，基本包含了主要的特性用法。更为细致的文档移步[SeimiCrawler主页](http://seimi.wanghaomiao.cn)中进一步查看

# Change log #
## v0.2.4 ##
- 自动跳转增强除301,302外增加支持识别通过meta refresh方式的页面跳转
- `Response`对象增加通过`getRealUrl()`获取内容对应重定向以及跳转后的真实连接
- 通过注解@Crawler中'useUnrepeated'属性控制是否启用系统级去重机制，默认开启

## v0.2.3 ##
- 支持自定义动态代理
开发者可以通过覆盖`BaseSeimiCrawler.proxy()`来自行决定每次请求所使用的代理，覆盖该方法并返回有效代理地址则`@Crawler`中`proxy`属性失效。
- 添加动态代理，动态User-Agent使用demo

## v0.2.2 ##
- 增强对不规范网页的编码识别与兼容能力

## v0.2.1 ##
- 优化黑白名单正则过滤机制

## v0.2.0 ##
- 增加支持内嵌http服务API提交json格式的Request请求
- 增加针对请求URL进行校验的`allowRules`和`denyRules`的自定义设置，即白名单规则和黑名单规则，格式均为正则表达式。默认为null不进行检查
- 增加对Request的合法性的统一校验
- 增加支持请求间的delay时间设置

# 项目源码 #
[Github](https://github.com/zhegexiaohuozi/SeimiCrawler)
> **BTW:**
> 如果您觉着这个项目不错，到github上`star`一下，我是不介意的 ^_^

# 联系我 #
- site: [www.wanghaomiao.cn](http://www.wanghaomiao.cn)
- email：`et.tw#163.com`