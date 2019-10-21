package com.ibyte.framework.meta.annotation;

import com.ibyte.framework.plugin.annotation.LocalExtensionPoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: <数据字典特征注解>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@LocalExtensionPoint(label = "数据字典扩展")
//TODO 类扫描监听配置 ListenerConfig
//@ListenerConfig(classAnnotation = Entity.class, memberAnnotation = {
//        Feature.class,
//        MetaProperty.class }, listener = MetaEntityScanListener.class)
public @interface Feature {

    Class<?> config();
}
