package com.kimliu.arouter_compiler.config;

/**
 * 用来记录一些名称 路径等
 */
public interface ProcessorConfig {

    // ARouter 的路径
    String AROUTE_PACKAGE = "com.kimliu.arouter_annotation.ARouter";

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







}
