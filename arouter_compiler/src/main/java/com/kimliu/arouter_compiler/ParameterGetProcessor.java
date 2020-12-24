package com.kimliu.arouter_compiler;

import com.google.auto.service.AutoService;
import com.kimliu.arouter_annotation.Parameter;
import com.kimliu.arouter_compiler.config.ProcessorConfig;
import com.kimliu.arouter_compiler.utils.ProcesserUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class) // 开启AutoService
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_ANNOTATION_PATH}) // 这个注解处理器服务的注解
public class ParameterGetProcessor extends AbstractProcessor {

    // 操作Element的工具类 （类、函数、属性其实都是Element）
    private Elements elementTool;

    // type（类）的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // 打印日志
    private Messager messager;

    // 文件生成器 生成类等
    private Filer filer;

    // 创建仓库 ：根据Activity 对所有被@Parameter注解的属性进行分类
    // key ： Activity类  value ： 该类中所有被标注的Parameter的集合
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

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

        if(!ProcesserUtils.isEmpty(annotations)){
            // 获取所有被 @ParameterGet 标记的Parameter
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);
            if(!ProcesserUtils.isEmpty(elements)){
                for (Element element : elements) {
                    // 获取属性的父节点  属性的父节点是类节点 : 该属性所在的类
                    TypeElement typeElement = (TypeElement) element.getEnclosingElement();
                    if(tempParameterMap.containsKey(typeElement)){
                        // 如果仓库中已经有了这个类，那么直接添加进去
                        tempParameterMap.get(typeElement).add(element);
                    }else{
                        // 没有 则创建
                        List<Element> fields = new ArrayList<>();
                        fields.add(element);
                        tempParameterMap.put(typeElement,fields);
                    }
                } // end for  此时，缓存中有值了

                // 如果缓存为空，说明，没有使用@Parameter 返回
                if(ProcesserUtils.isEmpty(tempParameterMap)){
                    return true;
                }

                // 如果有使用@Parameter ，那么生成相对应的类， 实现xx功能

                TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGENAME);
                TypeElement parameterType = elementTool.getTypeElement(ProcessorConfig.PARAMETER_GET_PATH);


                // 生成方法
                ParameterSpec parameterSpec =ParameterSpec.builder(TypeName.OBJECT,ProcessorConfig.PARAMETER_NAME).build();

                //
                for (Map.Entry<TypeElement,List<Element>> entry:tempParameterMap.entrySet()){
                    // key : 类 如：Personal_MainActivity
                    // value : 被@ParameterGet标记的属性
                    TypeElement typeElement = entry.getKey();

                    // 非Activity类直接报错
                    if(!typeTool.isSubtype(typeElement.asType(),activityType.asType())){
                        throw new RuntimeException("@Parameter注解目前仅限用于Activity类之上");
                    }

                    // 获取类名
                    ClassName className = ClassName.get(typeElement);


                };






            }

        }

        return false;
    }
}
