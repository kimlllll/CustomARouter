package com.kimliu.arouter_api;

import java.util.Map;

public interface ArouterGroup {


    Map<String,Class<? extends ARouterPath>> getGroupMap();
}
