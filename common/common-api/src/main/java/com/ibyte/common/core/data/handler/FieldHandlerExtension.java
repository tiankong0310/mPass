package com.ibyte.common.core.data.handler;

import com.ibyte.framework.plugin.annotation.LocalExtensionPoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@LocalExtensionPoint(label = "字段处理", baseOn = FieldHandler.class, manager = FieldHandlerManager.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldHandlerExtension {

}
