package com.lazymc.universalproxy.process;

import com.google.auto.service.AutoService;
import com.lazymc.universalproxy.annotation.ProxyInject;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

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
@AutoService(Processor.class)
public class ProxyInjectProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Filer filer;

    private List<IProcess> processList = new ArrayList<>();
    private Messager message;

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!processList.isEmpty()) {
            for (int i = 0; i < processList.size(); i++) {
                try {
                    processList.get(i).process(roundEnvironment);
                } catch (Exception e) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    PrintWriter pw = new PrintWriter(bos);
                    e.printStackTrace(pw);
                    error(bos.toString());
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ProxyInject.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        this.message = processingEnv.getMessager();
        IProcess annotationProcess = new AnnotationClassProcess(elementUtils, filer, processingEnv.getTypeUtils());
        processList.add(annotationProcess);
        annotationProcess = new AnnotationInterfaceProcess(elementUtils, filer, processingEnv.getTypeUtils());
        processList.add(annotationProcess);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public void error(String msg) {
        message.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    public void addProcess(IProcess process) {
        processList.add(process);
    }

    public Filer getFiler() {
        return filer;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }
}
