package cn.wanghaomiao.seimi.struct;

import cn.wanghaomiao.seimi.http.HttpMethod;

import java.util.Map;

/**
 * 封装一个抓取请求的基本信息体
 * @author 汪浩淼 [et.tw@163.com]
 *         Date:  14-7-7.
 */
public class Request extends CommonObject {
    public Request(String url, String callBack, HttpMethod httpMethod, Map<String, String> params, Map<String, Object> meta) {
        this.url = url;
        this.httpMethod = httpMethod;
        this.params = params;
        this.meta = meta;
        this.callBack = callBack;
    }

    public Request(String url, String callBack) {
        this.url = url;
        this.callBack = callBack;
    }

    public static Request build(String url, String callBack, HttpMethod httpMethod, Map<String, String> params, Map<String, Object> meta){
        return new Request(url, callBack, httpMethod, params, meta);
    }

    public static Request build(String url, String callBack){
        return new Request(url, callBack);
    }

    public Request(){
        super();
    }

    private String crawlerName;
    /**
     * 需要请求的url
     */
    private String url;
    /**
     * 要请求的方法类型 get,post,put...
     */
    private HttpMethod httpMethod;
    /**
     * 如果请求需要参数，那么将参数放在这里
     */
    private Map<String,String> params;
    /**
     * 这个主要用于存储向下级回调函数传递的一些自定义数据
     */
    private Map<String,Object> meta;
    /**
     * 回调函数方法名
     */
    private String callBack;
    /**
     * 是否停止的信号，收到该信号的处理线程会退出
     */
    private boolean stop = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public String getCallBack() {
        return callBack;
    }

    public void setCallBack(String callBack) {
        this.callBack = callBack;
    }

    public String getCrawlerName() {
        return crawlerName;
    }

    public void setCrawlerName(String crawlerName) {
        this.crawlerName = crawlerName;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
