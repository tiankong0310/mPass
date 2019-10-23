#  更新记录
## common-base 模块
- 2019年10月8日  
    - 基础模块划分整理common基础公共模块
    - framework-discovery插件工厂模块、系统部署模块
    - 系统SPI拓展开发Banner、config、logback
- 2019年10月9日  
    - component组件完善banner、config、logback、redis、spring;
    - common 常量管理，业务返回实体以及多语言处理;
    - resources 系统资源管理``
- 2019年10月10日  
    - 国际化语言,常用工具类(cookie,json,reflect,trace..)
    - 加密方式(aes,des,md5,rsa..)``
    - 系统错误基类(KmssException,KmssServiceException,ParamsNotValidException...)
    - 系统拦截器(CompositeFilterProxy,CorsSecurityFilter,RsaPublicKeyFilter...)
- 2019年10月11日  
    - Filter-租户信息过滤器、链路id信息过滤器，Web线程拦截器，接收语言信息过滤器完善;
    - Exception-鉴权认证异常,文件操作异常，无效请求异常补充完毕

:confetti_ball: 2019年10月12日 00:24:54  `common-base` 模块初步完成开发

## common-api 模块
- 2019年10月12日  
    - 模块依赖包整理，版本控制迁移至mvn-parent，mvn-min统一管理
    - 展现对象基类，通用展现对象：fdId,fdName，界面展现对象接口，Validation常用校验，constant基础常量
    - mpass开源社区构建`` [mPass开源社区](http://mpass.gitee.io/zh-cn),组件划分,和架构主要在此更新
- 2019年10月13日
    - React前端环境构建,基础包构建,完善mpass开源社区
- 2019年10月14日
    - 模块依赖包架构拆分framework-discovery-api,framework-plugin-client依赖更新
    - 采用HibernateValidato对DTO进行校验,基础数据对象完善，基础API字段处理接口完善
    - 添加IField字段接口，计划支持多种注解，字段处理器完善
- 2019年10月15日
    - 模块依赖包架构拆分framework-discovery-api,framework-plugin-api依赖更新
    - common-base # FieldHandlerExtension 处理本地拓展
    - 本地扩展点的声明采用注解的方式声明，并在注解上使用LocalExtensionPoint的注解
- 2019年10月16日
    - 公司加班到凌晨，晚上没办法更新 !!!
## framework-plugin-api 模块
- 2019年10月17日
    - 本地扩展点的声明采用注解的方式声明，并在注解上使用LocalExtensionPoint的注解
    - 在ExtensionPoint的manager类接收Provider参数的字段或setter的方法
    - redis客户端jvmId处理
    - 插件工厂反射工具
    - 监听系统加载事件
    - 完善模块信息接口
- 2019年10月18日
    - 元数据信息获取 入口，若本地获取不到，则获取远程的数据
    - 设计摘要信息、拓展接口定义、拓展实现
- 2019年10月19日
    - 架构设计 !!!
    - 项目架构图完善
- 2019年10月20日
    - 上下文基类、上下文扫描、插件扫描监听、扫描成员上下文、插件扫描监听
- 2019年10月21日
    - 模块信息加载、代理对接口进行序列化与反序列化
    - 插件监听 Entity信息扫描填充 、配置类扫描监听、插件扫描监听管理器
    - 数据字典特征注解、数据字典注解
- 2019年10月22日
    - 远程API代理生成工厂、本地API代理生成工厂
    - 插件工厂上下文->扩展点ID -> 扩展
    - LocalExtensionPoint 拓展、拓展点监听器ExtensionChangedListener
    - 本地扩展点的声明采用注解的方式声明
    - 类扫描监听配置
    - 远程api接口扫描
- 2019年10月23日
    - 插件扫描器完善、Extension实例构造完善
    - 插件工厂加载器完善、ExtensionPoint实例构造
    - 监听ApplicationReadyEvent事件，保存插件和元数据
    - 配置服务持久化处理
> 本地拓展注解待实现，元数据处理待实现
