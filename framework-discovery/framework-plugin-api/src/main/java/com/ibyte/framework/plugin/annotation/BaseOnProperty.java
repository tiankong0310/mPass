package com.ibyte.framework.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在ExtensionPoint的config类接收注解所在类的字段或setter的方法
 * 
 * @author 李尚志
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseOnProperty {
}
