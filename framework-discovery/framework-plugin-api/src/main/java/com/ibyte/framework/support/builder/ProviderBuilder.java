package com.ibyte.framework.support.builder;

import com.ibyte.common.util.JsonUtil;
import com.ibyte.framework.plugin.Plugin;
import com.ibyte.framework.support.ApplicationContextHolder;
import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.domain.ExtensionPointImpl;

import java.lang.reflect.*;
import java.util.*;

/**
 * @Description: <provider 构造器>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-23
 */
public class ProviderBuilder {

    /**
     * 获取Provider的类型
     */
    public static Class<?> getProviderType(ExtensionPointImpl point) {
        return point.getConfig() == null ? point.getBaseOnClass()
                : point.getConfigClass();
    }

    /**
     * 构造Provider
     */
    public static Object buildProvider(ExtensionImpl extension) {
        ExtensionPointImpl point = extension.getPoint();
        if (point.getConfig() != null) {
            return buildConfigProvider(extension);
        } else if (point.getBaseOn() != null) {
            return Plugin.getApi(extension.getRefName());
        }
        return null;
    }

    /**
     * 构造config的provider信息
     */
    public static Object buildConfigProvider(ExtensionImpl extension) {
        ExtensionPointImpl point = extension.getPoint();
        if (point.getConfigClass() == null) {
            return null;
        }
        Object config = JsonUtil.convert(extension.getConfig(),
                point.getConfigClass());
        // 将baseOn写入config
        AnnotatedElement baseOn = point.getBaseOnProperty();
        if (baseOn != null) {
            if (baseOn instanceof Field) {
                // 字段
                Field field = (Field) baseOn;
                Object value = getBaseOnValue(extension,
                        field.getType());
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    field.set(config, value);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (baseOn instanceof Method) {
                // 方法
                Method method = (Method) baseOn;
                Class<?>[] types = method.getParameterTypes();
                if (types == null || types.length != 1) {
                    throw new RuntimeException("扩展" + extension.getId()
                            + "指定的BaseOn的接收方法必须只有一个参数");
                }
                Object value = getBaseOnValue(extension, types[0]);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                try {
                    method.invoke(config, new Object[] { value });
                } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return config;
    }

    /**
     * 读取baseOn的值
     */
    private static Object getBaseOnValue(ExtensionImpl extension,
                                         Class<?> type) {
        if (type == Class.class) {
            return extension.getRefClass();
        }
        if (type == String.class) {
            return extension.getRefName();
        }
        if (type.isInterface()) {
            return Plugin.getApi(extension.getRefName(), type);
        }
        return Plugin.getApi(extension.getRefName());
    }

    /**
     * 将Extension写入Manager
     */
    public static void writeToManager(ExtensionPointImpl point,
                                      List<ExtensionImpl> extensions)
            throws ReflectiveOperationException {
        AnnotatedElement property = point.getProviderProperty();
        if (property == null) {
            throw new RuntimeException("扩展点" + point.getId()
                    + "指定的Manager必须采用@ProviderProperty指定接收Provider的字段");
        }
        if (property instanceof Field) {
            // 字段
            Field field = (Field) property;
            Object value = getManagerValue(extensions, field.getGenericType());
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (Modifier.isStatic(field.getModifiers())) {
                field.set(null, value);
            } else {
                field.set(ApplicationContextHolder
                        .findOrCreateInstance(point.getManager()), value);
            }
        } else if (property instanceof Method) {
            // 方法
            Method method = (Method) property;
            Type[] types = method.getGenericParameterTypes();
            if (types == null || types.length != 1) {
                throw new RuntimeException("扩展点" + point.getId()
                        + "指定的Manager的接收方法必须只有一个参数");
            }
            Object value = getManagerValue(extensions, types[0]);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (Modifier.isStatic(method.getModifiers())) {
                method.invoke(null, new Object[] { value });
            } else {
                method.invoke(ApplicationContextHolder.findOrCreateInstance(
                        point.getManager()), new Object[] { value });
            }
        }
    }

    /**
     * 取Manager的字段，并转换
     */
    private static Object getManagerValue(List<ExtensionImpl> extensions,
                                          Type type) throws ReflectiveOperationException {
        if (type instanceof Class) {
            if (((Class<?>) type).isArray()) {
                Class<?> componentType = ((Class<?>) type).getComponentType();
                Object[] array = (Object[]) Array.newInstance(
                        componentType, extensions.size());
                for (int i = 0; i < extensions.size(); i++) {
                    array[i] = getManagerValue(extensions.get(i),
                            componentType);
                }
                return array;
            } else {
                return getManagerValue(extensions.get(0), (Class<?>) type);
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) paramType.getRawType();
            if (Collection.class.isAssignableFrom(rawType)) {
                // 列表
                Class<?> componentType = (Class<?>) paramType
                        .getActualTypeArguments()[0];
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < extensions.size(); i++) {
                    list.add(getManagerValue(extensions.get(i),
                            componentType));
                }
                return list;
            } else if (Map.class.isAssignableFrom(rawType)) {
                // Map
                Class<?> componentType = (Class<?>) paramType
                        .getActualTypeArguments()[1];
                Map<String, Object> map = new HashMap<>(16);
                for (int i = 0; i < extensions.size(); i++) {
                    ExtensionImpl extension = extensions.get(i);
                    map.put(extension.getId(),
                            getManagerValue(extension, componentType));
                }
                return map;
            } else {
                return getManagerValue(extensions.get(0), (Class<?>) type);
            }
        }
        throw new RuntimeException(
                "未知的Manager参数类型：" + type.getClass().getName());
    }

    /**
     * 根据类型，转换管理器需要的值
     */
    private static Object getManagerValue(ExtensionImpl extension,
                                          Class<?> type) throws ReflectiveOperationException {
        if (type == Class.class) {
            return extension.getRefClass();
        }
        if (type == String.class) {
            return extension.getRefName();
        }
        ExtensionPointImpl point = extension.getPoint();
        if (point.getConfigClass() == type) {
            return buildConfigProvider(extension);
        } else if (point.getBaseOnClass() == type) {
            return ApplicationContextHolder
                    .findOrCreateInstance(extension.getRefClass());
        }
        throw new RuntimeException(
                "未知的Manager参数类型：" + type.getClass().getName());
    }


}
