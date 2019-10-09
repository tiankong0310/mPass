package com.ibyte.component.spring;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * restController扩展支持，
 * 支持在restController中，子类继承父类时，方法中参数注解@requestbody的传递
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 10:57
 */
public class RequestBodyArgumentResolver extends RequestResponseBodyMethodProcessor {

    public RequestBodyArgumentResolver(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean supported = parameter.hasParameterAnnotation(RequestBody.class);
        if (!supported) {
            Method curMethod = (Method) parameter.getExecutable();
            Class<?> curClass = curMethod.getDeclaringClass();
            List<Class<?>> supperList =new ArrayList<>(Arrays.asList(curClass.getInterfaces()));
            Class superclass=curClass.getSuperclass();
            if(superclass!=null && !Object.class.equals(superclass) ){
                supperList.add(superclass);
            }
            for (Class<?> clazz : supperList) {
                if (hasRequestBodyAnnotation(clazz, parameter)) {
                    supported = true;
                    break;
                }
            }
        }
        return supported;
    }

    /**
     * 判断类对应方法的参数是否有requestbody注解
     * @param clazz
     * @param refParam
     * @return
     */
    private boolean hasRequestBodyAnnotation(Class<?> clazz, MethodParameter refParam) {
        if (ClassUtils.hasMethod(clazz, refParam.getExecutable().getName(), refParam.getExecutable().getParameterTypes())) {
            Method tmpMethod = ClassUtils.getMethod(clazz, refParam.getExecutable().getName(), refParam.getExecutable().getParameterTypes());
            MethodParameter supperParam = new MethodParameter(tmpMethod, refParam.getParameterIndex());
            if (supperParam.hasParameterAnnotation(RequestBody.class)) {
                return true;
            }
        }
        return false;
    }
}
