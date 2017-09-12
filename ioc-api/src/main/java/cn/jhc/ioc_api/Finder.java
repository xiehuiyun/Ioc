package cn.jhc.ioc_api;

import android.app.Activity;
import android.view.View;

/**
 * Created by Administrator on 2017/9/11.
 */

public enum Finder {
    VIEW{
        @Override
        public View findView(Object source, int id){
            return ((View) source).findViewById(id);
        }
    },
    ACTIVITY{
        @Override
        public View findView(Object source,int id){
            return ((Activity) source).findViewById(id);
        }
    };

    public abstract View findView(Object source, int id);
}
