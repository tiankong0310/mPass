package com.ibyte.framework.support.listener;

import lombok.Getter;

import java.lang.annotation.Annotation;

/**
 * @Description: <扫描上下文>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
@Getter
public class ClassContext extends AbstractContext {
    private Class<?> refClass;

    public ClassContext(Class<?> refClass) {
        this.refClass = refClass;
    }

    /** 获取注解 */
    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        return refClass.getAnnotation(clazz);
    }

}
