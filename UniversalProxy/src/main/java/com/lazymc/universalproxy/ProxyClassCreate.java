package com.lazymc.universalproxy;

import com.lazymc.universalproxy.annotation.ProxyInject;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

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

public class ProxyClassCreate {
    private final CreateType type;
    private final Types typeUtils;
    private boolean isAbstract;
    private String proxyVarName;
    private Elements elementUtils;
    private Filer filer;

    public enum CreateType {
        Interface, Class
    }

    public ProxyClassCreate(boolean isAbstract, CreateType type, Elements elementUtils, Filer filer, Types typeUtils) {
        this.isAbstract = isAbstract;
        this.type = type;
        this.elementUtils = elementUtils;
        this.filer = filer;
        this.typeUtils = typeUtils;
    }

    public void create(Element element) throws Exception {
        createClass(element);
    }

    private void createClass(Element element) throws Exception {
        TypeElement typeElement = (TypeElement) element;

        List<? extends AnnotationMirror> clsAnn = typeElement.getAnnotationMirrors();
        String clsAnnStr = "";
        if (clsAnn.size() > 0) {
            for (int i = 0; i < clsAnn.size(); i++) {
                if (clsAnn.get(i).getAnnotationType().toString().equals(ProxyInject.class.getCanonicalName()))
                    continue;
                clsAnnStr += clsAnn.get(i).toString() + "\r\n";
            }
        }

        String className = typeElement.getSimpleName().toString();
        proxyVarName = "proxy_0_" + System.currentTimeMillis();
        String proxyClassName = className + ProxyFactory.suffix;

        String construct = addConstruct(className);

        String modiferStr = type == CreateType.Interface ? " implements  " : " extends ";

        StringBuilder classStr = new StringBuilder("package " + elementUtils.getPackageOf(element).getQualifiedName());
        classStr.append(";\r\n\r\n")
                .append("import ")
                .append(InvocationHandler.class.getName())
                .append(";\r\n\r\n").append(clsAnnStr)
                .append("public class ")
                .append(proxyClassName)
                .append(modiferStr)
                .append(className)
                .append("{\r\n\r\n")
                .append(construct);

        classStr.append("\r\n\r\n");
        classStr.append("private ").append(InvocationHandler.class.getName()).append(" ").append(proxyVarName).append(";\r\n");

        List<? extends Element> members = elementUtils.getAllMembers(typeElement);
        for (Element item : members) {
            if (item.getKind() == ElementKind.METHOD) {
                String methodStr = createMethod(item, className);
                if (methodStr != null)
                    classStr.append("\r\n")
                            .append(methodStr);
            }
        }

        classStr.append("\r\n}");

        try { // write the file
            JavaFileObject source = filer.createSourceFile(proxyClassName);
            Writer writer = source.openWriter();
            writer.write(classStr.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
            throw e;
        }
    }

    private String createMethod(Element item, String className) {
        ExecutableElement executableElement = (ExecutableElement) item;

        if (isValidateMethod(executableElement)) {
            String name = executableElement.getSimpleName().toString();

            List<? extends VariableElement> params = executableElement.getParameters();
            if (name.equals("finalize")) {//忽略object的回收方法
                if (params.isEmpty()) {
                    return null;
                }
            }

            List<? extends AnnotationMirror> mdAnn = executableElement.getAnnotationMirrors();
            String mdAnnStr = "";
            if (mdAnn.size() > 0) {
                for (int i = 0; i < mdAnn.size(); i++) {
                    mdAnnStr += mdAnn.get(i).toString() + "\r\n";
                }
            }

            String returnType = "";
            List<? extends TypeParameterElement> tp = executableElement.getTypeParameters();
            String executeParams = "";
            if (tp.size() > 0) {
                StringBuilder tps = new StringBuilder("<");
                String rt = executableElement.getReturnType().toString();
                for (int i = 0; i < tp.size(); i++) {
                    TypeParameterElement tpe = tp.get(i);
                    String tName = tpe.asType().toString();//泛型名称
                    if (i > 0)
                        tps.append(",");
                    tps.append(tName);
                    String te = ((TypeVariable) tp.get(0).asType()).getUpperBound().toString();//泛型继承类型
                    if (te != null && te.length() > 0) {
                        tps.append(" extends ")
                                .append(te);
                    }
                }
                tps.append(">");
                tps.append(" ");
                executeParams = tps.toString();
                returnType = rt;
            } else {
                returnType = getTypeName(executableElement.getReturnType(), false);
                if (returnType == null)
                    returnType = "java.lang.Object";
            }

            StringBuilder paramSb = new StringBuilder();
            StringBuilder paramTric = new StringBuilder();
            StringBuilder paramTricClass = new StringBuilder();
            for (int i = 0; i < params.size(); i++) {
                VariableElement element = params.get(i);
                String param = getTypeName(element.asType(), false);
                if (param == null)
                    param = "java.lang.Object";

                List<? extends AnnotationMirror> paramsAnn = element.getAnnotationMirrors();
                String varAnnStr = "";
                if (paramsAnn.size() > 0) {
                    for (int j = 0; j < paramsAnn.size(); j++) {
                        varAnnStr += paramsAnn.get(j).toString() + " ";
                    }
                }

                if (param.contains("<"))
                    param = param.substring(0, param.indexOf("<"));

                String pName = "p" + i;

                paramSb.append(varAnnStr)
                        .append(param)
                        .append(" ")
                        .append(pName);
                paramTric.append(pName);

                paramTricClass.append(param);
                paramTricClass.append(".class");


                if (i + 1 != params.size()) {
                    paramSb.append(",");
                    paramTric.append(",");
                    paramTricClass.append(",");
                }
            }

            StringBuilder method = new StringBuilder(mdAnnStr);
            method.append("public ");
            if (params.isEmpty()) {
                if (executeParams.length() > 0)
                    method.append(executeParams);
                method.append(returnType)
                        .append(" ")
                        .append(name)
                        .append("(){ \r\n try{");
                if (!returnType.equals("void")) {
                    method.append(" return (")
                            .append(returnType)
                            .append(")");
                }

                method.append("this.").append(proxyVarName).append(".invoke(this,")
                        .append(className)
                        .append(".class.getMethod(\"")
                        .append(name).append("\"),new Object[]{});}catch(Throwable e_){e_.printStackTrace();}");
                if (!returnType.equals("void")) {
                    method.append("\r\n Object result_=null;\r\n return (")
                            .append(returnType)
                            .append(")result_; }");
                } else {
                    method.append("\r\n  }");
                }
            } else {
                if (executeParams.length() > 0)
                    method.append(executeParams);
                method.append(returnType)
                        .append(" ")
                        .append(name)
                        .append("(")
                        .append(paramSb.toString())
                        .append("){ \r\n try{");
                if (!returnType.equals("void")) {
                    method.append(" return (")
                            .append(returnType)
                            .append(")");
                }

                method.append("this.").append(proxyVarName).append(".invoke(this,")
                        .append(className)
                        .append(".class.getMethod(\"")
                        .append(name)
                        .append("\",")
                        .append("new Class[]{")
                        .append(paramTricClass.toString())
                        .append("}),new Object[]{")
                        .append(paramTric.toString())
                        .append("});}catch(Throwable e_){e_.printStackTrace();}");
                if (!returnType.equals("void")) {
                    method.append("\r\n Object result_=null;\r\n return (")
                            .append(returnType)
                            .append(")result_; }");
                } else {
                    method.append("\r\n  }");
                }
            }

            return method.toString();
        }

        return null;
    }

    public String getTypeName(TypeMirror mirror, boolean usePrimitiveWrappers) {
        TypeKind kind = mirror.getKind();
        switch (kind) {
            case VOID:
                return "void";
            case DECLARED:
                Name paramType = ((TypeElement) ((DeclaredType) mirror).asElement()).getQualifiedName();

                List<? extends TypeMirror> typeArguments = ((DeclaredType) mirror).getTypeArguments();
                if (typeArguments.size() == 0)
                    return paramType.toString();
                else {
                    StringBuilder buff = new StringBuilder(paramType).append('<');
                    for (TypeMirror typeArgument : typeArguments)
                        buff.append(getTypeName(typeArgument, false));
                    return buff.append('>').toString();
                }
            case INT:
                return usePrimitiveWrappers ? Integer.class.getName() : kind.toString().toLowerCase();
            case CHAR:
                return usePrimitiveWrappers ? Character.class.getName() : kind.toString().toLowerCase();
            case BOOLEAN:
            case FLOAT:
            case DOUBLE:
            case LONG:
            case SHORT:
            case BYTE:
                String name = kind.toString().toLowerCase();
                if (usePrimitiveWrappers)
                    return "java.lang." + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                else
                    return name;
            case ARRAY:
                return getTypeName(((ArrayType) mirror).getComponentType(), false) + "[]";
            default:
                return null;
        }
    }

    private boolean isValidateMethod(ExecutableElement executableElement) {
        if (isAbstract) return executableElement.getModifiers().contains(Modifier.ABSTRACT);
        return type == CreateType.Interface ? executableElement.getModifiers().contains(Modifier.ABSTRACT) : (executableElement.getModifiers().contains(Modifier.ABSTRACT)
                || executableElement.getModifiers().contains(Modifier.PUBLIC)
                || executableElement.getModifiers().contains(Modifier.PROTECTED))
                && (!executableElement.getModifiers().contains(Modifier.STATIC)
                && !executableElement.getModifiers().contains(Modifier.FINAL));
    }

    private String addConstruct(String className) {
        return "public " + className + ProxyFactory.suffix + "(" + InvocationHandler.class.getName() + " handler" + "){super();\nthis." + proxyVarName + "=handler;}";
    }
}
