package com.lazymc.universalproxy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //接口测试
        try {
            final InterfaceTest interfaceTestHost = new InterfaceTest() {
                @Override
                public String test() {
                    return null;
                }
            };
            InterfaceTest interfaceTest = ProxyFactory.createProxy(InterfaceTest.class, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return method.invoke(interfaceTestHost, args);
                }
            });
            interfaceTest.test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //类测试
        final ClassTest classTestHost = new ClassTest();
        try {
            ClassTest classTest = ProxyFactory.createProxy(ClassTest.class, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return method.invoke(classTestHost, args);
                }
            });
            classTest.test("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
