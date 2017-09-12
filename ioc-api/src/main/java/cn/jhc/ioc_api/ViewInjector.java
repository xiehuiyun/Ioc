package cn.jhc.ioc_api;

import android.app.Activity;

/** API干的两件事：
 * 根据传入的host寻找我们生成的代理类：例如MainActivity->MainActity$$ViewInjector。
 *强转为统一的接口，调用接口提供的方法。
 */

public class ViewInjector {
    private static final String SUFFIX = "$$ViewInject";

    public static void injectView(Activity activity){
        ViewInject proxyActivity = findProxyActivity(activity);
    }

    private static ViewInject findProxyActivity(Object activity) {
        try {
            Class clazz = activity.getClass();
            Class injectorClass = Class.forName(clazz.getName() + SUFFIX);
            return (ViewInject) injectorClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s ,something when compiler.",activity.getClass().getSimpleName() + SUFFIX));
    }
}
