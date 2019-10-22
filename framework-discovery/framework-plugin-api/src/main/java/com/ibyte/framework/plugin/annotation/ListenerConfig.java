package com.ibyte.framework.plugin.annotation;

import com.ibyte.framework.plugin.spi.PluginListener;

import java.lang.annotation.*;

/**
 * @Description: <类扫描监听配置，该注解跟作用在哪个注解上无关>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ListenerConfig {
    /** 要监听有哪些注解的类 */
    Class<? extends Annotation>[] classAnnotation();

    /** 要监听这些类字段、方法上的那些注解 */
    Class<? extends Annotation>[] memberAnnotation() default {};

    /** 监听器，实现PluginListener接口 */
    Class<? extends PluginListener> listener();
}
