package com.ibyte.framework.support.builder;

import com.alibaba.fastjson.JSONObject;
import com.ibyte.common.i18n.ResourceUtil;
import com.ibyte.common.util.StringHelper;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.domain.ExtensionImpl;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.util.PluginReflectUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * @Description: <Extension实例构造>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-23
 */
public class ExtensionBuilder {

    /**
     * 从本地的Json文件加载
     */
    public static ExtensionImpl newExtension(ExtensionPointImpl point,
                                             Class<?> clazz, JSONObject json)
            throws ReflectiveOperationException {
        ExtensionImpl extension = new ExtensionImpl();
        if (json != null) {
            extension.setConfig(json);
        }
        extension.setPoint(point);
        extension.setRefClass(clazz);
        extension.setElementType(ElementType.TYPE);
        // 校验、补全
        validate(extension);
        format(extension);
        return extension;
    }

    /**
     * 从注解加载
     */
    public static ExtensionImpl newExtension(ExtensionPointImpl point,
                                             Class<?> clazz, Annotation annotation)
            throws ReflectiveOperationException {
        ExtensionImpl extension = new ExtensionImpl();
        JSONObject json = PluginReflectUtil.annotationToJson(annotation);
        if (point.getConfig() != null) {
            extension.setConfig(json);
        }
        extension.setPoint(point);
        extension.setRefClass(clazz);
        extension.setElementType(ElementType.TYPE);
        // 校验、补全
        validate(extension);
        format(extension);
        return extension;
    }

    /**
     * 从本地的Json文件加载
     */
    public static ExtensionImpl newExtension(ExtensionPointImpl point,
                                             Class<?> clazz, Member member, JSONObject json)
            throws ReflectiveOperationException {
        ExtensionImpl extension = new ExtensionImpl();
        if (json != null) {
            extension.setConfig(json);
        }
        extension.setPoint(point);
        extension.setRefClass(clazz);
        extension.setElementName(member.getName());
        extension.setElementType(member instanceof Method ? ElementType.METHOD
                : ElementType.FIELD);
        // 校验、补全
        validate(extension);
        format(extension);
        return extension;
    }

    /**
     * 从注解加载
     */
    public static ExtensionImpl newExtension(ExtensionPointImpl point,
                                             Class<?> clazz, Member member, Annotation annotation)
            throws ReflectiveOperationException {
        ExtensionImpl extension = new ExtensionImpl();
        JSONObject json = PluginReflectUtil.annotationToJson(annotation);
        if (point.getConfig() != null) {
            extension.setConfig(json);
        }
        extension.setPoint(point);
        extension.setRefClass(clazz);
        extension.setElementName(member.getName());
        extension.setElementType(member instanceof Method ? ElementType.METHOD
                : ElementType.FIELD);
        // 校验、补全
        validate(extension);
        format(extension);
        return extension;
    }

    /**
     * 校验
     */
    private static void validate(ExtensionImpl extension) {
        Class<?> baseOn = extension.getPoint().getBaseOnClass();
        if (baseOn != null) {
            if (!baseOn.isAssignableFrom(extension.getRefClass())) {
                String message = "根据扩展点" + extension.getPoint().getId() + "的要求，"
                        + extension.getRefName() + "必须基于："
                        + baseOn.getName();
                throw new RuntimeException(message);
            }
        }
    }

    /**
     * 信息补全
     */
    private static void format(ExtensionImpl extension)
            throws ReflectiveOperationException {
        // config
        JSONObject config = extension.getConfig();
        if (config == null) {
            config = new JSONObject();
            extension.setConfig(config);
        }
        // id
        if (StringUtils.isBlank(extension.getId())) {
            if (extension.getElementName() == null) {
                config.put("id", extension.getRefName());
            } else {
                config.put("id", StringHelper.join(extension.getRefName(), ".",
                        extension.getElementName()));
            }
        }
        // label
        if (StringUtils.isBlank(extension.getLabel())
                && StringUtils.isNotBlank(extension.getMessageKey())) {
            config.put("label",
                    ResourceUtil.getString(extension.getMessageKey()));
        }
        // module
        extension.setModule(LocalMetaContextHolder.get()
                .matchModule(extension.getRefName()));
    }
}
