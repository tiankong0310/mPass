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
> 本地拓展注解待实现，元数据处理待实现
