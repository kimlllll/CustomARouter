package com.kimliu.arouter_compiler;


import com.kimliu.arouter_compiler.config.ProcessorConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;

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




    public void addFristStatement(){
        method.addStatement("$T t = ($T)" + ProcessorConfig.PARAMETER_NAME ,className , className);
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
