package com.kimliu.arouter_compiler.config;

/**
 * 用来记录一些名称 路径等
 */
public interface ProcessorConfig {

    // ARouter 的路径
    String AROUTE_PATH = "com.kimliu.arouter_annotation.ARouter";

    // 接收参数 key 接收每个Module传递过来的 组名
    String OPTIONS = "moduleName";

    // 包名
    String PACKAGENAMEFORAPT = "packageNameForAPT";

    // Activity 全类名
    String ACTIVITY_PACKAGENAME = "android.app.Activity";


    // Group 标记
    String AROUTER_API_GROUP ="com.kimliu.arouter_api.ARouterGroup";

    //path 标记
    String AROUTER_API_PATH = "com.kimliu.arouter_api.ARouterPath";

    // path 方法名
    String PATH_METHOD_NAME = "getPathMap";
    // group方法名
    String GROUP_METHOD_NAME = "getGroupMap";
    // ARouterPath 方法中的变量名
    String PATH_VAR1 = "pathMap";

    // 路由组，中的 Group 里面 的 变量名 1
    String GROUP_VAR1 = "groupMap";

    // 路由组，PATH 最终要生成的 文件名
    String PATH_FILE_NAME = "ARouter$$Path$$";

    // 路由组，GROUP 最终要生成的 文件名
    String GROUP_FILE_NAME = "ARouter$$Group$$";


    // 获取参数的注解 路径
    String PARAMETER_ANNOTATION_PATH = "com.kimliu.arouter_api.Parameter";

    // ParameterGet 接口的路径
    String PARAMETER_GET_PATH = "com.kimliu.arouter_api.ParameterGet";


    // ARouter api 的 ParameterGet 方法参数的名字
    String PARAMETER_NAME = "targetParameter";

    //ARouter api 的 ParmeterGet 方法的名字
    String PARAMETER_METHOD_NAME = "getParameter";

    // String全类名
    public static final String STRING = "java.lang.String";

    // ARouter aip 的 ParmeterGet 的 生成文件名称 $$Parameter
    String PARAMETER_FILE_NAME = "$$Parameter";





}
