package com.kimliu.arouter_compiler;

import com.google.auto.service.AutoService;
import com.kimliu.arouter_compiler.config.ProcessorConfig;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({ProcessorConfig.PARAMETERGET_PATH})
public class ParameterGetProcessor extends AbstractProcessor {

    // 操作Element的工具类 （类、函数、属性其实都是Element）
    private Elements elementTool;

    // type（类）的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // 打印日志
    private Messager messager;

    // 文件生成器 生成类等
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnv);
        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }
}
