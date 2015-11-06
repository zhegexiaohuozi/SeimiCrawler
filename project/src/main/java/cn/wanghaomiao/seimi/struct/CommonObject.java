package cn.wanghaomiao.seimi.struct;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2015/5/31.
 */
public class CommonObject implements Serializable {
    private static final long serialVersionUID = -3239260503197729552L;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
