package com.kimliu.arouter_compiler;

import com.google.auto.service.AutoService;
import com.kimliu.arouter_annotation.ARouter;
import com.kimliu.arouter_annotation.bean.RouteBean;
import com.kimliu.arouter_compiler.config.ProcessorConfig;
import com.kimliu.arouter_compiler.utils.ProcesserUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7) //指定的JDK版本
@SupportedAnnotationTypes({ProcessorConfig.AROUTE_PACKAGE}) // 作用于哪个注解
@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.PACKAGENAMEFORAPT}) // 接收由别的组件传递过来的组名
public class ARouterProcesser extends AbstractProcessor {

    // 以下为固定写法 用来生成Java文件

    // 操作Element的工具类 （类、函数、属性其实都是Element）
    private Elements elementTool;

    // type（类）的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // 打印日志
    private Messager messager;

    // 文件生成器 生成类等
    private Filer filer;

    private String mOptions;
    private String mPackageNameForAPT;

    // 创建仓库一(缓存) ： Path仓库
    // Map<"personal", List<RouterBean>>
    private Map<String, List<RouteBean>> mAllPathMap = new HashMap<>();

    // 创建仓库二（缓存）：Group仓库
    // Map<"personal", "ARouter$$Path$$personal.class">
    private Map<String,String> mAllGroupMap = new HashMap<>();


    /**
     * 做初始化的工作，就像Activity中的oncreate方法一样
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();

        /*
        接收从APP传递过来的数据 在要传值的module的build.gradle文件中写上
        javaCompileOptions{
            annotationProcessorOptions{
                arguments = [moduleName:project.getName(),packageNameForAPT:packageNameForAPT]
            }
        }
         */

        mOptions = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        mPackageNameForAPT = processingEnvironment.getOptions().get(ProcessorConfig.PACKAGENAMEFORAPT);
        messager.printMessage(Diagnostic.Kind.NOTE,"==> options:"+ mOptions);
        messager.printMessage(Diagnostic.Kind.NOTE,"==> packageName:"+ mPackageNameForAPT);

        if (mOptions != null && mPackageNameForAPT != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT Completed....");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT NOT Completed...");
        }
    }

    /**
     * 注解处理器的关键方法，在这里处理注解，生成Java文件
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(set.isEmpty()){
            messager.printMessage(Diagnostic.Kind.NOTE,"未发现@ARouter");
            return false;
        }

        // 1. 获取所有被 @ARouter 注解的元素的集合 这里得到的值 是被ARouter注解的类的集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);

        // 获取Activity的类信息
        TypeElement typeElement = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGENAME);
        TypeMirror activityTypeMirror = typeElement.asType();


        // 2. 遍历所有的类元素
        for (Element element : elements) {
            // 获取类节点的包名
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE,"packageName:"+packageName);
            // 获取简单类名
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE,"===> @ARouter Annotation:"+className);

            // 拿到注解
            ARouter aRouter = element.getAnnotation(ARouter.class);


            // 对用户的使用进行规则的校验
            // 对路由对象进行封装
            RouteBean routeBean = new RouteBean.Builder()
                    .addGroup(aRouter.group())
                    .addPath(aRouter.path())
                    .addElement(element)
                    .build();

            // 1. 校验1 ： @ARouter只可以用于Activity之上
            // 获取每一个element的具体详情，如继承了哪个类等
            TypeMirror typeMirror = element.asType();
            // typeMirror 是否是Activity的子类
            if(typeTool.isSubtype(typeMirror,activityTypeMirror)){
                // 如果是 填入类型
                routeBean.setTypeEnum(RouteBean.TypeEnum.ACTIVITY);
            }else{
                // 抛出异常
                throw new RuntimeException("@ARouter just for Activity");
            }

            // 校验二： 校验用户是否按照规则编写path
            if(checkRoutePath(routeBean)){
                // 说明routeBean赋值成功
                messager.printMessage(Diagnostic.Kind.NOTE,"RouteBean check success:"+routeBean.toString());

                // 存入仓库
                List<RouteBean> routeBeans = mAllPathMap.get(routeBean.getGroup());
                if(ProcesserUtils.isEmpty(routeBeans)){
                    routeBeans = new ArrayList<>();
                }
                routeBeans.add(routeBean);
                mAllPathMap.put(routeBean.getGroup(),routeBeans);
            }else{
                // 未按照规范填写Path 规范：/app/MainActivity
                messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter未按照规范填写Path");
            }
        }


        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);

        messager.printMessage(Diagnostic.Kind.NOTE,"==> pathType:"+pathType);
        messager.printMessage(Diagnostic.Kind.NOTE,"==> groupType:"+groupType);

        createPathClass(pathType);
        createGroupClass(pathType,groupType);

        return true;
    }

    /**
     * 生成路由组Group文件，如：ARouter$$Group$$app
     * @param groupType ARouterLoadGroup接口信息
     * @param pathType ARouterLoadPath接口信息
     */
    private void createGroupClass(TypeElement pathType, TypeElement groupType) {
        // 仓库二 缓存二 判断是否有需要生成的类文件
        if (ProcesserUtils.isEmpty(mAllGroupMap) || ProcesserUtils.isEmpty(mAllPathMap)) return;

        // 返回值 这一段 Map<String, Class<? extends ARouterPath>>
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),        // Map
                ClassName.get(String.class),    // Map<String,

                // Class<? extends ARouterPath>> 难度
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        // ? extends ARouterPath
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))) // ? extends ARouterLoadPath
                // WildcardTypeName.supertypeOf() 做实验 ? super

                // 最终的：Map<String, Class<? extends ARouterPath>>
        );

        // 1.方法 public Map<String, Class<? extends ARouterPath>> getGroupMap() {
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME) // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(methodReturns); // 方法返回值

        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),

                // Class<? extends ARouterPath> 难度
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))), // ? extends ARouterPath
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        //  groupMap.put("personal", ARouter$$Path$$personal.class);
        //	groupMap.put("order", ARouter$$Path$$order.class);
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1, // groupMap.put
                    entry.getKey(), // order, personal ,app
                    ClassName.get(mPackageNameForAPT, entry.getValue()));
        }

        // return groupMap;
        methodBuidler.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        // 最终生成的类文件名 ARouter$$Group$$ + personal
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + mOptions;

        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                mPackageNameForAPT + "." + finalClassName);

        // 生成类文件：ARouter$$Group$$app
        try {
            JavaFile.builder(mPackageNameForAPT, // 包名
                    TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(groupType)) // 实现ARouterLoadGroup接口 implements ARouterGroup
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 生成Path类
     * @param pathType ARouterPath 高层标准
     */
    private void createPathClass(TypeElement pathType) {
        if(ProcesserUtils.isEmpty(mAllPathMap)){
            messager.printMessage(Diagnostic.Kind.NOTE,"Path仓库为空，没有要生成的类");
            return;
        }

        // 使用JavaPoet生成Class

        /*
        要生成的代码：
         @Override
         public Map<String, RouterBean> getPathMap() {
              Map<String, RouterBean> pathMap = new HashMap<>();
              pathMap.put("/personal/Personal_Main2Activity", RouterBean.create();
              pathMap.put("/personal/Personal_MainActivity", RouterBean.create());
              return pathMap;
            }
         */

        // 1. 生成返回值 Map 类型 ： Map<String,RouteBean> 使用ParameterizedTypeName.get()
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouteBean.class)
        );

        // 2. 遍历仓库 app personal order
        for(Map.Entry<String,List<RouteBean>> entry : mAllPathMap.entrySet()){
            // 假如是personal
            // 1. 生成方法
            // 1.方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class) // 给方法上添加注解  @Override
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .returns(methodReturn) // 把Map<String, RouterBean> 加入方法返回
                    ;

            // Map<String, RouterBean> pathMap = new HashMap<>(); // $N == 变量 为什么是这个，因为变量有引用 所以是$N
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),           // Map
                    ClassName.get(String.class),        // Map<String,
                    ClassName.get(RouteBean.class),    // Map<String, RouterBean>
                    ProcessorConfig.PATH_VAR1,          // Map<String, RouterBean> pathMap
                    ClassName.get(HashMap.class)        // Map<String, RouterBean> pathMap = new HashMap<>();
            );

            // 必须要循环，因为有多个
            // pathMap.put("/personal/Personal_Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
            // Personal_Main2Activity.class);
            // pathMap.put("/personal/Personal_MainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY));
            List<RouteBean> pathList = entry.getValue();
            /**
             $N == 变量 变量有引用 所以 N
             $L == TypeEnum.ACTIVITY
             */
            // personal 的细节
            for (RouteBean bean : pathList) {
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1, // pathMap.put
                        bean.getPath(), // "/personal/Personal_Main2Activity"
                        ClassName.get(RouteBean.class), // RouterBean
                        ClassName.get(RouteBean.TypeEnum.class), // RouterBean.Type
                        bean.getTypeEnum(), // 枚举类型：ACTIVITY
                        ClassName.get((TypeElement) bean.getElementType()), // MainActivity.class Main2Activity.class
                        bean.getPath(), // 路径名
                        bean.getGroup() // 组名
                );
            } // TODO end for

            // return pathMap;
            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            // TODO 注意：不能像以前一样，1.方法，2.类  3.包， 因为这里面有implements ，所以 方法和类要合为一体生成才行，这是特殊情况

            // 最终生成的类文件名  ARouter$$Path$$personal
            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();

            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                    mPackageNameForAPT + "." + finalClassName);

            // 生成类文件：ARouter$$Path$$personal
            try {
                JavaFile.builder(mPackageNameForAPT, // 包名  APT 存放的路径
                        TypeSpec.classBuilder(finalClassName) // 类名
                                .addSuperinterface(ClassName.get(pathType)) // 实现ARouterLoadPath接口  implements ARouterPath==pathType
                                .addModifiers(Modifier.PUBLIC) // public修饰符
                                .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                                .build()) // 类构建完成
                        .build() // JavaFile构建完成
                        .writeTo(filer); // 文件生成器开始生成类文件
            } catch (IOException e) {

                e.printStackTrace();
            }

            // 仓库二 缓存二  非常重要一步，注意：PATH 路径文件生成出来了，才能赋值路由组mAllGroupMap
            mAllGroupMap.put(entry.getKey(), finalClassName);
        }
    }

    /**
     *  校验用户是否按照规则填写 @ARouter中的path
     */
    private boolean checkRoutePath(RouteBean routeBean) {
        String groupName = routeBean.getGroup();
        String path = routeBean.getPath();


        //path 必须以 “/” 开头
        if(ProcesserUtils.isEmpty(path) || !path.startsWith("/")){
            // messager 打印信息 如果类型时ERROR 那么 程序必崩
            messager.printMessage(Diagnostic.Kind.NOTE,"@ARouter中的Path必须要以 / 开头");
            return false;
        }

        // "/"最后一次出现的位置 如果是第一位 没写组名
        if(path.lastIndexOf("/") == 0){
            messager.printMessage(Diagnostic.Kind.NOTE,"@ARouter中的Path未按规范填写，应如：/app/MainActivity");
            return false;
        }

        // 下面的情况就是按照规范填写了Path的情形

        // 如果Group没有赋值，才需要截取吧
        if(ProcesserUtils.isEmpty(groupName)){
            // 截取出组名 从第一个"/" 到第二个 "/" 中截取
           groupName = path.substring(1, path.indexOf("/"));
        }

        if(!ProcesserUtils.isEmpty(groupName)){
            if(!groupName.equals(mOptions)){
                messager.printMessage(Diagnostic.Kind.NOTE,"@ARouter中的group必须和组件名一致");
                return false;
            }
            routeBean.setGroup(groupName);
        }else{
            messager.printMessage(Diagnostic.Kind.ERROR,"@ARouter中的group不能为空，请检查path是否按照如：/app/MainActivity 填写");
            return false;
        }

        // 如果返回true 说明routeBean 的Group 赋值成功
        return true;
    }
}