package cn.wanghaomiao.seimi.http;

/**
 * @author 汪浩淼 [et.tw@163.com]
 *         Date:  14-7-7.
 */
public enum HttpMethod {
    GET("get"),POST("post");
    private String val;
    HttpMethod(String val){
        this.val = val;
    }
    public String val(){
        return this.val;
    }
}
