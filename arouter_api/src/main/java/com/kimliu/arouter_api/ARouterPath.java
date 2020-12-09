package com.kimliu.arouter_api;

import com.kimliu.arouter_annotation.bean.RouteBean;

import java.util.Map;

public interface ARouterPath {

    Map<String, RouteBean> getPathMap();

}
