# Change log #

## v1.3.0 ##
- 支持在`Request`对象中，通过`header`(map)来自定义本次请求的header，以及支持通过`seimiCookies`来自定义cookies，自定义cookies会直接进入cookiesStore，对同域下第二次请求依然有效

- 优化默认启动方式，改造`cn.wanghaomiao.seimi.boot.Run`支持`CommandLineParser`，可以使用 `-c` 和`-p`来传参，其中`-c`用来指定crawlernames，多个用','分隔，`-p`指定一个端口，可以选择性的启动一个内嵌的http服务，并开启使用内嵌http接口

- `maven-compiler-plugin`打包插件升级为1.3.0，完善Linux下的脚本，并增加启动配置文件，可以到[maven-seimicrawler-plugin主页详细查看](https://github.com/zhegexiaohuozi/maven-seimicrawler-plugin)

- 默认下载器改为Apache Httpclient,备用为下载器OkHttp3实现

- 优化部分代码

- demo日志默认全部输出至控制台


## v1.2.0 ##
- `OkhttpDownloader`支持处理contentType头中没有指定编码的中文页面
- 支持通过`@Crawler`注解中的`httpTimeOut`属性自定义http请求的超时时间，默认15000ms

## v1.1.0 ##
- 可通过实现`SeimiCrawler`的`List<Request> startRequests();`来实现更复杂的起始触发请求
- SemiQueue实现按需加载
- 修复抓取文件类型数据返回时尝试匹配`meta refresh`时产生的问题

## v1.0.0 ##
- http请求处理器重构，并默认改由`okhttp3`实现，且支持通过`@Crawler`注解中的`httpType`自由切换为apache httpclient
- 部分代码优化
- 支持通过[seimiAgent](https://github.com/zhegexiaohuozi/SeimiAgent)获取页面快照（png/pdf）
- 升级[JsoupXpath](https://github.com/zhegexiaohuozi/JsoupXpath)版本至`v0.3.1`

> 这一版是SeimiCrawler比较重大的一次更新，伴之而来的亦是更强悍的抓取体验。

## v0.3.2 ##
- 优化分布式模式下与redis的连接,增强分布式可靠性
- bug fix

## v0.3.0 ##
- 内置支持[SeimiAgent](https://github.com/zhegexiaohuozi/SeimiAgent)，完美解决动态页面渲染抓取问题
- 修复自动跳转在某些情况存在的bug

## v0.2.7 ##
- 内嵌http接口在可以接收单个Json形式Request基础上增加支持接收Json数组形式的多个Request
- `Request`对象支持设置`skipDuplicateFilter`用来告诉seimi处理器跳过去重机制，默认不跳过
- 增加定时调度使用Demo
- 回调函数通过Request传递自定义参数值类型由Object改为String，方便明确处理
- Fix:修复一个打日志的bug

## v0.2.6 ##
- 增加统一的启动入口类，配合未来SeimiCrawler的maven构建plugin一起使用
- meta refresh方式跳转优化，设置最多上限为3次，防止遇到持续刷新页面无法跳出
- bug fix:修复在Request中自定义数据无法传向Response的问题

## v0.2.5 ##
- 增加请求遭遇严重异常时重新打回队列处理机制
当一个请求在经历网络请求异常的重试机制后依然出现非预期异常，那么这个请求会在不超过开发者设置的或是默认的最大重新处理次数的情况下被打回队列重新等待被处理，如果被打回次数达到了最大限制，那么seimi会调用开发者自行覆盖实现的`BaseSeimiCrawler.handleErrorRequest(Request request)`来处理记录这个异常的请求。重新打回等待处理机制配合delay功能使用可以在很大程度上避免因访问站点的反爬虫策略引起的请求处理异常，并丢失请求的记录的情况。
- 优化去重判断
- 优化不规范页面的编码获取方式

## v0.2.4 ##
- 自动跳转增强，除301,302外增加支持识别通过meta refresh方式的页面跳转
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