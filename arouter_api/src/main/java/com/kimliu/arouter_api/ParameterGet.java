package com.kimliu.arouter_api;

/**
 * 定义一个接口，APT自定生成的类要继承这个接口，在这个接口里面定义规则
 */
public interface ParameterGet {

    /**
     * 目标对象.属性名 = getIntent().属性类型 完成赋值操作
     * @param targetParameter 目标对象 例如 MainActivity中的那些属性
     */
    void getParameter(Object targetParameter);

}
