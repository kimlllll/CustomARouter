package com.kimliu.arouter_annotation.bean;


import javax.lang.model.element.Element;

/**
 * 路由器 路径Path的最终封装类
 */
public class RouteBean {

    // 被注解的类的类型
    public enum TypeEnum{
        ACTIVITY
    }


    private TypeEnum typeEnum;
    private Element elementType;// 类节点
    private Class<?> clazz; // 被注解的Class对象
    private String path;// 路径
    private String group; // 组

    public Class<?> getClazz() {
        return clazz;
    }

    public Element getElementType() {
        return elementType;
    }

    public String getGroup() {
        return group;
    }

    public String getPath() {
        return path;
    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void setElementType(Element elementType) {
        this.elementType = elementType;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public RouteBean(TypeEnum typeEnum, /*Element element,*/ Class<?> clazz, String path, String group){
        this.typeEnum = typeEnum;
        // this.element = element;
        this.clazz = clazz;
        this.path = path;
        this.group = group;
    }

    // 对外暴露
    // 对外提供简易版构造方法，主要是为了方便APT生成代码
    public static RouteBean create(TypeEnum type, Class<?> clazz, String path, String group) {
        return new RouteBean(type, clazz, path, group);
    }

    // 构建者模式代码

    private RouteBean(Builder builder) {
        this.typeEnum = builder.type;
        this.elementType = builder.element;
        this.clazz = builder.clazz;
        this.path = builder.path;
        this.group = builder.group;
    }


    public static class Builder{
        // 枚举类型：Activity
        private TypeEnum type;
        // 类节点
        private Element element;
        // 注解使用的类对象
        private Class<?> clazz;
        // 路由地址
        private String path;
        // 路由组
        private String group;

        public Builder addType(TypeEnum type) {
            this.type = type;
            return this;
        }

        public Builder addElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder addClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder addPath(String path) {
            this.path = path;
            return this;
        }

        public Builder addGroup(String group) {
            this.group = group;
            return this;
        }

        // 最后的build或者create，往往是做参数的校验或者初始化赋值工作
        public RouteBean build() {
            if (path == null || path.length() == 0) {
                throw new IllegalArgumentException("path必填项为空，如：/app/MainActivity");
            }
            return new RouteBean(this);
        }
    }

    @Override
    public String toString() {
        return "RouteBean{" +
                "typeEnum=" + typeEnum +
                ", elementType=" + elementType +
                ", clazz=" + clazz +
                ", path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
