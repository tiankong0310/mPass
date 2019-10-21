package com.ibyte.framework.meta.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: <数据字典注解>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-21
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaEntity {

    /**
     * 国际化Key
     *
     * @return
     */
    String messageKey() default "";

    /**
     * 显示属性
     *
     * @return
     */
    String displayProperty() default "";

    /**
     * VO类，一般不需要设置，除非Api层不继承AbstractApi
     *
     * @return
     */
    Class<?> voClass() default Void.class;

}
