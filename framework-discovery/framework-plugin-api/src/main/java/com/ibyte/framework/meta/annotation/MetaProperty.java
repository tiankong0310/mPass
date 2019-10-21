package com.ibyte.framework.meta.annotation;

import com.ibyte.framework.meta.MetaConstant.ShowType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: <数据字典属性注解>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Target({ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaProperty {

    /**
     * 国际化Key
     *
     * @return
     */
    String messageKey() default "";

    /**
     * 非空
     *
     * @return
     */
    boolean notNull() default false;

    /**
     * 只读
     *
     * @return
     */
    boolean readOnly() default false;

    /**
     * 显示类型
     *
     * @return
     */
    ShowType showType() default ShowType.AUTO;


}
