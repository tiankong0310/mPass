package com.ibyte.framework.support.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @Description: <类成员扫描>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-22
 */
public class ClassMemberScaner {

    private final Class<?> clazz;

    private List<Class<?>> annotationTypes;

    private boolean stopOnFound = false;

    public ClassMemberScaner(Class<?> clazz) {
        this.clazz = clazz;
    }

    /** 需要扫描的注解 */
    public ClassMemberScaner setAnnotationTypes(Class<?>... types) {
        if (types != null) {
            this.annotationTypes = Arrays.asList(types);
        }
        return this;
    }

    /** 需要扫描的注解 */
    public ClassMemberScaner setAnnotationTypes(List<Class<?>> types) {
        this.annotationTypes = types;
        return this;
    }

    /** 是否当扫描到第一个匹配的注解就停止 */
    public ClassMemberScaner setStopOnFound(boolean stopOnFound) {
        this.stopOnFound = stopOnFound;
        return this;
    }

    /** 开始扫描 */
    public void scan(Consumer<AnnotatedElement> consumer) {
        List<String> visited = new ArrayList<>();
        if (scanMethod(consumer)) {
            return;
        }
        scanField(clazz, visited, consumer);
    }

    /** 递归扫描字段 */
    private boolean scanField(Class<?> clazz, List<String> visited,
                              Consumer<AnnotatedElement> consumer) {
        // 扫描当前类
        for (Field field : clazz.getDeclaredFields()) {
            String name = field.getName();
            if (!visited.contains(name)) {
                if (handle(field, consumer)) {
                    return true;
                }
                visited.add(name);
            }
        }
        // 扫描父类
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && superClazz != Object.class) {
            if (scanField(superClazz, visited, consumer)) {
                return true;
            }
        }
        return false;
    }

    /** 扫描public方法 */
    private boolean scanMethod(Consumer<AnnotatedElement> consumer) {
        for (Method method : clazz.getMethods()) {
            if (handle(method, consumer)) {
                return true;
            }
        }
        return false;
    }

    /** 触发回调 */
    private boolean handle(AnnotatedElement member,
                           Consumer<AnnotatedElement> consumer) {
        if (annotationTypes != null) {
            for (Class<?> type : annotationTypes) {
                @SuppressWarnings("unchecked")
                Annotation annotation = member
                        .getAnnotation((Class<Annotation>) type);
                if (annotation != null) {
                    consumer.accept(member);
                    if (stopOnFound) {
                        return true;
                    }
                    break;
                }
            }
        } else {
            Annotation[] annotations = member.getAnnotations();
            if (annotations != null && annotations.length > 0) {
                consumer.accept(member);
            }
        }
        return false;
    }


}
