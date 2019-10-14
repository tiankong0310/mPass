package com.ibyte.common.core.data.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO 本地拓展注解
// @LocalExtensionPoint(label = "字段处理", baseOn = FieldHandler.class, manager = FieldHandlerManager.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldHandlerExtension {

}
