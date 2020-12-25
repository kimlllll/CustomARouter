package com.kimliu.arouter_compiler;


import com.kimliu.arouter_annotation.Parameter;
import com.kimliu.arouter_compiler.config.ProcessorConfig;
import com.kimliu.arouter_compiler.utils.ProcesserUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 *        目的 生成以下代码：
 *         @Override
 *         public void getParameter(Object targetParameter) {
 *               Personal_MainActivity t = (Personal_MainActivity) targetParameter;
 *               t.name = t.getIntent().getStringExtra("name");
 *               t.sex = t.getIntent().getStringExtra("sex");
 *         }
 */
public class ParameterFactory {

    // 方法的构建
    private MethodSpec.Builder method;

    // 类名，如：MainActivity  /  Personal_MainActivity
    private ClassName className;

    // Messager用来报告错误，警告和其他提示信息
    private Messager messager;

    private ParameterFactory(Builder builder){
        this.messager = builder.messager;
        this.className = builder.className;

        method = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);

    }


    /**
     *  生成第一行代码
     *  Personal_MainActivity t = (Personal_MainActivity) targetParameter;
     */
    public void addFristStatement(){
        method.addStatement("$T t = ($T)" + ProcessorConfig.PARAMETER_NAME ,className , className);
    }

    /**
     * 生成下面的循环体
     *  t.name = t.getIntent().getStringExtra("name");
     */
    public void buildStatement(Element element){
        // 遍历注解的属性节点，生成函数体
        TypeMirror typeMirror = element.asType();

        // 获取 TypeKind 枚举类型的序列号
        int type = typeMirror.getKind().ordinal();
        messager.printMessage(Diagnostic.Kind.NOTE,"type:"+type);

        // 获取属性名 name sex age
        String fieldName = element.getSimpleName().toString();

        // 获取注解的值
        String annotationName = element.getAnnotation(Parameter.class).name();

        // 如果注解的值为空，那么使用属性值
        annotationName = ProcesserUtils.isEmpty(annotationName)? fieldName:annotationName;


        String finalValue = "t." + fieldName;

        /* t.s = t.getIntent().getStringExtra("name") */
        String methodContent = finalValue +" = t.getIntent().";


        // 添加后面的getIntExtra、 getStringExtra等 使用TypeKind
        if(type == TypeKind.INT.ordinal()){
            // int 类型
            // t.s = t.getIntent().getIntExtra("age", t.age);
            methodContent += "getIntExtra($S " + finalValue + ")";
        }else if(type == TypeKind.BOOLEAN.ordinal()){
            // boolean 类型
            // t.s = t.getIntent().getBooleanExtra("isSuccess", t.age);
            methodContent += "getBooleanExtra($S" + finalValue + ")";
        }else if(type == TypeKind.DOUBLE.ordinal()){
            // Double类型
            methodContent += "getDoubleExtra($S" + finalValue + ")";
        }else if(type == TypeKind.FLOAT.ordinal()){
            // Float类型
            methodContent += "getFloatExtra($S" + finalValue + ")";
        }else if(type == TypeKind.SHORT.ordinal()){
            // short类型
            methodContent += "getShortExtra($S" + finalValue + ")";
        }else{
            // String 类型  t.s = t.getIntent.getStringExtra("s");
            // typeMirror.toString() java.lang.String
            if(typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)){
                // String 类型
                methodContent += "getStringExtra($S)"; //没有默认值
            }
        }

        if(methodContent.endsWith(")")){
            method.addStatement(methodContent,annotationName);
        }else{
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂不支持byte、char类型参数");
        }
    }

    public MethodSpec build(){
        return method.build();
    }


    public static class Builder{

        // Messager用来报告错误，警告和其他提示信息
        private Messager messager;

        // 类名，如：MainActivity
        private ClassName className;

        // 方法参数体
        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public ParameterFactory build() {
            if (parameterSpec == null) {
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (className == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (messager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }

            return new ParameterFactory(this);
        }
    }
}
