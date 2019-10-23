package com.ibyte.framework.support.builder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibyte.framework.plugin.annotation.BaseOnProperty;
import com.ibyte.framework.plugin.annotation.GlobalExtensionPoint;
import com.ibyte.framework.plugin.annotation.LocalExtensionPoint;
import com.ibyte.framework.plugin.annotation.ProviderProperty;
import com.ibyte.framework.support.LocalMetaContextHolder;
import com.ibyte.framework.support.domain.ExtensionPointImpl;
import com.ibyte.framework.support.util.ClassMemberScaner;
import com.ibyte.framework.support.util.PluginReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: <ExtensionPoint实例构造>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-23
 */
@Slf4j
public class ExtensionPointBuilder {

    /**
     * 从注解创建ExtensionPoint
     */
    public static ExtensionPointImpl newExtensionPoint(String id,
                                                       LocalExtensionPoint annotation) {
        ExtensionPointImpl point = new ExtensionPointImpl();
        point.setBaseOnClass(formatClass(annotation.baseOn()));
        point.setConfigClass(formatClass(annotation.config()));
        point.setConfigurable(false);
        point.setGlobal(false);
        point.setId(id);
        point.setLabel(formatString(annotation.label()));
        point.setManager(formatClass(annotation.manager()));
        point.setModule(LocalMetaContextHolder.get().matchModule(id));
        point.setOrdered(annotation.ordered());
        point.setSingleton(annotation.singleton());

        format(point);
        return point;
    }

    /**
     * 从注解创建ExtensionPoint
     */
    public static ExtensionPointImpl newExtensionPoint(String id,
                                                       GlobalExtensionPoint annotation) {
        ExtensionPointImpl point = new ExtensionPointImpl();
        point.setBaseOnClass(formatClass(annotation.baseOn()));
        point.setConfigClass(formatClass(annotation.config()));
        point.setConfigurable(annotation.configurable());
        point.setGlobal(true);
        point.setId(id);
        point.setLabel(formatString(annotation.label()));
        point.setListener(formatString(annotation.listener()));
        point.setModule(LocalMetaContextHolder.get().matchModule(id));
        point.setOrdered(annotation.ordered());
        point.setSingleton(annotation.singleton());

        format(point);
        return point;
    }

    /**
     * 从JSON的配置中创建一个临时使用的扩展点信息
     */
    public static ExtensionPointImpl newVirtualExtensionPoint(JSONObject json) {
        ExtensionPointImpl point = new ExtensionPointImpl();
        point.setId(json.getString("id"));
        point.setConfig(json.getString("config"));
        JSONArray array = json.getJSONArray("superPoints");
        List<String> types = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                types.add(array.getString(i));
            }
        }
        types.add(point.getId());
        point.setAnnotationTypes(types);
        return point;
    }

    /**
     * 从注解创建ExtensionPoint并继承superPoint的信息
     */
    public static ExtensionPointImpl newExtensionPoint(String id,
                                                       ExtensionPointImpl superPoint, Annotation annotation)
            throws ReflectiveOperationException {
        JSONObject json = PluginReflectUtil.annotationToJson(annotation);

        ExtensionPointImpl point = new ExtensionPointImpl();
        point.setBaseOn(json.containsKey("baseOn")
                ? json.getString("baseOn")
                : superPoint.getBaseOn());
        point.setConfig(json.containsKey("config")
                ? json.getString("config")
                : superPoint.getConfig());
        point.setConfigurable(json.containsKey("configurable")
                ? json.getBoolean("configurable")
                : superPoint.isConfigurable());
        point.setGlobal(superPoint.isGlobal());
        point.setId(id);
        point.setLabel(json.containsKey("label")
                ? json.getString("label")
                : superPoint.getLabel());
        point.setManager(json.containsKey("manager")
                ? PluginReflectUtil.classForName(json.getString("manager"))
                : superPoint.getManager());
        point.setListener(json.containsKey("listener")
                ? json.getString("listener")
                : superPoint.getListener());
        point.setModule(LocalMetaContextHolder.get().matchModule(id));
        point.setOrdered(json.containsKey("ordered")
                ? json.getBoolean("ordered")
                : superPoint.isOrdered());
        point.setSingleton(json.containsKey("singleton")
                ? json.getBoolean("singleton")
                : superPoint.isSingleton());

        if (point.getConfigClass() == superPoint.getConfigClass()) {
            point.setBaseOnProperty(superPoint.getBaseOnProperty());
        }
        if (point.getManager() == superPoint.getManager()) {
            point.setProviderProperty(superPoint.getProviderProperty());
        }
        format(point);
        return point;
    }

    /**
     * 字段补全
     */
    private static void format(ExtensionPointImpl point) {
        if (point.isConfigurable() && point.getManager() != null) {
            point.setConfigurable(false);
            log.warn("扩展点：" + point.getId() + "开启了manager的功能，可配置功能将自动关闭！");
        }
        if (point.getConfigClass() != null
                && point.getBaseOnProperty() == null) {
            ClassMemberScaner scaner = new ClassMemberScaner(
                    point.getConfigClass());
            scaner.setAnnotationTypes(BaseOnProperty.class)
                    .setStopOnFound(true).scan(m -> {
                point.setBaseOnProperty(m);
            });
        }
        if (point.getManager() != null && point.getProviderProperty() == null) {
            ClassMemberScaner scaner = new ClassMemberScaner(
                    point.getManager());
            scaner.setAnnotationTypes(ProviderProperty.class)
                    .setStopOnFound(true).scan(m -> {
                point.setProviderProperty(m);
            });
        }
    }

    /**
     * 格式化字符串
     */
    private static String formatString(String value) {
        return StringUtils.isBlank(value) ? null : value;
    }

    /**
     * 格式化Class
     */
    private static Class<?> formatClass(Class<?> clazz) {
        return clazz == Void.class ? null : clazz;
    }


}
