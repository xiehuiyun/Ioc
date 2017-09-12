package cn.jhc.ioc_api;

/**
 * Created by Administrator on 2017/9/11.
 */

public interface ViewInject<T> {
    void inject(T t, Object source);
}
