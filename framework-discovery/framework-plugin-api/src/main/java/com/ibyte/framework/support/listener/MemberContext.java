package com.ibyte.framework.support.listener;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Description: <成员扫描上下文>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-18
 */
@Getter
public class MemberContext extends AbstractContext {
    private static final String IS = "is";
    private static final String GETTER = "get";
    private static final String SETTER = "set";

    private AnnotatedElement member;

    public MemberContext(AnnotatedElement member) {
        this.member = member;
    }

    /** 若member是一个字段或getter/setter，则返回字段名，否则返回null */
    public String getPropertyName() {
        if (member instanceof Field) {
            return ((Field) member).getName();
        } else if (member instanceof Method) {
            Method method = (Method) member;
            int count = method.getParameterCount();
            String name = method.getName();
            if (count == 0) {
                if (name.startsWith(IS)) {
                    return StringUtils
                            .uncapitalize(name.substring(IS.length()));
                } else if (name.startsWith(GETTER)) {
                    return StringUtils
                            .uncapitalize(name.substring(GETTER.length()));
                }
            } else if (count == 1) {
                if (name.startsWith(SETTER)) {
                    return StringUtils
                            .uncapitalize(name.substring(SETTER.length()));
                }
            }
        }
        return null;
    }

    /** 获取注解 */
    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        return member.getAnnotation(clazz);
    }
}

