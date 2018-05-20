package cn.wanghaomiao.seimi.struct;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author 汪浩淼  wanghaomiao@huli.com
 * @since 2018/5/11.
 */
@FunctionalInterface
public interface SeimiCallbackFunc<T> extends Consumer<T>,Serializable {

    default SeimiCallbackFunc<T> andThen(SeimiCallbackFunc<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}
