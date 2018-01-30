package com.lazymc.universalproxy.process;


import com.lazymc.universalproxy.ProxyClassCreate;
import com.lazymc.universalproxy.annotation.ProxyInject;

import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by longyu on 2018/1/15.
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

class AnnotationInterfaceProcess implements IProcess {
    private ProxyClassCreate create;

    AnnotationInterfaceProcess(Elements elementUtils, Filer filer, Types typeUtils) {
        create = new ProxyClassCreate(false, ProxyClassCreate.CreateType.Interface, elementUtils, filer, typeUtils);
    }

    public boolean process(RoundEnvironment roundEnvironment) throws Exception {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ProxyInject.class);
        if (elements.isEmpty()) return false;
        for (Element element : elements) {
            // 判断是否Class
            if (element.getKind() != ElementKind.INTERFACE)
                continue;
            create.create(element);
        }

        return true;
    }
}
