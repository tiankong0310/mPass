package com.ibyte.component.annotaion;

import com.ibyte.framework.plugin.annotation.GlobalExtensionPoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基础业务实体扩展点
 * 暂时只适用于支持数据导出的业务场景
 *
 * @author li.Shangzhi
 * @Date: 2019年10月28日 23:26:12
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@GlobalExtensionPoint(label = "基础业务实体扩展点")
public @interface BaseEntity {

    //模糊搜索字段
    String searchField() default "fdName";

    // 基础字段
    String[] baseColumns() default {"fdId", "fdCreateTime"};

    // 列表显示字段
    String[] extraColumns() default {};
}
