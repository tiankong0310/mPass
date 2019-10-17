package com.ibyte.framework.support.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.util.JsonUtil;
import com.ibyte.common.util.ReflectUtil;
import com.ibyte.framework.support.ApplicationContextHolder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @Description: <插件工厂反射工具>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-17
 */
public class PluginReflectUtil extends ReflectUtil {

    /**
     * 将注解的信息写到Bean中
     */
    @SuppressWarnings("unchecked")
    public static <T> T annotationToBean(Annotation ann, Type targetType)
            throws ReflectiveOperationException {
        return (T) JsonUtil.convert(annotationToJson(ann), targetType);
    }

    /**
     * 将注解的信息写到Json中
     */
    public static JSONObject annotationToJson(Annotation ann)
            throws ReflectiveOperationException {
        Method[] methods = ann.getClass().getInterfaces()[0]
                .getDeclaredMethods();
        JSONObject json = new JSONObject();
        for (Method method : methods) {
            Object value = method.invoke(ann);
            if (value == Void.class || "".equals(value) || value == null) {
                continue;
            }
            String key = method.getName();
            Class<?> srcType = value.getClass();
            if (srcType.isArray()) {
                srcType = srcType.getComponentType();
                if (srcType.isAnnotation()) {
                    JSONArray array = new JSONArray();
                    for (Object val : (Object[]) value) {
                        array.add(annotationToJson((Annotation) val));
                    }
                    json.put(key, array);
                } else {
                    json.put(key, JSON.toJSON(value));
                }
            } else {
                if (srcType.isAnnotation()) {
                    json.put(key, annotationToJson((Annotation) value));
                } else {
                    json.put(key, JSON.toJSON(value));
                }
            }
        }
        return json;
    }

    /**
     * 获取注解的值，若没有该字段，则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAnnotationValue(Annotation ann, String key) {
        try {
            Method method = ann.getClass().getDeclaredMethod(key);
            return (T) method.invoke(ann);
        } catch (ReflectiveOperationException e) {
        }
        return null;
    }

    /**
     * 取Bean的类名
     */
    public static String getBeanClassName(String name) {
        int index = name.indexOf(ApplicationContextHolder.SPRING_PROXY_FLAG);
        if (index > -1) {
            return name.substring(0, index);
        }
        return name;
    }
}
