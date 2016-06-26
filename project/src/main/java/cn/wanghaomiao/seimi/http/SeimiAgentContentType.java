package cn.wanghaomiao.seimi.http;

/**
 * 用于指定SeimiAgent处理请求返回的内容的数据格式
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/6/26.
 */
public enum SeimiAgentContentType {
    /**
     * 向SeimiAgent请求返回内容为HTML
     */
    HTML(1),
    /**
     * 向SeimiAgent请求返回内容为图片，实际图片格式为png
     */
    IMG(2),
    /**
     * 向SeimiAgent请求返回内容为PDF
     */
    PDF(3);
    private int val;
    SeimiAgentContentType(int val){
        this.val = val;
    }
    public int val(){
        return this.val;
    }
}
