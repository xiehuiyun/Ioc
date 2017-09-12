package com.example;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class ViewInjectProcessor extends AbstractProcessor {

    /*
      这里简单提一下Elemnet，我们简单认识下它的几个子类，根据下面的注释，应该已经有了一个简单认知。
      Element
        - VariableElement //一般代表成员变量
        - ExecutableElement //一般代表类中的方法
        - TypeElement //一般代表代表类
        - PackageElement //一般代表Package
    */
    private Filer mFileUtils;
    private Elements mElementUtil;
    private Messager messager;
    private Map<String, ProxyInfo> mProxyMap = new HashMap<String, ProxyInfo>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFileUtils = processingEnvironment.getFiler();//跟文件相关的辅助类，生成JavaSourceCode.
        mElementUtil = processingEnvironment.getElementUtils();//跟元素相关的辅助类，帮助我们去获取一些元素相关的信息。
        messager = processingEnvironment.getMessager();//跟日志相关的辅助类。
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //返回支持的注解类型
        Set<String> annotationType = new LinkedHashSet<String>();
        annotationType.add(BindView.class.getCanonicalName());
        return annotationType;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        //返回支持的源码版本
        return SourceVersion.latestSupported();
    }

    /*
    第一步：收集信息: 就是根据你的注解声明，拿到对应的Element，然后获取到我们所需要的信息，这个信息肯定是为了后面生成JavaFileObject所准备的。
      例如本例，我们会针对每一个类生成一个代理类，例如MainActivity我们会生成一个MainActivity$$ViewInjector。那么如果多个类中声明了注解，就对应了多个类，这里就需要：
        一个类对象，代表具体某个类的代理类生成的全部信息，本例中为ProxyInfo
        一个集合，存放上述类对象（到时候遍历生成代理类），本例中为Map<String, ProxyInfo>，key为类的全路径。
        这里的描述有点模糊没关系，一会结合代码就好理解了。
     第二步：生成代理类（本文把编译时生成的类叫代理类）:
     */


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "process...");
        mProxyMap.clear();

        Set<? extends Element> elesWithBind = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //收集信息
        for (Element element : elesWithBind) {
            checkAnnotationValid(element, BindView.class);

            //field type
            VariableElement variableElement = (VariableElement) element;
            //class type
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();//TypeElement
            //full class name
            String qualifiedName = typeElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
            if (proxyInfo == null) {
                proxyInfo = new ProxyInfo(mElementUtil, typeElement);
                mProxyMap.put(qualifiedName, proxyInfo);
            }
            BindView annotation = variableElement.getAnnotation(BindView.class);
            int id = annotation.value();
            proxyInfo.injectVariables.put(id, variableElement);
        }
        //第二步，生成代理类
        for (String key : mProxyMap.keySet()) {
            ProxyInfo info = mProxyMap.get(key);
            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(info.getProxyClassFullName(), info.getTypeElement());
                Writer writer = jfo.openWriter();
                writer.write(info.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                error(info.getTypeElement(), "Unbale to write injector for type %s: %s", info.getTypeElement(), e.getMessage());
            }
        }
        return true;
    }

    private boolean checkAnnotationValid(Element element, Class clazz) {
        if (element.getKind() != ElementKind.FIELD) {
            error(element, "%s must be declared on field.", clazz.getSimpleName());
            return false;
        }
        if (ClassValidator.isPrivate(element)) {
            error(element, "%s() must can not be private.", element.getSimpleName());
            return false;
        }
        return true;
    }

    private void error(Element element, String message, Object... agrs) {
        if (agrs.length > 0) {
            message = String.format(message, agrs);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
