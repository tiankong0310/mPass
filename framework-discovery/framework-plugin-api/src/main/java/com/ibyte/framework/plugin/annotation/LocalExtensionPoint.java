package com.ibyte.framework.plugin.annotation;

import java.lang.annotation.*;

/**
 * 本地扩展点的声明采用注解的方式声明，并在注解上使用LocalExtensionPoint的注解<br>
 *
 * <li>若定义了manager类，插件工厂在初始化时会自动往manager中注入Provider的信息，configurable强制为false。</li>
 * <li>若定义了config类，则Plugin.getProvider的class参数为config，返回值为config类的实例。</li>
 * <li>若未定义了config类，但定义了baseOn，则Plugin.getProvider的class参数为baseOn，返回值为注解所在类的实例（优先Spring的Bean）。</li>
 * <li>未定义config，未定义baseOn，则扩展点无效</li>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalExtensionPoint {

    /**
     * 插件名
     */
    String label();

    /**
     * 是否单例
     */
    boolean singleton() default false;

    /**
     * 是否有序
     */
    boolean ordered() default false;

    /**
     * 扩展点注解所在类的接口或基类的约束。
     */
    Class<?> baseOn() default Void.class;

    /**
     * 配置类，接收扩展点注解参数信息的地方。 <br>
     * 配置类可使用@BaseOnProperty注解，标记需要接收注解所在类的信息的字段或Setter方法。
     */
    Class<?> config() default Void.class;

    /**
     * 接管Provider数据，接管后不能再通过Plugin接口获取。<br>
     * 管理类可使用@ProviderProperty注解，标记需要接收Provider信息的字段或Setter方法，接收的字段类型若为Map，则注解必须定义id字段。
     */
    Class<?> manager() default Void.class;

    /**
     * 若插件的@Target为FIELD或METHOD的时，需指定要扫描的类的特征（即注解）
     */
    Class<? extends Annotation>[] scanMemberFor() default {};
}
