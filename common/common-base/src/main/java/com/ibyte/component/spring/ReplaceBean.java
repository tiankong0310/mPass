package com.ibyte.component.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: <替换Bean>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 10:50
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReplaceBean {

    /**
     * bean名称
     * @return
     */
    String beanName() default "";

    /**
     * bean class
     * @return
     */
    Class<?> beanClass() default Void.class;


}
