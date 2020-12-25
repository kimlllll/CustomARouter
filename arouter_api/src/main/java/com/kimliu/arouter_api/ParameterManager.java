package com.kimliu.arouter_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * 参数管理器
 * 第一步 ：查找生成的类
 * 第二步： 使用生成的类
 *
 */
public class ParameterManager {

    // 单例

    private static ParameterManager instance;

    public static ParameterManager getInstance(){
        if(instance == null){
            synchronized (ParameterManager.class){
                if(instance == null){
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    private LruCache<String,ParameterGet> cache;
    private ParameterManager(){
        cache = new LruCache<>(100);
    }

    // 用于拼接类名，便于查找
    static final String FILE_SUFFIX_NAME = "$$Parameter";

    /**
     * 这个方法就是用于调用 生成的类中的getParameter方法
     * @param activity
     */
    public void loadParameter(Activity activity){
        String className = activity.getClass().getName();

        ParameterGet parameterGet = cache.get(className);
        if(null == parameterGet){
            try {
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                parameterGet = (ParameterGet) aClass.newInstance();
                cache.put(className,parameterGet);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        parameterGet.getParameter(activity);
    }

}
