package com.lazymc.universalproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by longyu on 2018/1/16.
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　＞　　　＜　┃
 * ┃　　　　　　　┃
 * ┃...　⌒　...　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * ┃　　　┃
 * ┃　　　┃
 * ┃　　　┃
 * ┃　　　┃  神兽保佑
 * ┃　　　┃  代码无bug
 * ┃　　　┃
 * ┃　　　┗━━━┓
 * ┃　　　　　　　┣┓
 * ┃　　　　　　　┏┛
 * ┗┓┓┏━┳┓┏┛
 * ┃┫┫　┃┫┫
 * ┗┻┛　┗┻┛
 * <p>
 * 如果生命可以延续，代码也将永无止境。
 * bug的不期而遇，请接受加班的惩罚。
 */

public class ProxyFactory {
    static final Object suffix = "$$Proxy0";

    public static <T> T createProxy(Class<T> cls, final InvocationHandler handler) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (T) Class.forName(cls.getName() + suffix).getConstructor(InvocationHandler.class).newInstance(handler);
    }
}
