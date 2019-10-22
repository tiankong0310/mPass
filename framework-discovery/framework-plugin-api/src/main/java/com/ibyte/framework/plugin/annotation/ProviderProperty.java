package com.ibyte.framework.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在ExtensionPoint的manager类接收Provider参数的字段或setter的方法
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ProviderProperty {
}
