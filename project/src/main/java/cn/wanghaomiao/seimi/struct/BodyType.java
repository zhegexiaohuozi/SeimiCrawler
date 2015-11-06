package cn.wanghaomiao.seimi.struct;

/**
 * @author 汪浩淼 [et.tw@163.com]
 */
public enum BodyType {
    BINARY("binary"),TEXT("text");
    private String val;
    private BodyType(String type){
        this.val = type;
    }
    public String val() {
        return this.val;
    }
}
