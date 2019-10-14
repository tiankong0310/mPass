<p align="center">
	<a href="http://mpass.gitee.io/zh-cn"><img src="https://images.gitee.com/uploads/images/2019/1009/235538_73450b95_1468963.png" width="700"></a>
</p>
<p align="center">
	<a target="_blank" href="https://search.maven.org/search?q=M-PasS">
		<img src="https://img.shields.io/badge/Maven Central-1.12.0-blue.svg" ></img>
	</a>
	<a target="_blank" href="https://gitee.com/ibyte/M-Pass/blob/master/LICENSE">
		<img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" ></img>
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-1.8+-green.svg" ></img>
	</a>
	<a target="_blank" href="https://gitee.com/ibyte/M-Pass" title="API文档">
		<img src="https://img.shields.io/badge/Api Docs-1.12.0-orange.svg" ></img>
	</a>
        <a href='https://gitee.com/ibyte/M-Pass/stargazers'>
	  <img src='https://gitee.com/ibyte/M-Pass/badge/star.svg?theme=white' alt='star'></img>
	</a>
</p>

# mPass （Microservice Pass）
微服务租户级开发平台

 :anger:  :facepunch:   _系统处于开发阶段, 预计**两个月**时间完成整体架构体系_

[TOC]

    ______  ___     ________                      
    ___   |/  /     ___  __ \_____ _______________
    __  /|_/ /________  /_/ /  __ `/_  ___/_  ___/
    _  /  / /_/_____/  ____// /_/ /_(__  )_(__  )
    /_/  /_/        /_/     \__,_/ /____/ /____/  

**作者**：iByte (码哥 or 李尚志)

[中文说明](https://gitee.com/ibyte/M-Pass/blob/master/README.md)  |  [English](https://gitee.com/ibyte/M-Pass/blob/master/README.en.md)

微服务开发平台 mPass（Microservice Pass）为 租户开发、测试、运营及运维提供云到端的一站式解决方案，能有效降低技术门槛、减少研发成本、提升开发效率，协助企业快速搭建稳定高质量的微服务应用

**核心功能**：
- **快速开发**：工程化的开发框架可以自动生成初始化代码，框架还提供模块化开发模式，适用于多人协作开发。
- **性能优化**：支持运营活动投放一站式全流程创建管理，加载智能化投放能力，最大可能提升运营效率和转化效果，助力业务增长。
- **数字化运营闭环**：所有组件都经历了高并发，大流量的检验，对弱网，保活，容器等都有深度的优化，能够兼容复杂的客户端情况
- **使用方式灵活**：框架与组件并没有强依赖，可分可合，灵活机动。各组件可以独立的提供强大的功能，也可以互相配合优化使用体验，发挥更大的作用

**查看更新记录请移步**
- [x] common
    - [x] common-base
    - [x] common-api [目前进度](https://gitee.com/ibyte/M-Pass/blob/master/UPDAT-RECORD.md)

## 1.1 开发细节
### 1.1 SPI拓展
**SpringBoot SPI拓展**
- 支持自定义Banner拓展
- 支持设置默认config配置文件加载
- 支持实现logback拓展
- 支持实现redis拓展
- 支持实现spring基础拓展

```java
org.springframework.boot.SpringApplicationRunListener=\
  com.ibyte.component.config.DefaultConfigListener,com.ibyte.component.logback.LogbackListener,com.ibyte.component.banner.BannerListener
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.ibyte.component.spring.PackageScanAutoConfig,com.ibyte.component.redis.RedissonAutoConfiguration
com.ibyte.component.config.DefaultConfigFactory=\
  com.ibyte.component.config.InitPropConfigFactory
```
## 1.2 IDGenerator
**ID生成器**
```java
/**
 * 生成主键，36位，[0-9a-w]：时间+w+jvmId+w+流水号+w+随机数填充+w+tenantId
 */
public static String generateID() {
    return generateID(System.currentTimeMillis(), TenantUtil.getTenantId());
}
/**
 * 根据指定时间生成主键
 */
public static String generateID(long time) {
    return generateID(time, TenantUtil.getTenantId());
}

/**
 * 根据指定时间，租户生成主键
 */
public static String generateID(long time, int tenant) {
    StringBuilder id = new StringBuilder(LEN);
    id.append(Long.toUnsignedString(time, 32)).append(SPLIT)
            .append(Long.toUnsignedString(jvmId, 32)).append(SPLIT);
    String tenantId = Integer.toUnsignedString(tenant, 32);
    // 除去租户ID长度
    int length = LEN - tenantId.length() - 1;
    // 序列号填充，若填充完超过指定长度，则取后半部分
    String seqNum = Long.toUnsignedString(SEQ.incrementAndGet(), 32);
    if (id.length() + seqNum.length() > length) {
        seqNum = seqNum.substring(id.length() + seqNum.length() - length,
                seqNum.length());
    }
    id.append(seqNum).append(SPLIT);
    // 随机数填充，不超过leng长度
    while (id.length() < length) {
        id.append(Integer.toUnsignedString(RANDOM.nextInt(), 32));
    }
    id.delete(length, id.length());
    return id.append(SPLIT).append(tenantId).toString();
}
```
### 1.3 异常国际化处理
**RuntimeException 拓展类处理**
```java
public KmssRuntimeException(String messageKey) {
		super(ResourceUtil.getString(messageKey));
		this.code = messageKey;
}
```
### 1.3 系统内置配置
**@EnableConfigurationProperties 与 @ConfigurationProperties 处理**
```java
@Configuration
@EnableConfigurationProperties({SystemConfig.class, TenantConfig.class, LicenseConfig.class, LanguageConfig.class, CorsConfig.class})
```
```java
// 多语言配置信息
@ConfigurationProperties("kmss.lang")
public class LanguageConfig {
    //...
}

// 系统配置
@ConfigurationProperties("kmss.system")
public class SystemConfig {
    //...
}
```

** 核心JAR架构依赖视图 ** 
![输入图片说明](https://images.gitee.com/uploads/images/2019/1009/000659_b0861629_1468963.png "JAR.png")

**交流群**
<table border="0">
    <tr>
        <td>QQ交流群：<font color="red">877056205</font> mPaaS</td>
        <td>微信交流群：<font color="red">iByte_Li</font>(备注拉群要求)</td>
        <td>微信公众号：<font color="red">码农架构</font></td>
    </tr>
    <tr>&nbsp;</tr>
    <tr>
        <td><img title="QQ交流群-877056205" src="package/docs/images/QQ群-877056205.png" height="200" width="200"/></td>
        <td><img title="微信交流群-iByte_Li" src="package/docs/images/个人微信-iByte_Li.png" height="200" width="200"/></td>
        <td><img title="微信公众号-微技术栈" src="package/docs/images/码农架构-公众号.jpg" height="200" width="200"/></td>
    </tr>
</table>
